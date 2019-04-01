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

* Clojure 1.10.0.
* `React` in scope.
* `npm` to run tests.

### Running tests

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
(require-macros '[com.onionpancakes.veil.core :as v])
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
(js/ReactDOM.render (h/compile [:MyComponent])
                    (js/document.getElementById "app"))
```
