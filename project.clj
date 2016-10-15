(defproject com.bytebutt/geohash-clj "1.0.0-SNAPSHOT"
  :description "Library for encoding and decoding geohashes (http://www.geohash.org)"
  :url "https://github.com/bytebutt/geohash-clj"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :target-path "target/%s"
  :main nil)
