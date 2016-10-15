geohash-clj
===========

[![Build Status](https://travis-ci.org/bytebutt/geohash-clj.svg?branch=develop)](https://travis-ci.org/bytebutt/geohash-clj)

This library provides a Clojure implementation of [geohash](http://www.geohash.org) encoding and decoding.
I wrote this library as a learning experience to help me understand the geohash algorithm.
Performance is not optimized for production use-cases.

## Installation

TODO

## Usage

Import the library into a namespace.

```clojure
(ns my-cool-project
  (:require [geohash-clj.core :as geohash]))
```

Use the `encode` function to encode a vector of latitude and longitude to a geohash of the desired precision.

```clojure
(geohash/encode [40.6892 -74.0445] 9) ; "dr5r7p4ry"
```

Use the `decode` function to decode a geohash to a vector of latitude and longitude.

```clojure
(geohash/decode "dr5r7p4ry") ; [40.689218044281006 -74.0444827079773]
```

## License

This library is distributed under the terms of the [MIT License](https://opensource.org/licenses/MIT).
