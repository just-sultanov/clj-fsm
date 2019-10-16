(ns clj-fsm.core-test
  (:require
    #?(:clj  [clojure.test :as t]
       :cljs [cljs.test :as t])
    [clj-fsm.core :as sut]))

(t/deftest ^:unit square-test
  (t/is (= 4 (sut/square 2))))
