(ns dev.onionpancakes.veil.test-js.components
  (:require [dev.onionpancakes.veil.core :as v]
            [cljs.nodejs]))

(set! js/React (cljs.nodejs/require "react"))

(defn ^:export Widget [props]
  (v/compile
   [:div "text"]))

(defn ^:export WidgetNested [props]
  (v/compile
   [:div [:Widget]]))

(defn ^:export WidgetKeyword [props]
  (v/compile
   [:div :#foo.bar.baz "text"]))

(defn ^:export WidgetMapProps [props]
  (v/compile
   [:div {:id         "foo"
          ::v/classes {:yes1 true
                       :no1  false
                       :yes2 (= 0 0)
                       :no2  (= 0 1)}}
    "text"]))

(defn ^:export WidgetFor [props]
  (v/compile
   [:div
    (for [i (range 5)]
      [:p {:key i} (str "text" i)])]))

(defn ^:export WidgetFragment [props]
  (v/compile
   [:js/React.Fragment
    [:div "foo"]
    [:div "bar"]]))
