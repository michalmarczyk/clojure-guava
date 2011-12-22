(ns clojure-guava.test.factories
  (:require [clojure-guava.factories :as f])
  (:use clojure.test))

;;; a handful of sanity checks; all the factories are exercised in the
;;; wrapper tests

(deftest test-immutable-bi-map
  (let [gibm (f/into-guava-immutable-bi-map
              [[:foo 1] (first {:bar 2})])]
    (are [k v]
         (= (get gibm k) v)
         :foo  1
         :bar  2
         :quux nil)))

(deftest test-immutable-list-multimap
  (let [gilmm (f/into-guava-immutable-list-multimap
               [[:foo 1] (first {:foo 2}) [:bar 3]])]
    (are [k vs]
         (= (vec (.get gilmm k)) vs)
         :foo  [1 2]
         :bar  [3]
         :quux [])))

(deftest test-immutable-map
  (let [gim (f/into-guava-immutable-map
             [[:foo 1] (first {:bar 2})])]
    (are [k v]
         (= (get gim k) v)
         :foo  1
         :bar  2
         :quux nil)))

(deftest test-immutable-sorted-set
  (let [giss (f/into-guava-immutable-sorted-set [:foo])]
    (are [k v]
         (= (.contains giss k) v)
         :foo true
         :bar false)))
