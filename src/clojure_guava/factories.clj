(ns clojure-guava.factories
  (:use clojure-guava.factories.internal)
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

(define-into-factories
  ImmutableBiMap        true  false
  ImmutableList         false false
  ImmutableListMultimap true  false
  ImmutableMap          true  false
  ImmutableMultimap     true  false
  ImmutableMultiset     false false
  ImmutableSet          false false
  ImmutableSetMultimap  true  false
  ImmutableSortedMap    true  true
  ImmutableSortedSet    false true
  )
