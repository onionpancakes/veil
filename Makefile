.PHONY: test test-clj test-js clean

test: test-clj test-js

test-clj:
	clojure -A:test:run-tests

test-js: test-js/components.js
	npm run test

test-js/components.js: test-js/__cljs__/com/onionpancakes/veil/test_js/components.cljs
	clj -A:test-js/build

clean:
	rm test-js/components.js
	rm -r out