(ns com.onionpancakes.veil.example.todo.components
  (:require-macros [com.onionpancakes.veil.core :as v]))

(defn TodoInput [^js/TodoListProps props]
  (let [[st st!]          (js/React.useState "")
        ^js/Ref input-ref (js/React.createRef)
        handleAdd         (.-handleAdd props)
        handleClear       (.-handleClear props)]
    (v/compile
     [:div :.TodoInput
      [:input {:value      st
               :onChange   (fn [^js/Event e]
                             (st! (.. e -target -value)))
               :onKeyPress (fn [^js/Event e]
                             (when (and (= (.-key e) "Enter") (not= st ""))
                               (handleAdd st)
                               (st! "")))
               :ref        input-ref}]
      [:button {:onClick #(when (not= st "")
                            (handleAdd st)
                            (st! "")
                            (.. input-ref -current focus))}
       "Add Task"]
      [:button {:onClick handleClear} "Clear"]])))

(defn TodoTask [^js/TodoTaskProps props]
  (let [done?      (.-done props)
        text       (.-text props)
        handleDone (.-handleDone props)]
    (v/compile
     [:div {::v/classes {:TodoTask true
                         :done     done?}}
      [:label
       [:input {:type     "checkbox"
                :checked  done?
                :onChange handleDone}]
       [:span text]]])))

(defn TodoTaskList [^js/TodoTaskListProps props]
  (let [tasks    (.-tasks props)
        dispatch (.-dispatch props)]
    (v/compile
     [:div
      (if-not (empty? tasks)
        (for [[idx {:keys [id done? text]}] (map-indexed vector tasks)]
          [:TodoTask {:key        id
                      :done       done?
                      :text       text
                      :handleDone #(dispatch {:action :done :idx idx})}])
        [:i :.TodoApp-placeholder "What needs to be done?"])])))

(defn reducer [state action]
  (case (:action action)
    :add   (conj state {:id    (random-uuid)
                        :done? false
                        :text  (:text action)})
    :done  (update-in state [(:idx action) :done?] not)
    :clear (into [] (remove :done?) state)))

(defn TodoApp [_]
  (let [[st dispatch] (js/React.useReducer reducer [])]
    (v/compile
     [:div :.TodoApp
      [:h1 "Todo List"]
      [:TodoTaskList {:tasks st :dispatch dispatch}]
      [:TodoInput {:handleAdd   #(dispatch {:action :add :text %})
                   :handleClear #(dispatch {:action :clear})}]])))
