(ns dev.onionpancakes.veil.test-js.components
  (:require [dev.onionpancakes.veil.core :as c]
            [cljs.nodejs]))

(set! js/React (cljs.nodejs/require "react"))

(defn ^:export Widget [props]
  (c/compile
   [:div "text"]))

(defn ^:export WidgetNested [props]
  (c/compile
   [:div [:Widget]]))

(defn ^:export WidgetKeyword [props]
  (c/compile
   [:div :#foo.bar.baz "text"]))

(defn ^:export WidgetMapProps [props]
  (c/compile
   [:div {:id         "foo"
          ::c/classes {:yes1 true
                       :no1  false
                       :yes2 (= 0 0)
                       :no2  (= 0 1)}}
    "text"]))

(defn ^:export WidgetFor [props]
  (c/compile
   [:div
    (for [i (range 5)]
      [:p {:key i} (str "text" i)])]))

(defn ^:export WidgetFragment [props]
  (c/compile
   [:js/React.Fragment
    [:div "foo"]
    [:div "bar"]]))
