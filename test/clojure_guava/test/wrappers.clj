(ns clojure-guava.test.wrappers
  (:require (clojure-guava [factories :as f]
                           [wrappers :as w]))
  (:use clojure.test))

(deftest test-wrapped-maps
  (doseq [m (->> [f/into-guava-immutable-bi-map
                  f/into-guava-immutable-map
                  f/into-guava-immutable-sorted-map]
                 (map #(% [[:foo 1] [:bar 2]]))
                 (map w/wrap))]
    (are [k v]
         (= (get m k) v)
         :foo  1
         :quux nil)
    (are [k v]
         (= (contains? m k) v)
         :foo  true
         :quux false)
    (is (= (m :bar) 2))
    (is (= (m :quux) nil))
    (is (= (m :quux 3) 3))
    (is (= m {:foo 1 :bar 2}))))

(deftest test-wrapped-sets
  (doseq [s (->> [f/into-guava-immutable-set
                  f/into-guava-immutable-sorted-set]
                 (map #(% [:foo :bar]))
                 (map w/wrap))]
    (are [k v]
         (= (get s k) v)
         :foo  :foo
         :quux nil)
    (are [k v]
         (= (contains? s k) v)
         :foo  true
         :quux false)
    (is (= (s :bar) :bar))
    (is (= s #{:foo :bar}))))

(deftest test-wrapped-bi-map
  (let [bm (w/wrap (f/into-guava-immutable-bi-map [[:foo 1] [:bar 2]]))]
    (are [m k v]
         (= (get m k) v)
         bm :foo  1
         bm :quux nil
         (w/invert bm) 2 :bar
         (w/invert bm) 3 nil)
    (are [k n v]
         (= (get bm k n) v)
         :foo  3 1
         :quux 3 3)
    (are [k n v]
         (= (get (w/invert bm) k n))
         1 :quux :foo
         3 :quux :quux)
    (is (= (bm :bar) 2))))

(deftest test-wrapped-list
  (is (= (list 1 2 3) (w/wrap (f/into-guava-immutable-list [1 2 3])))))

(deftest test-wrapped-multimap-contains
  (doseq [mm (->> [f/into-guava-immutable-list-multimap
                   f/into-guava-immutable-multimap
                   f/into-guava-immutable-set-multimap]
                  (map #(% [[:foo [1]] [:bar [2]] [:foo [3]]]))
                  (map w/wrap))]
    (are [k v]
         (= (contains? mm k) v)
         :foo  true
         :bar  true
         :quux false)))

(deftest test-wrapped-list-multimap
  (let [lm (w/wrap (f/into-guava-immutable-list-multimap
                    [[:foo [1]] [:foo [2]] [:bar [3 4]]]))]
    (are [k v]
         (= (get lm k) v)
         :foo  (list 1 2)
         :quux (list))
    (is (= (list 3 4) (lm :bar)))))

(deftest test-wrapped-multimap
  (let [mm (w/wrap (f/into-guava-immutable-multimap
                    [[:foo [1]] [:foo [2]] [:bar [3 4]]]))]
    (are [k v]
         (= (get mm k) v)
         :foo  (list 1 2)
         :quux (list))
    (is (= (list 3 4) (mm :bar)))))

(deftest test-wrapped-set-multimap
  (let [sm (w/wrap (f/into-guava-immutable-set-multimap
                    [[:foo [1]] [:foo [2]] [:bar [3 4]]]))]
    (are [k v]
         (= (get sm k) v)
         :foo  #{1 2}
         :quux #{})
    (is (= #{3 4} (sm :bar)))))

(deftest test-wrapped-multiset
  (let [ms (w/wrap (f/into-guava-immutable-multiset [:foo :bar :foo]))]
    (are [k v]
         (= (contains? ms k) v)
         :foo  true
         :bar  true
         :quux false)
    (are [k v]
         (= (get ms k) v)
         :foo  :foo
         :quux nil)
    (is (= [:foo :foo :bar] (seq ms)))
    (are [k c]
         (== (w/occurrences ms k) c)
         :foo  2
         :bar  1
         :quux 0)))
