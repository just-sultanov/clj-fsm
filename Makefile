.PHONY: help
.DEFAULT_GOAL := help

SHELL = bash

SCM_URL="https://github.com/just-sultanov/clj-fsm"
TAG_MSG="release a new version"

help: ## Show help
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)


clean: ## Clean
	@echo "=================================================================="
	@echo "Clean..."
	@echo "=================================================================="
	rm -f pom.xml && rm -rf target
	@echo -e "\n"


test-clj: ## Run Clojure tests
	@echo "=================================================================="
	@echo "Run Clojure tests..."
	@echo "=================================================================="
	./bin/kaocha
	@echo -e "\n"


test-cljs: ## Run ClojureScript tests
	@echo "=================================================================="
	@echo "Run ClojureScript tests..."
	@echo "=================================================================="
	clojure -R:common -A:test-cljs --env node --compile-opts '{:optimizations :advanced}'
	@echo -e "\n"


test: test-clj test-cljs ### Run all tests


build: ## Build jar
	@echo "=================================================================="
	@echo "Build..."
	@echo "=================================================================="
	clojure -A:build
	clojure -A:version --pom --scm-url ${SCM_URL}
	mv target/clj-fsm*.jar target/clj-fsm.jar
	@echo -e "\n"


init: ## Init first version
	git tag --annotate --message ${TAG_MSG} v0.1.0


patch: ## Increment patch version
	clojure -A:version patch --tag --message ${TAG_MSG}


minor: ## Increment minor version
	clojure -A:version minor --tag --message ${TAG_MSG}


major: ## Increment major version
	clojure -A:version major --tag --message ${TAG_MSG}


deploy: ## Deploy to clojars
	@echo "=================================================================="
	@echo "Deploy..."
	@echo "=================================================================="
	clojure -A:deploy
	@echo -e "\n"
