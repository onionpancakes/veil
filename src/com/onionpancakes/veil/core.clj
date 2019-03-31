(ns com.onionpancakes.veil.core
  (:refer-clojure :exclude [compile])
  (:require [clojure.spec.alpha :as spec]
            [clojure.string]
            [clojure.walk])
  (:import [cljs.tagged_literals JSValue]))

;; Type

(defn component-tag?
  "True if keyword represents a user-defined component."
  [k]
  ;; https://reactjs.org/docs/jsx-in-depth.html#user-defined-components-must-be-capitalized

  ;; Capitalized types or types containing dot access (undocumented)
  ;; are consider user-defined components.
  (let [s (name k)]
    (or (Character/isUpperCase (first s))
        (clojure.string/includes? s "."))))

(defn tag->type
  "Convert a keyword tag to a type suitable for React.createElement."
  [k]
  ;; For user-defined components, perserve the namespace.
  (if (component-tag? k) (symbol k) (name k)))

;; Keyword props

(def id-class-pattern
  #"(?:#[^.]*)|(?:\.[^#.]*)")

(defn keyword->props-map
  [k]
  (let [{ids \# classes \.} (->> (name k)
                                 (re-seq id-class-pattern)
                                 (group-by first))]
    (cond-> nil
      ids     (assoc :id (subs (first ids) 1))
      classes (assoc :className (->> (map #(subs % 1) classes)
                                     (clojure.string/join " "))))))

;; Map props

(defn map->className-form [m]
  `(-> ~m
       (eduction (filter val)
                 (map (comp name key)))
       (into-array)
       (.join " ")))

(defmulti map->props-map-entry key)

(defmethod map->props-map-entry ::classes
  [[_ m]]
  [:className (map->className-form m)])

(defmethod map->props-map-entry :default
  [entry]
  entry)

(defn map->props-map [m]
  (into {} (map map->props-map-entry) m))

;; Props

(defprotocol Props
  (transform-props* [this]))

(extend-protocol Props
  clojure.lang.Keyword
  (transform-props* [this]
    (->> (keyword->props-map this)
         (JSValue.)))
  clojure.lang.PersistentArrayMap
  (transform-props* [this]
    (->> (map->props-map this)
         (JSValue.)))
  clojure.lang.PersistentHashMap
  (transform-props* [this]
    (->> (map->props-map this)
         (JSValue.)))
  Object
  (transform-props* [this] this)
  nil
  (transform-props* [this] nil))

;; Conform spec

(def ^:dynamic *create-element-fn*
  `js/React.createElement)

(def props?
  (some-fn nil? keyword? map?))

(spec/def ::element-form
  (spec/and
   (spec/coll-of any? :kind vector?)
   (complement map-entry?) ; Do not match map entry vectors.
   (comp not ::skip meta)  ; Skip vectors with ^::skip
   (spec/cat ::tag keyword?
             ::props (spec/? props?)
             ::children (spec/* ::form))))

(spec/def ::create-element-form
  (spec/and
   (spec/coll-of any? :kind list?)
   (spec/cat ::fn #{*create-element-fn*}
             ::type any?
             ::props (spec/? any?)
             ::children (spec/* ::form))))

(spec/def ::form
  (spec/or ::element ::element-form
           ::create-element ::create-element-form
           ::map (spec/map-of any? ::form)
           ::coll (spec/coll-of ::form)
           ::other any?))

;; Transform

(def ^:dynamic transform-tag tag->type)

(def ^:dynamic transform-props transform-props*)

(defn ^:dynamic transform-element
  "Transform a conformed element into a conformed create-element-form."
  [m]
  (cond-> m
    true                           (assoc ::fn *create-element-fn*)
    true                           (assoc ::type (transform-tag (::tag m)))
    ;; If props was skipped, props is nil.
    (and (not (contains? m ::props))
         (contains? m ::children)) (assoc ::props nil)
    (contains? m ::props)          (update ::props transform-props)))

;; Compile

(defn conformed-element? [x]
  (and (map-entry? x) (= (first x) ::element)))

(defn process [x]
  (if (conformed-element? x)
    [::create-element (transform-element (second x))]
    x))

(defn walk
  [conform-spec f form]
  (->> (spec/conform conform-spec form)
       (clojure.walk/postwalk f)
       (spec/unform conform-spec)))

(def compile*
  (partial walk ::form process))

(defmacro compile
  "Transform keyword vectors into React.createElement calls."
  [form]
  (compile* form))
