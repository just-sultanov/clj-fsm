(ns clj-fsm.fsm-test
  (:refer-clojure :exclude [key])
  (:require
    #?(:clj  [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest testing is]])
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm :as sut]))

(def objects
  ['sym '() #{} [] {}])

(def invalid-objects
  ["str" 1 true :keyword ::keyword #?(:clj (Object.) :cljs (js/Object.))])



(deftest ^:unit assign-test
  (let [fsm (gen/generate (s/gen ::sut/fsm))]
    (doseq [obj objects]
      (testing (helpers/format "should be valid - assign `fsm` to `%s` metadata" (type obj))
        (is (= fsm (get (meta (sut/assign obj fsm)) sut/meta-fsm-key)))))


    (doseq [obj invalid-objects]
      (testing (helpers/format "should be thrown an exception - assign `fsm` to a `%s` metadata" (type obj))
        (is (thrown? #?(:clj ClassCastException :cljs js/Error) (sut/assign obj fsm)))))))



(deftest ^:unit unassign-test
  (let [fsm (gen/generate (s/gen ::sut/fsm))]
    (doseq [obj objects
            :let [o (sut/assign obj fsm)]]
      (testing (helpers/format "should be valid - unassign `fsm` from `%s` metadata" (type obj))
        (is (nil? (get (meta (sut/unassign o)) sut/meta-fsm-key)))))))



(deftest ^:unit get-fsm-test
  (let [fsm (gen/generate (s/gen ::sut/fsm))]
    (doseq [obj objects
            :let [o (sut/assign obj fsm)]]
      (testing (helpers/format "should be valid - get `fsm` from `%s` metadata" (type obj))
        (is (nil? (sut/get-fsm obj)))
        (is (= fsm (sut/get-fsm o)))))))



(deftest ^:unit get-fsm-name-test
  (let [fsm      (gen/generate (s/gen ::sut/fsm))
        fsm-name (::sut/name fsm)]
    (doseq [obj objects
            :let [o (sut/assign obj fsm)]]
      (testing (helpers/format "should be valid - get `fsm` name from `%s` metadata" (type obj))
        (is (nil? (sut/get-fsm-name obj)))
        (is (= fsm-name (sut/get-fsm-name o)))))))



(deftest ^:unit get-fsm-desc-test
  (let [fsm      (gen/generate (s/gen ::sut/fsm))
        fsm-desc (::sut/desc fsm)]
    (doseq [obj objects
            :let [o (sut/assign obj fsm)]]
      (testing (helpers/format "should be valid - get `fsm` desc from `%s` metadata" (type obj))
        (is (nil? (sut/get-fsm-desc obj)))
        (is (= fsm-desc (sut/get-fsm-desc o)))))))



(deftest ^:unit get-fsm-states-test
  (let [fsm        (gen/generate (s/gen ::sut/fsm))
        fsm-states (::sut/states fsm)]
    (doseq [obj objects
            :let [o (sut/assign obj fsm)]]
      (testing (helpers/format "should be valid - get `fsm` states from `%s` metadata" (type obj))
        (is (nil? (sut/get-fsm-states obj)))
        (is (= fsm-states (sut/get-fsm-states o)))))))
