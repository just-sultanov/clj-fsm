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
      (update states k update ::sut/initial? not))))


(defn- randomize [states]
  (into {} (shuffle (vec states))))



(deftest ^:unit state-specification-test
  (testing "samples generation by `::sut/state` specification"
    (testing "should be returned valid generated samples"
      (is (every? #(s/valid? ::sut/state %) (gen/sample (s/gen ::sut/state)))))))



(deftest ^:unit states-test
  (testing "samples generation and conforming by `::sut/states` specification"
    (let [states         (gen/sample (s/gen ::sut/states))
          valid-states   (map randomize states)
          invalid-states (map randomize (map broke states))]

      (testing "should be returned valid generated samples"
        (is (every? sut/states-valid? states))
        (is (every? sut/states-valid? valid-states))
        (is (every? #(s/valid? ::sut/states %) states))
        (is (every? #(s/valid? ::sut/states %) valid-states)))

      (testing "should be declined all invalid states"
        (is (not-every? sut/states-valid? invalid-states))
        (is (not-every? #(s/valid? ::sut/states %) invalid-states))))))


(deftest ^:unit states-validation-test
  (testing "states validation:"
    (testing "should be returned `false` with a missing initial and finish states"
      (let [v {:document/unverified {::sut/desc "Unverified"}
               :document/verified   {::sut/desc "Verified"}
               :document/published  {::sut/desc "Published"}
               :document/archived   {::sut/desc "Archived"}
               :document/rejected   {::sut/desc "Rejected"}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? ::sut/states v)))))

    (testing "should be returned `false` with a missing initial state"
      (let [v1 {:document/unverified {::sut/desc "Unverified"}
                :document/verified   {::sut/desc "Verified"}
                :document/published  {::sut/desc "Published"}
                :document/archived   {::sut/desc "Archived"}
                :document/rejected   {::sut/desc "Rejected", ::sut/finish? true}}
            v2 {:document/unverified {:sut/:desc "Unverified"}
                :document/verified   {:sut/:desc "Verified"}
                :document/published  {:sut/:desc "Published"}
                :document/archived   {:sut/:desc "Archived", ::sut/initial? false}
                :document/rejected   {:sut/:desc "Rejected", ::sut/finish? true}}]
        (is (false? (sut/states-valid? v1)))
        (is (false? (sut/states-valid? v2)))
        (is (false? (s/valid? ::sut/states v1)))
        (is (false? (s/valid? ::sut/states v2)))))

    (testing "should be returned `false` with more that one initial states"
      (let [v {:document/unverified {:sut/:desc "Unverified", ::sut/initial? true}
               :document/verified   {:sut/:desc "Verified"}
               :document/published  {:sut/:desc "Published"}
               :document/archived   {:sut/:desc "Archived", ::sut/initial? true}
               :document/rejected   {:sut/:desc "Rejected", ::sut/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? ::sut/states v)))))

    (testing "should be returned `false` with a missing finish state"
      (let [v1 {:document/unverified {::sut/desc "Unverified", ::sut/initial? true}
                :document/verified   {::sut/desc "Verified"}
                :document/published  {::sut/desc "Published"}
                :document/archived   {::sut/desc "Archived"}
                :document/rejected   {::sut/desc "Rejected"}}
            v2 {:document/unverified {::sut/desc "Unverified", ::sut/initial? true}
                :document/verified   {::sut/desc "Verified"}
                :document/published  {::sut/desc "Published"}
                :document/archived   {::sut/desc "Archived"}
                :document/rejected   {::sut/desc "Rejected", ::sut/finish? false}}]
        (is (false? (sut/states-valid? v1)))
        (is (false? (sut/states-valid? v2)))
        (is (false? (s/valid? ::sut/states v1)))
        (is (false? (s/valid? ::sut/states v2)))))

    (testing "should be returned `false` with more that one finish states"
      (let [v {:document/unverified {:sut/:desc "Unverified", ::sut/finish? true}
               :document/verified   {:sut/:desc "Verified"}
               :document/published  {:sut/:desc "Published"}
               :document/archived   {:sut/:desc "Archived", ::sut/initial? true}
               :document/rejected   {:sut/:desc "Rejected", ::sut/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? ::sut/states v)))))

    (testing "should be returned `false` when a state defined as `initial` and `finish`"
      (let [v {:document/unverified {:sut/:desc "Unverified"}
               :document/verified   {:sut/:desc "Verified"}
               :document/published  {:sut/:desc "Published"}
               :document/archived   {:sut/:desc "Archived"}
               :document/rejected   {:sut/:desc "Rejected", ::sut/initial? true, ::sut/finish? true}}]
        (is (false? (sut/states-valid? v)))
        (is (false? (s/valid? ::sut/states v)))))))
