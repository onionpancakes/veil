# Veil

ClojureScript library for transforming vectors into `React.createElement` calls.

## Getting started

Veil is a deps project available from its git coordinate. Add the following to your `deps.edn`.

```clojure
{:deps {com.onionpancakes/veil
         {:git/url "https://github.com/onionpancakes/veil.git"
          :sha     "71a3399ed90d1ba7686a05ecd4a6ba934d0947f7"}}}
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

Use `compile` where `React.createElement` would be needed.

```clojure
(js/ReactDOM.render (v/compile [:MyComponent])
                    (js/document.getElementById "app"))
```

### Capitalize tags for user-defined components

Veil follows JSX's semantics[<sup>[Link]</sup>](https://reactjs.org/docs/jsx-in-depth.html#specifying-the-react-element-type) when determining the type of the element.

When Veil sees a capitalized tag, the keyword is converted to a symbol.

```clojure
(v/compile [:MyComponent])

;; expands into

(js/React.createElement MyComponent)
```

### Namespace is preserved for capitalized tags

For components with capitalized tags, the namespace on the tag is preserved. This allows components to be reference from other namespaces.

```clojure
(v/compile [:other.ns/MyOtherComponent])

;; expands into

(js/React.createElement other.ns/MyOtherComponent)
```

### Accessing React features

Access React features with capitalized components, the `js` namespace, and dot access.

#### Fragments[<sup>[Link]</sup>](https://reactjs.org/docs/fragments.html)

```clojure
(v/compile
  [:js/React.Fragment
   [:div "foo"]
   [:div "bar"]])
```

#### Contexts[<sup>[Link]</sup>](https://reactjs.org/docs/context.html)

```clojure
(v/compile
  [:js/MyContext.Provider {:value some-value}
   [:MyComponent]])
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

### Use `::v/classes` to manage `className` at runtime

The key `::v/classes` must have a map as a value. The keys in the map are interpreted as classes and the values can be any expression. If the expression is truthy, the class is included within `className`.

```clojure
(v/compile
  [:div {::v/classes {:foo (= 0 0) ; foo will be in className
                      :bar (= 0 1) ; bar will not be in className
                      :buz my-truthy-value}}])
```

### Use keyword props to declare `id` and `className`

Keywords props expand into `id` and `className` keys in the props object at compile time.

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

### Vectors with `::v/skip` are not elements.

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