(ns clj-fsm.fsm.helpers-test
  (:require
    #?(:clj  [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest testing is]])
    [clj-fsm.fsm.helpers :as sut]))

(deftest ^:unit find-first-test
  (testing "should be valid - find first not nilable element"
    (is (= 1 (sut/find-first some? [nil 1 nil 2])))
    (is (= 2 (sut/find-first some? [nil nil 2 nil 3])))
    (is (= 3 (sut/find-first some? [3 nil 1 nil 2 nil])))))



(deftest ^:unit format-test
  (testing "should be valid - format a string"
    (is (= "hello, world!" (sut/format "hello, %s!" "world")))
    (is (= "hello, 42!" (sut/format "hello, %d!" 42)))))
