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

(defn on-initial-state-enter [data name]
  (println :on-initial-state-enter name)
  (update data :document/name (comp str/capitalize str/trim)))



(def d {:document/name   " sImplE nAme    "
        :document/author "John Doe"})


(def f {:fsm/name   :document/fsm
        :fsm/desc   "Simple document FSM"
        :fsm/enter  [on-enter]
        :fsm/leave  [on-leave]
        :fsm/error  [on-error]
        :fsm/states {:document/unverified {:state/description "Unverified"
                                           :state/initial?    true
                                           :state/enter       [on-initial-state-enter]
                                           :state/leave       [on-state-leave]
                                           :state/error       [on-state-error]}
                     :document/verified   {:state/description "Verified"
                                           :state/enter       [on-state-enter]
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
        :fsm/events {:document/verify    {:transition/from [:document/unverified]
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
                                          :transition/to   [:document/archived]}}})


(comment

  ;;
  ;; assign fsm to data
  ;;

  (def d1 (fsm/assign d f))

  (identity d1)
  ;; => #:document{:name " sImplE nAme    ", :author "John Doe"}

  (fsm/get-fsm-previous-state d1)
  ;; => nil
  (fsm/get-fsm-current-state d1)
  ;; => nil


  ;;
  ;; initialize fsm
  ;;

  (def d2 (fsm/init d1))
  ;; :on-enter :document/unverified
  ;; :on-initial-state-enter :document/unverified

  (identity d2)
  ;; => #:document{:name "Simple name", :author "John Doe"}


  (fsm/get-fsm-previous-state d2)
  ;; => nil
  (fsm/get-fsm-current-state d2)
  ;; => :document/unverified



  ;;
  ;; apply :document/verified state
  ;;

  (def d3 (fsm/apply-state d2 :document/verified))
  ;; :on-state-leave :document/unverified
  ;; :on-state-enter :document/verified

  (fsm/get-fsm-previous-state d3)
  ;; => :document/unverified
  (fsm/get-fsm-current-state d3)
  ;; => :document/verified



  ;;
  ;; apply :document/published state
  ;;

  (def d4 (fsm/apply-state d3 :document/published))
  ;; :on-state-leave :document/verified
  ;; :on-state-enter :document/published

  (fsm/get-fsm-previous-state d4)
  ;; => :document/verified
  (fsm/get-fsm-current-state d4)
  ;; => :document/published


  (fsm/get-fsm-finish-state d4)
  ;; => :document/archived


  ;;
  ;; apply :document/archived (finish state) directly
  ;;

  (def d5 (fsm/apply-state d4 :document/archived))
  ;; :on-state-leave :document/published
  ;; :on-state-enter :document/archived
  ;; :on-leave :document/archived

  (fsm/get-fsm-previous-state d5)
  ;; => :document/published
  (fsm/get-fsm-current-state d5)
  ;; => :document/archived



  (def d6 (fsm/finish d5))
  ;; => no any changes

  (fsm/get-fsm-previous-state d6)
  ;; => :document/published
  (fsm/get-fsm-current-state d6)
  ;; => :document/archived



  (def d7 (fsm/init d6))
  ;; => no any changes

  (fsm/get-fsm-previous-state d7)
  ;; => :document/published
  (fsm/get-fsm-current-state d7)
  ;; => :document/archived
  )
