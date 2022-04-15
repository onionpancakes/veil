(ns dev.onionpancakes.veil.core
  (:require-macros [dev.onionpancakes.veil.core]))

(def toggle-classnames-xf
  (comp (filter val)
        (map (comp name key))))

(defn toggle-classnames
  [m]
  (-> (eduction toggle-classnames-xf m)
      (into-array)
      (.join " ")))
