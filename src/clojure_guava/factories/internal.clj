(ns clojure-guava.factories.internal
  (:require [clojure.string :as str])
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
           (java.util Map$Entry)))

(def non-wrapping-factory-name-prefix "into-guava-")

(defn prepare-factory-name [class-name factory-name-prefix]
  (->> (str/split (name class-name) #"(?=(?<=.)\p{Upper})")
       (map #(.toLowerCase ^String %))
       (interpose "-")
       (cons factory-name-prefix)
       (apply str)
       symbol))

(defn key* [o]
  (if (instance? Map$Entry)
    (key o)
    (nth o 0)))

(defn val* [o]
  (if (instance? Map$Entry)
    (val o)
    (nth o 1)))

(defn put-call [put-call-type builder-sym items-sym]
  (case put-call-type
    :map `(.put ~builder-sym
                (key* (first ~items-sym))
                (val* (first ~items-sym)))
    :multimap `(.putAll ~builder-sym
                        (key* (first ~items-sym))
                        (val* (first ~items-sym)))
    :default `(.add ~builder-sym (first ~items-sym))))

(defmacro define-into-factory [class-name put-call-type ordered?]
  (let [builder-sym 'builder ;(gensym "builder")
        items-sym   'items   ;(gensym "items")
        docstring (str "Collects the given items into a "
                       (.getName (resolve class-name)) ".")]
    `(defn ~(prepare-factory-name class-name non-wrapping-factory-name-prefix)
       ~docstring
       [~items-sym]
       (let [~builder-sym (. ~class-name ~(if ordered? 'naturalOrder 'builder))]
         (loop [~items-sym (seq ~items-sym)]
           (if ~items-sym
             (do ~(put-call put-call-type builder-sym items-sym)
                 (recur (next ~items-sym)))
             (.build ~builder-sym)))))))

(defmacro define-into-factories [& descs]
  `(do ~@(for [[class-name put-call-type ordered?] (partition 3 descs)]
           `(define-into-factory ~class-name ~put-call-type ~ordered?))))
