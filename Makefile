.PHONY: test test-clj test-js update-tests clean

test: test-clj test-js

test-clj:
	clojure -M:test:run-tests

test-js: test-js/components.js
	npm run test

test-js/components.js: test-js/__cljs__/dev/onionpancakes/veil/test_js/components.cljs
	clj -M:test-js/build

# Update tests

update-tests: test-js/components.js
	npm run update-tests

# Clean

clean:
	rm test-js/components.js
	rm -r out