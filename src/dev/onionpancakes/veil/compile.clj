(ns dev.onionpancakes.veil.compile
  (:refer-clojure :exclude [compile])
  (:require [clojure.spec.alpha :as spec]
            [clojure.string]
            [clojure.walk])
  (:import [cljs.tagged_literals JSValue]))

;; Conform spec

(def props?
  (some-fn map? keyword? nil?))

(spec/def ::element
  (spec/and
   (spec/coll-of any? :kind vector?)
   (complement map-entry?)
   (comp not ::skip meta)
   (spec/cat ::tag keyword?
             ::props (spec/? props?)
             ::children (spec/* ::form))))

(spec/def ::create-element
  (spec/and
   (spec/coll-of any? :kind list?)
   (spec/cat ::fn any?
             ::type any?
             ::props (spec/? any?)
             ::children (spec/* ::form))))

(spec/def ::form
  (spec/or ::element ::element
           ::create-element ::create-element
           ::map (spec/map-of any? ::form)
           ::coll (spec/coll-of ::form)
           ::other any?))

;; Tag

(defn component-tag?
  "True if keyword represents a user-defined component."
  [k]
  ;; https://reactjs.org/docs/jsx-in-depth.html#user-defined-components-must-be-capitalized

  ;; Capitalized types or types containing dot access (undocumented)
  ;; are consider user-defined components.
  (let [s (name k)]
    (or (Character/isUpperCase (first s))
        (clojure.string/includes? s "."))))

(defn tag-to-type
  "Convert a keyword tag to a type suitable for React.createElement."
  [k]
  ;; For user-defined components, perserve the namespace.
  (if (component-tag? k) (symbol k) (name k)))

;; Props

(defmulti to-props-entry key)

(defmethod to-props-entry :default
  [entry]
  entry)

(defn to-props-map
  [m]
  (into {} (map to-props-entry) m))

(def re-keyword-props
  #"(?:#[^.]*)|(?:\.[^#.]*)")

(defn to-props-keyword
  [k]
  (let [{ids \# classes \.} (->> (name k)
                                 (re-seq re-keyword-props)
                                 (group-by first))]
    (cond-> nil
      ids     (assoc :id (subs (first ids) 1))
      classes (assoc :className (->> (map #(subs % 1) classes)
                                     (clojure.string/join " "))))))

(defprotocol IProps
  (to-props [this]))

(extend-protocol IProps
  clojure.lang.APersistentMap
  (to-props [this]
    (JSValue. (to-props-map this)))
  clojure.lang.Keyword
  (to-props [this]
    (JSValue. (to-props-keyword this)))
  nil
  (to-props [_] nil))

;; Create element

(defn to-create-element
  [{::keys [tag props children]}]
  {::fn       `js/React.createElement
   ::type     (tag-to-type tag)
   ::props    (to-props props)
   ::children children})

(defn element-to-create-element
  [conform-entry]
  (if (and (vector? conform-entry)
           (= (first conform-entry) ::element))
    [::create-element (to-create-element (second conform-entry))]
    conform-entry))

(defn compile*
  [form]
  (->> (spec/conform ::form form)
       (clojure.walk/postwalk element-to-create-element)
       (spec/unform ::form)))

(defmacro compile
  [form]
  (compile* form))
