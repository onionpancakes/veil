(ns dev.onionpancakes.veil.test-core
  (:require [dev.onionpancakes.veil.core :as c]
            [clojure.test :refer [deftest are]]))

(deftest test-compile
  (are [x y] (= (c/compile* x) y)
    [:div]            '(js/React.createElement "div" nil)
    [:div nil]        '(js/React.createElement "div" nil)
    [:div nil "foo"]  '(js/React.createElement "div" nil "foo")
    [:div nil
     [:div]]          '(js/React.createElement "div" nil
                         (js/React.createElement "div" nil))
    [:div nil
     [:div nil
      [:div]]]        '(js/React.createElement "div" nil
                         (js/React.createElement "div" nil
                           (js/React.createElement "div" nil)))
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

(deftest test-compile-user-defined-components
  (are [x y] (= (c/compile* x) y)
    [:Foo]            '(js/React.createElement Foo nil)
    [:foo.bar]        '(js/React.createElement foo.bar nil)
    [::Foo]           `(js/React.createElement Foo nil)
    [:foo/Bar]        '(js/React.createElement foo/Bar nil)))

(deftest test-compile-no-props
  (are [x y] (= (c/compile* x) y)
    [:div "foo"]  '(js/React.createElement "div" nil "foo")
    [:div 'foo]   '(js/React.createElement "div" nil foo)
    [:div [:div]] '(js/React.createElement "div" nil
                     (js/React.createElement "div" nil))
    [:div '(foo)] '(js/React.createElement "div" nil (foo))))

(deftest test-compile-js-props
  ;; Compare props map
  (are [x y] (= (some-> (c/compile* x) (nth 2) (.val)) y)
    [:div "foo"]             nil
    [:div nil "foo"]         nil
    [:div {} "foo"]          {}
    [:div {:id "foo"} "foo"] {:id "foo"}
    [:div :.foo]             {:className "foo"}
    [:div :.foo#bar.baz#buz] {:id "bar" :className "foo baz"}))
