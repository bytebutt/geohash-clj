(ns geohash-clj.core-test
  (:require [clojure.test :refer :all]
            [geohash-clj.core :refer :all]))

; Helpers ---------------------------------------------------------------------

(defn- approx=
  "Check that `a` and `b` are approximately equal."
  [a b]
  (< (Math/abs (- a b)) 0.0001))

(defn- equal-coords
  "Checks if the `expected` vector of coordinates approximately equals the
  `actual` vector of coordinates."
  [expected actual]
  (every? identity
          (map approx= expected actual)))

; Tests -----------------------------------------------------------------------

(deftest test-encode
  (testing "Encodes coordinates correctly."
    (is (= "d" (encode [40.6892 -74.0445] 1)))
    (is (= "dr" (encode [40.6892 -74.0445] 2)))
    (is (= "dr5" (encode [40.6892 -74.0445] 3)))
    (is (= "dr5r" (encode [40.6892 -74.0445] 4)))
    (is (= "dr5r7" (encode [40.6892 -74.0445] 5)))
    (is (= "dr5r7p" (encode [40.6892 -74.0445] 6)))
    (is (= "dr5r7p4" (encode [40.6892 -74.0445] 7)))
    (is (= "dr5r7p4r" (encode [40.6892 -74.0445] 8)))
    (is (= "dr5r7p4ry" (encode [40.6892 -74.0445] 9)))
    (is (= "s00000000" (encode [0.0 0.0] 9)))
    (is (= "zzzzzzzzz" (encode [90.0 180.0] 9)))
    (is (= "000000000" (encode [-90.0 -180.0] 9)))))

(deftest test-decode
  (testing "Decodes geohashes correctly."
    (is (equal-coords [40.6892 -74.0445] (decode "dr5r7p4ry")))
    (is (equal-coords [0.0 0.0] (decode "s00000000")))
    (is (equal-coords [90.0 180.0] (decode "zzzzzzzzz")))
    (is (equal-coords [-90.0 -180.0] (decode "000000000")))))

(deftest test-encode-validation
  (testing "Returns nil for invalid `encode` inputs."
    (is (nil? (encode [200.0 0.0] 6)))
    (is (nil? (encode [0.0 200.0] 6)))
    (is (nil? (encode [200.0 0.0] 0)))
    (is (nil? (encode [0.0 200.0] -1)))
    (is (nil? (encode [0.0 0.0] 0)))
    (is (nil? (encode [0.0 0.0] -1)))))

(deftest test-decode-validation
  (testing "Returns nil for invalid `decode` inputs."
    (is (nil? (decode "")))
    (is (nil? (decode " ")))
    (is (nil? (decode "@")))
    (is (nil? (decode "moose")))))
