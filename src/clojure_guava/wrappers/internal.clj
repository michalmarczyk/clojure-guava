(ns clojure-guava.wrappers.internal
  (:import (com.google.common.collect ImmutableBiMap
                                      ImmutableList
                                      ImmutableListMultimap
                                      ImmutableMap
                                      ImmutableMultimap
                                      ImmutableMultiset
                                      ImmutableSet
                                      ImmutableSetMultimap
                                      ImmutableSortedMap
                                      ImmutableSortedSet)
           (clojure.lang IPersistentMap MapEntry)
           (java.util Map$Entry)))

(defprotocol PWrap
  (-wrap [this]))

(defprotocol PUnwrap
  (-unwrap [this]))

(defprotocol PBiMap
  (-invert [this]))

(defn not-implemented [m]
  (throw (RuntimeException. (format "%s not implemented" m))))

(defn emit-not-implemented [& keys-and-arities]
  (mapcat (fn [[k a & [tss]]]
            (let [s (symbol (name k))
                  emit-args (fn [ts]
                              (if (seq ts)
                                (->> (repeatedly a #(gensym "arg"))
                                     (map (fn [t arg]
                                            (with-meta arg {:tag t}))
                                          ts)
                                     (cons (gensym "arg"))
                                     vec)
                                (vec (repeatedly (inc a) #(gensym "arg")))))
                  emit-one (fn [s ts]
                             (let [s (if-let [t (first ts)]
                                       (with-meta s {:tag t})
                                       s)]
                               `(~s ~(emit-args (next ts))
                                    (not-implemented ~(name s)))))]
              (if (empty? tss)
                (list (emit-one s nil))
                (map (partial emit-one s) tss))))
          keys-and-arities))

(defn emit-delegating-impls [t o & keys-and-arities]
  (map (fn [[k a]]
         (let [s (with-meta (symbol (name k)) {:tag t})
               args (repeatedly (inc a) #(gensym "arg"))]
           `(~s ~(vec args)
                (. ~o ~s ~@(next args)))))
       keys-and-arities))

(defmulti implement (fn [_ iface-or-proto] iface-or-proto))

(defmethod implement :ilookup [{:keys [type wrapped-object-field-name]} _]
  (case type

    (:bimap :map)
    `(clojure.lang.ILookup
      (~'valAt [~'this ~'key]
               (.get ~wrapped-object-field-name
                     ~'key))
      (~'valAt [~'this ~'key ~'not-found]
               (if-let [val# (.get ~wrapped-object-field-name
                                   ~'key)]
                 val#
                 ~'not-found)))

    :multimap
    `(clojure.lang.ILookup
      (~'valAt [~'this ~'key]
               (-wrap (.get ~wrapped-object-field-name ~'key)))
      (~'valAt [~'this ~'key ~'not-found]
               (let [val# (.get ~wrapped-object-field-name ~'key)]
                 (if (seq val#)
                   val#
                   ~'not-found))))

    ()))

(defmethod implement :ifn [{:keys [type wrapped-object-field-name]} _]
  (if (#{:bimap :map :multimap} type)
    `(clojure.lang.IFn
      (~'invoke [~'this ~'key] (.valAt ~'this ~'key))
      (~'invoke [~'this ~'key ~'not-found]
                (.valAt ~'this ~'key ~'not-found)))
    `(clojure.lang.IFn
      (~'invoke [~'this ~'key]
                (if (.contains ~wrapped-object-field-name ~'key)
                  ~'key
                  nil)))))

(defmethod implement :seqable [{:keys [wrapped-object-field-name type]} _]
  `(clojure.lang.Seqable
    ~(if-not (= :multimap type)
       `(~'seq [~'this] (seq ~wrapped-object-field-name))
       `(~'seq [~'this]
               (map #(MapEntry. % (-wrap (.get ~wrapped-object-field-name %)))
                    (.keySet ~wrapped-object-field-name))))))

(defmethod implement :counted [{:keys [wrapped-object-field-name]} _]
  `(clojure.lang.Counted
    (~'count [~'this] (.size ~wrapped-object-field-name))))

(defmethod implement :iobj
  [{:keys [wrapping-type-name wrapped-object-field-name]} _]
  `(clojure.lang.IObj
    (~'withMeta [~'this ~'new-meta-map]
                (new ~wrapping-type-name
                     ~wrapped-object-field-name
                     ~'new-meta-map))))

(defmethod implement :imeta [{:keys [meta-map-field-name]} _]
  `(clojure.lang.IMeta
    (~'meta [~'this] ~meta-map-field-name)))

(defmethod implement :object
  [{:keys [wrapped-class-name wrapping-type-name wrapped-object-field-name]} _]
  `(java.lang.Object
    (~'equals [~'this ~'that]
              (if (instance? clojure_guava.wrappers.internal.PUnwrap ~'that) #_(satisfies? PUnwrap ~'that)
                (.equals ~wrapped-object-field-name (-unwrap ~'that))
                (.equals ~wrapped-object-field-name ~'that)))
    (~'hashCode [~'this] (.hashCode ~wrapped-object-field-name))))

(defmethod implement :punwrap [{:keys [wrapped-object-field-name]} _]
  `(PUnwrap (~'-unwrap [~'this] ~wrapped-object-field-name)))

(defmulti extra-impls :type)

(defmethod extra-impls :default [_] ())

(defmethod extra-impls :bimap [specs]
  (concat
   `(PBiMap
     (~'-invert [~'this]
                (with-meta (-wrap (.inverse ^ImmutableBiMap (-unwrap ~'this)))
                  (meta ~'this))))
   (extra-impls (assoc specs :type :map))))

(defmethod extra-impls :map [{:keys [wrapped-object-field-name]}]
  `(java.util.Map

    ~@(emit-not-implemented [:clear 0]
                            [:put 2]
                            [:putAll 1]
                            [:remove 1])

    ~@(emit-delegating-impls java.util.Map
                             wrapped-object-field-name
                             [:containsKey 1]
                             [:containsValue 1]
                             [:entrySet 0]
                             [:get 1]
                             [:isEmpty 0]
                             [:keySet 0]
                             [:size 0]
                             [:values 0])

    clojure.lang.IPersistentCollection

    ~@(emit-not-implemented [:cons 1])

    (~'empty [~'this] {})

    (~'equiv [~'this ~'that] (.equals ~wrapped-object-field-name ~'that))))

(defmethod extra-impls :set [{:keys [wrapped-object-field-name]}]
  `(java.util.Set

    ~@(emit-not-implemented [:add 1]
                            [:addAll 1]
                            [:clear 0]
                            [:remove 1]
                            [:removeAll 1]
                            [:retainAll 1])

    ~@(emit-delegating-impls java.util.Set
                             wrapped-object-field-name
                             [:contains 1]
                             [:containsAll 1]
                             [:isEmpty 0]
                             [:iterator 0]
                             [:size 0]
                             [:toArray 0]
                             [:toArray 1])

    clojure.lang.IPersistentCollection

    ~@(emit-not-implemented [:cons 1])

    (~'empty [~'this] #{})

    (~'equiv [~'this ~'that] (.equals ~wrapped-object-field-name ~'that))

    clojure.lang.IPersistentSet

    ~@(emit-not-implemented [:disjoin 1])

    (~'get [~'this ~'key]
           (if (.contains ~wrapped-object-field-name ~'key)
             ~'key
             nil))))

(defmethod extra-impls :list [{:keys [wrapped-object-field-name]}]
  `(java.util.List

    ~@(emit-not-implemented [:add 1]
                            [:add 2]
                            [:addAll 1]
                            [:addAll 2]
                            [:clear 0]
                            [:remove 1 '#{[Object int] [boolean Object]}]
                            [:removeAll 1]
                            [:retainAll 1]
                            [:set 2])

    ~@(emit-delegating-impls java.util.List
                             wrapped-object-field-name
                             [:contains 1]
                             [:containsAll 1]
                             [:get 1]
                             [:indexOf 1]
                             [:iterator 0]
                             [:lastIndexOf 1]
                             [:listIterator 0]
                             [:listIterator 1]
                             [:toArray 0]
                             [:toArray 1])

    (~'subList [~'this ~'from ~'to]
               (-wrap (.subList ~wrapped-object-field-name ~'from ~'to)))))

(defmethod extra-impls :multimap [{:keys [wrapped-object-field-name] :as specs}]
  (map (fn [item]
         (if-not (and (seq? item) (= 'entrySet (first item)))
           item
           `(entrySet [~'this] (.entrySet ^Multimap ~'this))))
       (extra-impls (assoc specs :type :map))))

(defmethod extra-impls :multiset [specs]
  (extra-impls (assoc specs :type :set)))

(defmacro define-wrapper
  "Define a Clojure wrapper for the immutable Guava collection type indicated
   by the suffix.

   * suffix is the part of the Guava class name following `Immutable'
   * type is one of :set, :multiset, :map, :multimap"
  [suffix type]
  (let [wrapped-class-name (symbol (str "Immutable" suffix))
        wrapping-type-name (symbol (str "WrappedGuava" suffix))
        wrapped-object-field-name 'guava-object
        wrapped-object-tagged-field-name (with-meta wrapped-object-field-name
                                           {:tag wrapped-class-name})
        meta-map-field-name 'meta-map
        meta-map-tagged-field-name (with-meta meta-map-field-name
                                     {:tag IPersistentMap})

        info {:wrapped-class-name wrapped-class-name
              :wrapping-type-name wrapping-type-name
              :wrapped-object-field-name wrapped-object-field-name
              :meta-map-field-name meta-map-field-name
              :type type}

        impls (concat (mapcat (partial implement info)
                              [:ilookup :ifn :counted :seqable :iobj :imeta
                               :object :punwrap])
                      (extra-impls info))]

    ;; TODO: also define factory function for the WrappedGuava*?
    `(do (deftype ~wrapping-type-name [~wrapped-object-tagged-field-name
                                       ~meta-map-tagged-field-name]
           ~@impls)
         (extend-protocol PWrap
           ~wrapped-class-name
           (~'-wrap [~'g] (new ~wrapping-type-name ~'g {})))
         (defmethod print-method ~wrapping-type-name [o# ^java.io.Writer writer#]
           (.write writer#
                   (str "#<" ~(name wrapping-type-name)
                        " " (.toString ^Object (-unwrap o#)) ">"))))))

(defmacro define-wrappers [& suffixes-and-types]
  `(do ~@(map (partial list* `define-wrapper)
              (partition 2 suffixes-and-types))))
