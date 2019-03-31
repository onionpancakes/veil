(ns com.onionpancakes.veil.test-js.components
  (:require-macros [com.onionpancakes.veil.core :as c])
  (:require [cljs.nodejs]))

(set! js/React (cljs.nodejs/require "react"))

(defn ^:export Widget [props]
  (c/compile
   [:div nil "foo"]))

