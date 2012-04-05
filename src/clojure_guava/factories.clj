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
  ImmutableBiMap        :map      false
  ImmutableList         :default  false
  ImmutableListMultimap :multimap false
  ImmutableMap          :map      false
  ImmutableMultimap     :multimap false
  ImmutableMultiset     :default  false
  ImmutableSet          :default  false
  ImmutableSetMultimap  :multimap false
  ImmutableSortedMap    :map      true
  ImmutableSortedSet    :default  true
  )
