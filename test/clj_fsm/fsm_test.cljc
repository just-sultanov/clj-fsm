(ns clj-fsm.fsm-test
  (:require
    #?(:clj  [clojure.test :refer [deftest testing is]]
       :cljs [cljs.test :refer-macros [deftest testing is]])
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm.state]
    [clj-fsm.fsm.event]
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
          (is (thrown? #?(:clj ClassCastException :cljs js/Error) (sut/assign obj fsm))))))

    (testing "should be thrown an error on assigning invalid `fsm` to the given data "
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"The given `fsm` is not satisfied by the specification" (sut/assign {} {}))))))



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
          name (:fsm/name fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-name obj)))
          (is (= name (sut/get-fsm-name o))))))))



(deftest ^:unit get-fsm-description-test
  (testing "getting `fsm` description:"
    (let [fsm  (gen/generate (s/gen ::sut/fsm))
          desc (:fsm/description fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-description obj)))
          (is (= desc (sut/get-fsm-description o))))))))



(deftest ^:unit get-fsm-states-test
  (testing "getting `fsm` states:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          states (:fsm/states fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-states obj)))
          (is (= states (sut/get-fsm-states o))))))))



(deftest ^:unit get-fsm-states-names-test
  (testing "getting `fsm` states names:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          states (keys (:fsm/states fsm))]
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



(deftest ^:unit get-fsm-events-test
  (testing "getting `fsm` events:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          events (:fsm/events fsm)]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-events obj)))
          (is (= events (sut/get-fsm-events o))))))))



(deftest ^:unit get-fsm-events-names-test
  (testing "getting `fsm` events names:"
    (let [fsm    (gen/generate (s/gen ::sut/fsm))
          events (keys (:fsm/events fsm))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-events-names obj)))
          (is (= events (sut/get-fsm-events-names o))))))))



(deftest ^:unit get-fsm-event-test
  (testing "getting `fsm` event:"
    (let [fsm (gen/generate (s/gen ::sut/fsm))]
      (doseq [obj objects
              :let [o     (sut/assign obj fsm)
                    name  (first (sut/get-fsm-events-names o))
                    event (get (sut/get-fsm-events o) name)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm-event obj name)))
          (is (= event (sut/get-fsm-event o name))))))))



(deftest ^:unit init-finish-test
  (testing "initializing `fsm`:"
    (let [d    {:document/name   " sImplE nAme    "
                :document/author "John Doe"}
          f    {:fsm/name        :document/fsm
                :fsm/description "Simple document FSM"
                :fsm/states      {:document/unverified {:state/description "Unverified", :state/initial? true}
                                  :document/verified   {:state/description "Verified"}
                                  :document/published  {:state/description "Published"}
                                  :document/archived   {:state/description "Archived", :state/finish? true}
                                  :document/rejected   {:state/description "Rejected"}}
                :fsm/events      {:document/verify    {:transition/from [:document/unverified]
                                                       :transition/to   [:document/verified]}
                                  :document/reject    {:transition/from [:document/unverified]
                                                       :transition/to   [:document/rejected]}
                                  :document/reverify  {:transition/from [:document/verified]
                                                       :transition/to   [:document/unverified]}
                                  :document/publish   {:transition/from [:document/verified]
                                                       :transition/to   [:document/published]}
                                  :document/unpublish {:transition/from [:document/published]
                                                       :transition/to   [:document/verified]}
                                  :document/archive   {:transition/from [:document/published :document/verified :document/unverified]
                                                       :transition/to   [:document/archived]}}}
          data (sut/assign d f)]

      (testing "should be returned a valid initial state name"
        (is (= :document/unverified (sut/get-fsm-initial-state data))))

      (testing "should be returned a valid finish state name"
        (is (= :document/archived (sut/get-fsm-finish-state data))))

      (testing "should be returned a valid current state name"
        (let [data' (sut/init data)]
          (is (nil? (sut/get-fsm-previous-state data')))
          (is (= :document/unverified (sut/get-fsm-current-state data')))))

      (testing "should be returned a valid previous state name"
        (let [data' (-> data
                        sut/init
                        (sut/apply-state :document/verified))]
          (is (= :document/unverified (sut/get-fsm-previous-state data')))
          (is (= :document/verified (sut/get-fsm-current-state data')))))

      (testing "should be returned valid states after initialize and finalize"
        (let [data' (-> data
                        sut/init
                        sut/finish)]
          (is (= :document/unverified (sut/get-fsm-previous-state data')))
          (is (= :document/archived (sut/get-fsm-current-state data')))))

      (testing "should be thrown an error on initializing data without `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"Not exists assigned `fsm` in the given data" (-> data sut/unassign sut/init))))

      (testing "should be thrown an error on finalizing data without `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"Not exists assigned `fsm` in the given data" (-> data sut/unassign sut/finish))))

      (testing "should be thrown an error on finalizing uninitialized `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"Not initialized `fsm` in the given data" (sut/finish data))))

      (testing "should be thrown an error on applying not existing state"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"Not exists `fsm` state with the given name" (sut/apply-state data ::unknown)))))))
