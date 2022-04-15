(ns dev.onionpancakes.veil.example.todo.app
  (:require [dev.onionpancakes.veil.example.todo.components :as c]
            [dev.onionpancakes.veil.core :as v]))

(set! *warn-on-infer* true)

(js/ReactDOM.render (v/compile [::c/TodoApp])
                    (js/document.getElementById "app"))
