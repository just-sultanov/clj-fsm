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
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects]
        (testing (helpers/format "to `%s` metadata" (type obj))
          (is (= fsm (get (meta (sut/assign obj fsm)) sut/meta-fsm-key)))))

      (doseq [obj invalid-objects]
        (testing (helpers/format "throws an exception for a `%s` metadata" (type obj))
          (is (thrown? #?(:clj ClassCastException :cljs js/Error)
                       (sut/assign obj fsm))))))

    (testing "should be thrown an error on assigning invalid `fsm` to the given data "
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"The given `fsm` is not satisfied by the specification"
                            (sut/assign {} {}))))))



(deftest ^:unit unassign-test
  (testing "unassigning `fsm`:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (some? (sut/get-fsm o)))
          (is (nil? (get (meta (sut/unassign o)) sut/meta-fsm-key))))))))



(deftest ^:unit get-fsm-test
  (testing "getting `fsm`:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (nil? (sut/get-fsm obj)))
          (is (= fsm (sut/get-fsm o))))))))


(deftest ^:unit fsm->state-test
  (testing "getting `fsm` event:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o     (sut/assign obj fsm)
                    f     (sut/get-fsm o)
                    name  (last (keys (:fsm/states f)))
                    state (get-in f [:fsm/states name])]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (= state (sut/fsm->state f name))))))))



(deftest ^:unit fsm->initial-state-test
  (testing "getting `fsm` initial state:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)
                    f (sut/get-fsm o)]]
        (testing (helpers/format "from `%s`metadata" (type obj))
          (is (some? (sut/fsm->initial-state f))))))))



(deftest ^:unit fsm->finish-state-test
  (testing "getting `fsm` finish state:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o (sut/assign obj fsm)
                    f (sut/get-fsm o)]]
        (testing (helpers/format "from `%s`metadata" (type obj))
          (is (some? (sut/fsm->finish-state f))))))))



(deftest ^:unit fsm->event-test
  (testing "getting `fsm` event:"
    (let [fsm (gen/generate (s/gen :fsm/map))]
      (doseq [obj objects
              :let [o     (sut/assign obj fsm)
                    f     (sut/get-fsm o)
                    name  (last (keys (:fsm/events f)))
                    event (get-in f [:fsm/events name])]]
        (testing (helpers/format "from `%s` metadata" (type obj))
          (is (= event (sut/fsm->event f name))))))))



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

      (testing "should be returned a valid initialized status"
        (is (false? (-> data sut/get-fsm sut/fsm-initialized?)))
        (is (true? (-> data sut/init sut/get-fsm sut/fsm-initialized?))))

      (testing "should be returned a valid finalized status"
        (is (false? (-> data sut/get-fsm sut/fsm-finalized?)))
        (is (false? (-> data sut/init sut/get-fsm sut/fsm-finalized?)))
        (is (true? (-> data sut/init sut/finish sut/get-fsm sut/fsm-finalized?))))

      (testing "should be returned a valid initial state name"
        (is (= :document/unverified (-> data sut/get-fsm sut/fsm->initial-state))))

      (testing "should be returned a valid finish state name"
        (is (= :document/archived (sut/fsm->finish-state (sut/get-fsm data)))))

      (testing "should be returned a valid current state name"
        (let [data' (sut/init data)]
          (is (nil? (:fsm/previous (sut/get-fsm data'))))
          (is (= :document/unverified (:fsm/current (sut/get-fsm data'))))))

      (testing "should be returned a valid previous state name"
        (let [data' (-> data sut/init (sut/apply-state :document/verified))]
          (is (= :document/unverified (:fsm/previous (sut/get-fsm data'))))
          (is (= :document/verified (:fsm/current (sut/get-fsm data'))))))

      (testing "should be returned valid states after initialize and finalize"
        (let [data' (-> data sut/init sut/finish)]
          (is (= :document/unverified (:fsm/previous (sut/get-fsm data'))))
          (is (= :document/archived (:fsm/current (sut/get-fsm data'))))))

      (testing "should be thrown an error on initializing data without `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                              #"Not exists assigned `fsm` in the given data"
                              (-> data sut/unassign sut/init))))

      (testing "should be thrown an error on finalizing data without `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                              #"Not exists assigned `fsm` in the given data"
                              (-> data sut/unassign sut/finish))))

      (testing "should be thrown an error on finalizing uninitialized `fsm`"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                              #"Not initialized `fsm` in the given data"
                              (sut/finish data))))

      (testing "should be thrown an error on applying not existing state"
        (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                              #"Not exists `fsm` state with the given name"
                              (sut/apply-state data ::unknown)))))))
