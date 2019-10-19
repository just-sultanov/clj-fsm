(ns clj-fsm.fsm-test
  (:require
    #?(:clj  [clojure.test :refer [deftest testing is]]
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
  (testing "assigning `fsm`:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects]
        (testing (helpers/format "to `%s` metadata" (type obj))
          (is (= fsm (get (meta (sut/assign obj fsm)) sut/meta-fsm-key)))))


      (doseq [obj invalid-objects]
        (testing (helpers/format "throws an exception for a `%s` metadata" (type obj))
          (is (thrown? #?(:clj ClassCastException :cljs js/Error) (sut/assign obj fsm))))))))



(deftest ^:unit unassign-test
  (testing "unassigning `fsm`:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (get (meta (sut/unassign o)) sut/meta-fsm-key))))))))



(deftest ^:unit get-fsm-test
  (testing "getting `fsm`:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm obj)))
          (is (= fsm (sut/get-fsm o))))))))



(deftest ^:unit get-fsm-name-test
  (testing "getting `fsm` name:"
    (let [fsm  (gen/generate (s/gen ::sut/fsm))
          name (::sut/name fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-name obj)))
          (is (= name (sut/get-fsm-name o))))))))



(deftest ^:unit get-fsm-desc-test
  (testing "getting `fsm` desc:"
    (let [fsm  (gen/generate (s/gen ::sut/fsm))
          desc (::sut/desc fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-desc obj)))
          (is (= desc (sut/get-fsm-desc o))))))))



(deftest ^:unit get-fsm-states-test
  (testing "getting `fsm` states:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          states (::sut/states fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-states obj)))
          (is (= states (sut/get-fsm-states o))))))))



(deftest ^:unit get-fsm-states-names-test
  (testing "getting `fsm` states names:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          states (keys (::sut/states fsm))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-states-names obj)))
          (is (= states (sut/get-fsm-states-names o))))))))



(deftest ^:unit get-fsm-initial-state-test
  (testing "getting `fsm` initial state:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s`metadata" (type obj))
          (is (nil? (sut/get-fsm-initial-state obj)))
          (is (some? (sut/get-fsm-initial-state o))))))))



(deftest ^:unit get-fsm-state-test
  (testing "getting `fsm` state:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects
              :let [o     (sut/assign obj fsm)
                    name  (first (sut/get-fsm-states-names o))
                    state (get (sut/get-fsm-states o) name)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-state obj name)))
          (is (= state (sut/get-fsm-state o name))))))))
