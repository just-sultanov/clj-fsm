(ns clj-fsm.fsm.helpers-test
  (:require
    #?(:clj  [clojure.test :refer [deftest testing is]]
       :cljs [cljs.test :refer-macros [deftest testing is]])
    [clj-fsm.fsm.helpers :as sut]))

(deftest ^:unit find-first-test
  (testing "should be returned a first not nilable element"
    (is (= 1 (sut/find-first some? [nil 1 nil 2])))
    (is (= 2 (sut/find-first some? [nil nil 2 nil 3])))
    (is (= 3 (sut/find-first some? [3 nil 1 nil 2 nil])))))



(deftest ^:unit format-test
  (testing "should be returned a correctly formatted a string"
    (is (= "hello, world!" (sut/format "hello, %s!" "world")))
    (is (= "hello, 42!" (sut/format "hello, %d!" 42)))))



(deftest ^:unit fn-name-test
  (testing "should be returned a correct function name"
    #?@(:clj
        (do
          (is (= "clojure.core/identity" (sut/fn-name identity)))
          (is (= "clojure.core/some" (sut/fn-name some))))

        :cljs
        (do
          (is (= "cljs.core/identity" (sut/fn-name identity)))
          (is (= "cljs.core/some" (sut/fn-name some)))))))
