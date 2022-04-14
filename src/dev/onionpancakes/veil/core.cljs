(ns dev.onionpancakes.veil.core)

(defn toggle-classnames-xf
  (comp (filter val)
        (map (comp name key))))

(defn toggle-classnames
  [m]
  (-> (eduction kv-classname-xf m)
      (into-array)
      (.join " ")))
