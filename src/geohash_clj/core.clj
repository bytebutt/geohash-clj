(ns geohash-clj.core)

; Constants -------------------------------------------------------------------

(def ^:private ^:static BASE32-MAP (let [base32 "0123456789bcdefghjkmnpqrstuvwxyz"
                                         base10 (range 32)]
                                     (into {} (map vector base32 base10))))

(def ^:private ^:static BASE10-MAP (let [base10 (range 32)
                                         base32 "0123456789bcdefghjkmnpqrstuvwxyz"]
                                     (into {} (map vector base10 base32))))

(def ^:private ^:static LAT_MIN -90.0)
(def ^:private ^:static LAT_MAX 90.0)
(def ^:private ^:static LNG_MIN -180.0)
(def ^:private ^:static LNG_MAX 180.0)

; Helpers ---------------------------------------------------------------------

(defn- base32->base10
  "Returns a base10 integer for the given base32 `char`."
  [char]
  (get BASE32-MAP char))

(defn- base10->base32
  "Returns a base32 character for the given base10 integer `n`."
  [n]
  (get BASE10-MAP n))

(defn- char->bits
  "Returns a sequence of bits representing the value of a geohash
  character `char`."
  [char]
  (if-let [value (base32->base10 char)]
    (loop [n value
           bits 5
           binary []]
      (if (zero? bits)
        binary
        (recur (unsigned-bit-shift-right n 1)
               (dec bits)
               (cons (bit-and n 1) binary))))))

(defn- bits->char
  "Converts the given vector of `bits` into a base32 character."
  [bits]
  (let [value (Integer/parseInt (apply str bits) 2)]
    (base10->base32 value)))

(defn- geohash->bits
  "Returns a sequence of bits representing the given `geohash` string. Returns
  nil if the geohash is not entirely composed of valid characters."
  [geohash]
  (let [bits (flatten (map char->bits geohash))]
    (when (and (seq bits) (not-any? nil? bits))
      bits)))

(defn- bits->geohash
  "Converts the given vector of `bits` into a geohash string."
  [bits]
  (->> (partition 5 bits)
       (map bits->char)
       (apply str)))

(defn- split-bits
  "Splits the given vector of `bits` into separate latitiude bits
  and longitude bits."
  [bits]
  (loop [[lng lat & xs] bits
         lat-bits []
         lng-bits []]
    (cond
      ; No bits remaining.
      (and (nil? lat) (nil? lng))
      [lat-bits lng-bits]
      ; Extra longitude bit.
      (nil? lat)
      [lat-bits (conj lng-bits lng)]
      ; Separate latitude and longitude bits.
      :else
      (recur xs (conj lat-bits lat) (conj lng-bits lng)))))

(defn- merge-bits
  "Combines the given vectors of `lat-bits` and `lng-bits` into a single
  vector of bits."
  [lat-bits lng-bits]
  (loop [[lat & lats] lat-bits
         [lng & lngs] lng-bits
         result []]
    (cond
      ; Latitude and longitude have same number of bits.
      (and (nil? lat) (nil? lng))
      result
      ; Extra longitude bit.
      (nil? lat)
      (conj result lng)
      ; Interleave longitude and latitude bits.
      :else
      (recur lats lngs (conj result lng lat)))))

(defn- bits->coordinate
  "Converts the given vector of `bits` into a coordinate bounded by the given
  `min` and `max` coordinate values."
  [bits min max]
  (loop [[x & xs] bits
         left min
         right max]
    (let [mid (/ (+ left right) 2)]
      (cond
        ; All bits processed, so take the midpoint.
        (nil? x)
        mid
        ; Low bit, so take the left interval.
        (zero? x)
        (recur xs left mid)
        ; High bit, so take the right interval.
        :else
        (recur xs mid right)))))

(defn- coordinate->bits
  "Converts the given `coordinate` with `min` and `max` coordinate values
  into a vector of bits of length `num-bits`."
  [coordinate min max num-bits]
  (loop [left min
         right max
         bits num-bits
         result []]
    (let [mid (/ (+ left right) 2)]
      (cond
        ; No bits remaining so return result.
        (zero? bits)
        result
        ; Coordinate is less than midpoint, so 0 bit.
        (< coordinate mid)
        (recur left mid (dec bits) (conj result 0))
        ; Coordinate is greater than midpoint, so 1 bit.
        :else
        (recur mid right (dec bits) (conj result 1))))))

(defn- validate-encode
  "Returns true if the given arguments are valid arguments for
  geohash encoding, false otherwise."
  [latitude longitude precision]
  (and (<= LAT_MIN latitude) (<= latitude LAT_MAX)
       (<= LNG_MIN longitude) (<= longitude LNG_MAX)
       (pos? precision)))

; Public API ------------------------------------------------------------------

(defn encode
  "Encodes the given vector of `latitude` and `longitude` to a geohash of the
  given `precision`. Returns nil if the coordinates can't be encoded to the
  given `precision`."
  [[latitude longitude] precision]
  (when (validate-encode latitude longitude precision)
    (let [lat-size (quot (* 5 precision) 2)
          lng-size (if (even? precision) lat-size (inc lat-size))
          lat-bits (coordinate->bits latitude LAT_MIN LAT_MAX lat-size)
          lng-bits (coordinate->bits longitude LNG_MIN LNG_MAX lng-size)
          bits (merge-bits lat-bits lng-bits)]
      (bits->geohash bits))))

(defn decode
  "Decodes the given `geohash` into a vector of latitude and longitude. Returns
  nil if the given `geohash` can't be decoded."
  [geohash]
  (if-let [binary (geohash->bits geohash)]
    (let [[lat-bits lng-bits] (split-bits binary)
          lat (bits->coordinate lat-bits LAT_MIN LAT_MAX)
          lng (bits->coordinate lng-bits LNG_MIN LNG_MAX)]
      [lat lng])))
