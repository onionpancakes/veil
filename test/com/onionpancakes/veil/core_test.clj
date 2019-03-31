(ns com.onionpancakes.veil.core-test
  (:require [com.onionpancakes.veil.core :as c]
            [clojure.test :refer [deftest are]]))

(deftest test-compile*
  (are [x y] (= (c/compile* x) y)
    [:div]            '(js/React.createElement "div")
    [:div nil]        '(js/React.createElement "div" nil)
    [:div nil "foo"]  '(js/React.createElement "div" nil "foo")
    [:div nil
     [:div]]          '(js/React.createElement "div" nil
                         (js/React.createElement "div"))
    [:div nil
     [:div nil
      [:div]]]        '(js/React.createElement "div" nil
                         (js/React.createElement "div" nil
                           (js/React.createElement "div")))
    [:ul nil
     [:li nil "foo"]
     [:li nil "bar"]] '(js/React.createElement "ul" nil
                         (js/React.createElement "li" nil "foo")
                         (js/React.createElement "li" nil "bar"))
    [:ul nil
     '(for [i (range 9)]
        [:li nil i])] '(js/React.createElement "ul" nil
                         (for [i (range 9)]
                           (js/React.createElement "li" nil i)))))

(deftest test-compile*-user-defined-components
  (are [x y] (= (c/compile* x) y)
    [:Foo]            '(js/React.createElement Foo)
    [:foo.bar]        '(js/React.createElement foo.bar)
    [::Foo]           `(js/React.createElement Foo)
    [:foo/Bar]        '(js/React.createElement foo/Bar)))

(deftest test-compile*-no-props
  (are [x y] (= (c/compile* x) y)
    [:div "foo"]  '(js/React.createElement "div" nil "foo")
    [:div 'foo]   '(js/React.createElement "div" nil foo)
    [:div [:div]] '(js/React.createElement "div" nil
                     (js/React.createElement "div"))
    [:div '(foo)] '(js/React.createElement "div" nil (foo))))