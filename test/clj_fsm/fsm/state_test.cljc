(ns clj-fsm.fsm.state-test
  (:require
    #?(:clj  [clojure.test :refer [deftest testing is]]
       :cljs [cljs.test :refer-macros [deftest testing is]])
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.state :as sut]))

(defn- broke [states]
  (if-not (sut/states-valid? states)
    states
    (let [n (rand-int (count states))
          k (nth (keys states) n)]
      (update states k update :state/initial? not))))


(defn- randomize [states]
  (into {} (shuffle (vec states))))



(deftest ^:unit state-specification-test
  (testing "samples generation by `:state/map` specification"
    (testing "should be returned valid generated samples"
      (is (every? #(s/valid? :state/map %) (gen/sample (s/gen :state/map)))))))



(deftest ^:unit states-test
  (testing "samples generation and conforming by `:states/map` specification"
    (let [states         (gen/sample (s/gen :states/map))
          valid-states   (map randomize states)
          invalid-states (map randomize (map broke states))]

      (testing "should be returned valid generated samples"
        (is (every? sut/states-valid? states))
        (is (every? sut/states-valid? valid-states))
        (is (every? #(s/valid? :states/map %) states))
        (is (every? #(s/valid? :states/map %) valid-states)))

      (testing "should be declined all invalid states"
        (is (not-every? sut/states-valid? invalid-states))
        (is (not-every? #(s/valid? :states/map %) invalid-states))))))



(deftest ^:unit states-validation-test
  (testing "states validation:"
    (testing "should be returned `false` with a missing initial and finish states"
      (let [v {:document/unverified {:state/description "Unverified"}
               :document/verified   {:state/description "Verified"}
               :document/published  {:state/description "Published"}
               :document/archived   {:state/description "Archived"}
               :document/rejected   {:state/description "Rejected"}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? :states/map v)))))

    (testing "should be returned `false` with a missing initial state"
      (let [v1 {:document/unverified {:state/description "Unverified"}
                :document/verified   {:state/description "Verified"}
                :document/published  {:state/description "Published"}
                :document/archived   {:state/description "Archived"}
                :document/rejected   {:state/description "Rejected", :state/finish? true}}
            v2 {:document/unverified {:state/description "Unverified"}
                :document/verified   {:state/description "Verified"}
                :document/published  {:state/description "Published"}
                :document/archived   {:state/description "Archived", :state/initial? false}
                :document/rejected   {:state/description "Rejected", :state/finish? true}}]
        (is (false? (sut/states-valid? v1)))
        (is (false? (sut/states-valid? v2)))
        (is (false? (s/valid? :states/map v1)))
        (is (false? (s/valid? :states/map v2)))))

    (testing "should be returned `false` with more that one initial states"
      (let [v {:document/unverified {:state/description "Unverified", :state/initial? true}
               :document/verified   {:state/description "Verified"}
               :document/published  {:state/description "Published"}
               :document/archived   {:state/description "Archived", :state/initial? true}
               :document/rejected   {:state/description "Rejected", :state/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? :states/map v)))))

    (testing "should be returned `false` with a missing finish state"
      (let [v1 {:document/unverified {:state/description "Unverified", :state/initial? true}
                :document/verified   {:state/description "Verified"}
                :document/published  {:state/description "Published"}
                :document/archived   {:state/description "Archived"}
                :document/rejected   {:state/description "Rejected"}}
            v2 {:document/unverified {:state/description "Unverified", :state/initial? true}
                :document/verified   {:state/description "Verified"}
                :document/published  {:state/description "Published"}
                :document/archived   {:state/description "Archived"}
                :document/rejected   {:state/description "Rejected", :state/finish? false}}]
        (is (false? (sut/states-valid? v1)))
        (is (false? (sut/states-valid? v2)))
        (is (false? (s/valid? :states/map v1)))
        (is (false? (s/valid? :states/map v2)))))

    (testing "should be returned `false` with more that one finish states"
      (let [v {:document/unverified {:state/description "Unverified", :state/finish? true}
               :document/verified   {:state/description "Verified"}
               :document/published  {:state/description "Published"}
               :document/archived   {:state/description "Archived", :state/initial? true}
               :document/rejected   {:state/description "Rejected", :state/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? :states/map v)))))

    (testing "should be returned `false` when a state defined as `initial` and `finish`"
      (let [v {:document/unverified {:state/description "Unverified"}
               :document/verified   {:state/description "Verified"}
               :document/published  {:state/description "Published"}
               :document/archived   {:state/description "Archived"}
               :document/rejected   {:state/description "Rejected", :state/initial? true, :state/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? :states/map v)))))))
