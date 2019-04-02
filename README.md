# Veil

ClojureScript library for templating `React.createElement` calls with keyword vectors.

## Motivation

With the release of React hooks, it is now strait forward to write React components in ClojureScript using ordinary functions, forgoing the need of wrapper libraries. However, writing components using direct calls to `React.createElement` is verbose. In JavaScript, JSX provides templating of `React.createElement` calls. Veil is meant to be the JSX equivalent for ClojureScript by providing a macro to template `React.createElement` calls with.

### Why Veil vs other templating libraries?

* Vectors expands directly to `React.createElement` calls. There is no wrapper!
* All expansions to `React.createElement` happen at compile time.
* Support for user-defined components and React components such as `React.Fragment`.
* Simple as heck. Direct pass through to `React`. Source is less than 200 lines.

## Getting started

Veil is a deps project available from its git coordinate. Add the following to your `deps.edn`.

```clojure
{:deps {com.onionpancakes/veil
         {:git/url "https://github.com/onionpancakes/veil.git"
          :sha     "42f78b50c2d032a0352de8706a8f983d5cdbb512"}}}
```

### Requirements

* Clojure 1.10.0 or later.
* `React` must be in scope.

### Running tests

Install npm deps:

```bash
$ npm i
```

To running tests, execute:

```bash
$ make test
```

To update test snapshots, execute:

```bash
$ make update-tests
```

## Usage

Require Veil in your ClojureScript file.

```clojure
(ns myproject.app
  (require-macros [com.onionpancakes.veil.core :as v]))
```

Use the `compile` macro to transform keyword vectors into `React.createElement` calls.

```clojure
(v/compile [:h1 "Hello World!"])

;; expands into

(js/React.createElement "h1" nil "Hello World!")
```

Combine `compile` with `defn` to write functional components.

```clojure
(defn MyComponent [props]
  (v/compile
   [:div
    [:h1 "Hello World!"]
    [:p "Foo bar baz"]]))
```

Then combine functional components with [hooks](https://reactjs.org/docs/hooks-intro.html) as needed.

```clojure
(defn Example [props]
  (let [[cur-count set-count!] (js/React.useState 0)]
    (v/compile
     [:div
      [:p (str "You clicked " cur-count " times")]
      [:button {:onClick #(set-count! (inc cur-count))}
       "Click me"]])))
```

Use `compile` where `React.createElement` would be needed.

```clojure
(js/ReactDOM.render (v/compile [:MyComponent])
                    (js/document.getElementById "app"))
```

### Capitalize tags for user-defined components

Veil follows JSX's [type semantics](https://reactjs.org/docs/jsx-in-depth.html#specifying-the-react-element-type) when determining the type of the element.

When Veil sees a capitalized tag, the keyword is converted to a symbol.

```clojure
(v/compile [:MyComponent])

;; expands into

(js/React.createElement MyComponent)
```

### Namespace is preserved for capitalized tags

For components with capitalized tags, the namespace on the tag is preserved. This allows components to be referenced from other namespaces.

```clojure
(v/compile [:my-ns/MyOtherComponent])

;; expands into

(js/React.createElement my-ns/MyOtherComponent)
```

Use namespace aliases in keywords as normal.

```clojure
(v/compile [::my-ns-alias/MyOtherComponent])
```

Refer to components in the same namespace with global keywords or double colon keywords.

```clojure
(v/compile [:MyOtherComponentHere])

;; or

(v/compile [::MyOtherComponentHere])
```

### Accessing React features

Access React features with dot access.

#### Fragments

```clojure
(v/compile
  [:js/React.Fragment
   [:div "foo"]
   [:div "bar"]])
```

### Props must be maps or keywords

If the second element is a keyword or map, it will be interpreted as props. Otherwise, props will be nil.

```clojure
(v/compile [:div "foo"])

;; expands into

(js/React.createElement "div" nil "foo")
```

### Props expand to JavaScript objects.

React only works with JavaScript objects for props. Veil converts map props into JavaScript objects.

```clojure
(v/compile [:div {:id "foo"}])

;; expands into

(js/React.createElement "div" #js {:id "foo"})
```

### Use camelCase for prop keys

Veil doesn't not convert kabob-case to camelCase. Use camelCase like normal React.

```clojure
(v/compile [:div {:class-name "foo bar"}]) ; No

(v/compile [:div {:className "foo bar"}])  ; Yes
```

### Pass JavaScript objects to keys such as `:style`

For global keys, Veil does not transform their values. React expects JavaScript object for keys such as `:style`.

```clojure
(v/compile [:div {:style {:color "green"}} "foo"])     ; No

(v/compile [:div {:style #js {:color "green"}} "foo"]) ; Yes
```

### Use `::v/classes` to build `:className` at runtime

The key `::v/classes` must have a map as a value. The keys are classes and the values can be any expression. If the expression is truthy, the class is included within `:className`.

```clojure
(v/compile
  [:div {::v/classes {:foo true
                      :bar false
                      :baz (= 0 0)}}])

;; Values are evaluated at runtime!
;; Keys with truthy values are joined into :className.

;; Example above will produce this on render.

<div class="foo baz"></div>
```

### Use keyword props to declare `:id` and `:className`

Keywords props expand into `:id` and `:className` keys in the props object at compile time.

```clojure
(v/compile [:div :#foo.bar.baz])

;; expands into

(js/React.createElement "div" #js {:id "foo"
                                   :className "bar baz"})
````

### Vectors in props are not elements.

Keyword vectors anywhere inside a props will not be transformed into React elements.

```clojure
(v/compile
  [:button {:onClick (fn []
                       ;; Sends a vector, not an element.
                       (my-dispatch! [:action :foo]))}
   "My Button"])
```

### Vectors with `^::v/skip` are not elements.

Use `^::v/skip` to escape keyword vectors which should not be React elements.

```clojure
(v/compile [:div (get my-map ^::v/skip [:not :an :element])])
```

### Vectors in map keys are not elements.

Map keys will not be transformed into React elements.

```clojure
(v/compile
  (let [my-map {[:not :an :element] ; Will not be an element.
                [:div "foo"]}]      ; Will be an element.
    [:div (get my-map ^::v/skip [:not :an :element])]))
```