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



(deftest ^:unit state-test
  (testing "samples generation by `::sut/state` specification"
    (is (every? #(s/valid? ::sut/state %) (gen/sample (s/gen ::sut/state))))))



(deftest ^:unit states-test
  (testing "samples generation and conforming by `::sut/states` specification"
    (let [states         (gen/sample (s/gen ::sut/states))
          valid-states   (map randomize states)
          invalid-states (map randomize (map broke states))]

      (is (every? sut/states-valid? states))
      (is (every? sut/states-valid? valid-states))
      (is (every? #(s/valid? ::sut/states %) states))
      (is (every? #(s/valid? ::sut/states %) valid-states))

      (is (not-every? sut/states-valid? invalid-states))
      (is (not-every? #(s/valid? ::sut/states %) invalid-states)))))
