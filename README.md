# clojure-guava

This library provides a handful of Clojure wrappers around the
immutable Guava collections (`com.google.common.collect.Immutable*`).

The wrappers implement some of the `clojure.lang.IPersistent*`
interfaces despite *not* being persistent (in fact, they currently
provide no direct means of creating modified copies). This is
primarily so that `contains?` and `get` handle them as expected.

Please note that at this early stage the API is subject to change
without notice.

## Usage

There are two namespaces of interest to user code:
`clojure-guava.factories` and `clojure-guava.wrappers`.

The `factories` namespace provides functions for building the
(unwrapped) Guava immutable collections with the supplied contents in
a manner similar to that of `clojure.core/into`. These carry names
derived from the names of the constructed classes (e.g.
`ImmutableBiMap` -> `into-guava-immutable-bi-map`).

Usage example:

    (into-guava-immutable-bi-map [[:foo 1] [:bar 2]])
    ; => #<RegularImmutableBiMap {:foo=1, :bar=2}>

The `wrappers` namespace provides three functions:

1. `wrap`, which wraps a given immutable Guava collection in a
   Clojure-friendly wrapper;

2. `unwrap`, which extracts the Guava object from a wrapper;

3. `invert`, which inverts a BiMap (possibly wrapped and preserving
   the wrapped/unwrapped status).

## Names of interest

### List of wrapped classes

    com.google.common.collect.ImmutableBiMap
    com.google.common.collect.ImmutableList
    com.google.common.collect.ImmutableListMultimap
    com.google.common.collect.ImmutableMap
    com.google.common.collect.ImmutableMultimap
    com.google.common.collect.ImmutableMultiset
    com.google.common.collect.ImmutableSet
    com.google.common.collect.ImmutableSetMultimap
    com.google.common.collect.ImmutableSortedMap
    com.google.common.collect.ImmutableSortedSet

### List of factory function names

    into-guava-immutable-multimap
    into-guava-immutable-bi-map
    into-guava-immutable-map
    into-guava-immutable-sorted-set
    into-guava-immutable-list
    into-guava-immutable-list-multimap
    into-guava-immutable-set-multimap
    into-guava-immutable-set
    into-guava-immutable-multiset
    into-guava-immutable-sorted-map

## Fablo

This work was sponsored by Fablo (http://fablo.eu/). Fablo provides a
set of tools for building modern e-commerce storefronts. Tools include
a search engine, product and search result navigation, accelerators,
personalized recommendations, and real-time statistics and analytics.

## Licence

Copyright (C) 2011 Michał Marczyk

Distributed under the Eclipse Public License, the same as Clojure.
