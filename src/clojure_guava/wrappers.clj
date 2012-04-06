(ns clojure-guava.wrappers
  (:use clojure-guava.wrappers.internal)
  (:import (com.google.common.collect ImmutableBiMap
                                      ImmutableList
                                      ImmutableListMultimap
                                      ImmutableMap
                                      ImmutableMultimap
                                      ImmutableMultiset
                                      ImmutableSet
                                      ImmutableSetMultimap
                                      ImmutableSortedMap
                                      ImmutableSortedSet)))

(defn wrap
  "Wraps guava-object in the appropriate wrapper type."
  [guava-object]
  (-wrap guava-object))

(defn unwrap
  "Unwraps a wrapped Guava object."
  [wrapped-guava-object]
  (-unwrap wrapped-guava-object))

(defn invert
  "Inverts a (possibly wrapped) Guava ImmutableBiMap."
  [bimap]
  (if (instance? ImmutableBiMap bimap)
    (.inverse ^ImmutableBiMap bimap)
    (-invert bimap)))

(define-wrappers
  BiMap        :bimap
  List         :list
  ListMultimap :multimap
  Map          :map
  Multimap     :multimap
  Multiset     :multiset
  Set          :set
  SetMultimap  :multimap
  SortedMap    :map
  SortedSet    :set
  )
