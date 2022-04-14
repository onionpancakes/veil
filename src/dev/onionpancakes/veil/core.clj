(ns dev.onionpancakes.veil.core
  (:refer-clojure :exclude [compile])
  (:require [dev.onionpancakes.veil.compile :as c]))

(defmethod c/to-props-entry ::classes
  [[_ m]]
  [:className `(toggle-classnames m)])

(extend-protocol c/ISkip
  clojure.lang.APersistentVector
  (c/skip? [this]
    (::skip (meta this))))

(def compile*
  c/compile*)

(defmacro compile
  [form]
  (compile* form))
