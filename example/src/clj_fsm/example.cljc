(ns clj-fsm.example
  "FSM usage example."
  (:require
    [clojure.string :as str]
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm :as fsm]
    [clj-fsm.fsm.state]
    [clj-fsm.fsm.event]))

(defn on-enter [data name]
  (println :on-enter name)
  data)

(defn on-error [data name error]
  (println :on-error name :error error)
  data)

(defn on-leave [data name]
  (println :on-leave name)
  data)

(defn on-state-enter [data name]
  (println :on-state-enter name)
  data)

(defn on-state-leave [data name]
  (println :on-state-leave name)
  data)

(defn on-state-error [data name error]
  (println :on-state-error name :error error)
  data)

(defn on-unverified-state-enter [data name]
  (println :on-unverified-state-enter name)
  (update data :document/name (comp str/upper-case str/trim)))

(defn on-verified-state-enter [data name]
  (println :on-verified-state-enter name)
  (update data :document/name (comp str/lower-case str/trim)))

(defn upper? [data]
  (println :guard :upper?)
  (->> data
       :document/name
       (re-seq #"[a-z]+")
       nil?))

(defn lower? [data]
  (println :guard :lower?)
  (->> data
       :document/name
       (re-seq #"[A-Z]+")
       nil?))


(def d {:document/name   " sImplE nAme    "
        :document/author "John Doe"})


(def f {:fsm/name        :document/fsm
        :fsm/description "Simple document FSM"
        :fsm/enter       [on-enter]
        :fsm/leave       [on-leave]
        :fsm/error       [on-error]
        :fsm/states      {:document/unverified {:state/description "Unverified"
                                                :state/initial?    true
                                                :state/enter       [on-unverified-state-enter]
                                                :state/leave       [on-state-leave]
                                                :state/error       [on-state-error]}
                          :document/verified   {:state/description "Verified"
                                                :state/enter       [on-verified-state-enter]
                                                :state/leave       [on-state-leave]
                                                :state/error       [on-state-error]}
                          :document/published  {:state/description "Published"
                                                :state/enter       [on-state-enter]
                                                :state/leave       [on-state-leave]
                                                :state/error       [on-state-error]}
                          :document/archived   {:state/description "Archived"
                                                :state/enter       [on-state-enter]
                                                :state/leave       [on-state-leave]
                                                :state/error       [on-state-error]
                                                :state/finish?     true}
                          :document/rejected   {:state/description "Rejected"
                                                :state/enter       [on-state-enter]
                                                :state/leave       [on-state-leave]
                                                :state/error       [on-state-error]}}
        :fsm/events      {:document/verify    {:transition/from   [:document/unverified]
                                               :transition/to     :document/verified
                                               :transition/guards [not-empty some? upper?]}
                          :document/reject    {:transition/from [:document/unverified]
                                               :transition/to   :document/rejected}
                          :document/reverify  {:transition/from   [:document/verified]
                                               :transition/to     :document/unverified
                                               :transition/guards [not-empty some? lower?]}
                          :document/publish   {:transition/from [:document/verified]
                                               :transition/to   :document/published}
                          :document/unpublish {:transition/from [:document/published]
                                               :transition/to   :document/verified}
                          :document/archive   {:transition/from [:document/published :document/verified :document/unverified]
                                               :transition/to   :document/archived}}})


(comment

  ;;
  ;; assign fsm to data
  ;;

  (def d1 (fsm/assign d f))

  (identity d1)
  ;; => #:document{:name " sImplE nAme    ", :author "John Doe"}

  (-> d1 fsm/get-fsm :fsm/previous)
  ;; => nil
  (-> d1 fsm/get-fsm :fsm/current)
  ;; => nil


  ;;
  ;; initialize fsm
  ;;

  (def d2 (fsm/init d1))
  ;; :on-enter :document/unverified
  ;; :on-unverified-state-enter :document/unverified

  (identity d2)
  ;; => #:document{:name "SIMPLE NAME", :author "John Doe"}


  (-> d2 fsm/get-fsm :fsm/previous)
  ;; => nil
  (-> d2 fsm/get-fsm :fsm/current)
  ;; => :document/unverified


  ;;
  ;; dispatch :document/verify event
  ;;

  (def d3 (fsm/dispatch d2 :document/verify))
  ;; :on-state-leave :document/unverified
  ;; :guard :upper?
  ;; :on-verified-state-enter :document/verified

  (-> d3 fsm/get-fsm :fsm/previous)
  ;; => :document/unverified
  (-> d3 fsm/get-fsm :fsm/current)
  ;; => :document/verified


  ;;
  ;; dispatch :document/reverify event
  ;;

  (def d4 (fsm/dispatch d3 :document/reverify))
  ;; :on-state-leave :document/verified
  ;; :guard :lower?
  ;; :on-unverified-state-enter :document/unverified

  (-> d4 fsm/get-fsm :fsm/previous)
  ;; => :document/verified
  (-> d4 fsm/get-fsm :fsm/current)
  ;; => :document/published


  (-> d4 fsm/get-fsm fsm/fsm->finish-state)
  ;; => :document/archived


  ;;
  ;; dispatch :document/archive event
  ;;

  (def d5 (fsm/dispatch d4 :document/archive))
  ;; :on-state-leave :document/unverified
  ;; :on-state-enter :document/archived
  ;; :on-leave :document/archived

  (-> d5 fsm/get-fsm :fsm/previous)
  ;; => :document/published
  (-> d5 fsm/get-fsm :fsm/current)
  ;; => :document/archived


  (def d6 (fsm/finish d5))
  ;; => no any changes

  (-> d6 fsm/get-fsm :fsm/previous)
  ;; => :document/published
  (-> d6 fsm/get-fsm :fsm/current)
  ;; => :document/archived


  (def d7 (fsm/init d6))
  ;; => no any changes

  (-> d7 fsm/get-fsm :fsm/previous)
  ;; => :document/published
  (-> d7 fsm/get-fsm :fsm/current)
  ;; => :document/archived
  )
