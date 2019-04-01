(ns com.onionpancakes.veil.example.todo.app
  (:require [com.onionpancakes.veil.example.todo.components :as c])
  (:require-macros [com.onionpancakes.veil.core :as v]))

(set! *warn-on-infer* true)

(js/ReactDOM.render (v/compile [::c/TodoApp])
                    (js/document.getElementById "app"))
