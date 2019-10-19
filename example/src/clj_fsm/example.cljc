(ns clj-fsm.example
  "FSM usage example."
  (:require
    [clojure.string :as str]
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm :as fsm]
    [clj-fsm.fsm.state :as fsm.state]))

(def document-fsm
  {::fsm/name   :acme/document-fsm
   ::fsm/desc   "Simple document FSM"
   ::fsm/state  :document/unverified
   ::fsm/states {:document/unverified {::fsm.state/desc "Unverified", ::fsm.state/initial? true}
                 :document/verified   {::fsm.state/desc "Verified"}
                 :document/published  {::fsm.state/desc "Published"}
                 :document/archived   {::fsm.state/desc "Archived"}
                 :document/rejected   {::fsm.state/desc "Rejected"}}
   ::fsm/events {:document/verify    {:transition/from [:document/unverified], :transition/to [:document/verified]}
                 :document/reject    {:transition/from [:document/unverified], :transition/to [:document/rejected]}
                 :document/reverify  {:transition/from [:document/verified], :transition/to [:document/unverified]}
                 :document/publish   {:transition/from [:document/verified], :transition/to [:document/published]}
                 :document/unpublish {:transition/from [:document/published], :transition/to [:document/verified]}
                 :document/archive   {:transition/from [:document/published, :document/verified, :document/unverified], :transition/to [:document/archived]}}})


(def document {:document/name   " Simple name    "
               :document/author "John Doe"})

(defn on-enter [data]
  (println :on-enter)
  (throw (ex-message "Boom on enter!"))
  data)

(defn on-leave [data]
  (println :on-leave)
  data)

(defn on-error [data]
  (println :on-error)
  data)

(defn on-state-enter [data]
  (println :on-state-enter)
  (throw (ex-message "Boom on state enter!"))
  data)

(defn on-state-leave [data]
  (println :on-state-leave)
  data)

(defn on-state-error [data]
  (println :on-state-error)
  data)

(comment
  ;; example 1

  (def d {:document/name   " sImplE nAme    "
          :document/author "John Doe"})

  (def f {::fsm/name   :acme/document-fsm
          ::fsm/desc   "Simple document FSM"
          ::fsm/enter  [on-enter]
          ::fsm/leave  [on-leave]
          ::fsm/error  [on-error]
          ::fsm/states {:document/unverified {::fsm.state/desc     "Unverified"
                                              ::fsm.state/initial? true
                                              ::fsm.state/enter    [on-state-enter]
                                              ::fsm.state/leave    [on-state-leave]
                                              ::fsm.state/error    [on-state-error]}
                        :document/verified   {::fsm.state/desc "Verified"}
                        :document/published  {::fsm.state/desc "Published"}
                        :document/archived   {::fsm.state/desc "Archived"}
                        :document/rejected   {::fsm.state/desc "Rejected"}}})

  (def d1 (fsm/assign d f))
  (meta d1)

  (def d2 (fsm/init d1))
  ;; => :on-enter
  ;;    :on-error
  ;;    :on-state-enter
  ;;    :on-state-error
  (meta d2)

  (def d3 (fsm/init d2))
  (meta d3)
  (identity d3)

  (fsm/init {})
  ;; => error
  )



(comment
  ;; example 2

  (defn on-state-enter [data]
    (update data :document/name (comp str/capitalize str/trim)))

  (def d {:document/name   " sImplE nAme    "
          :document/author "John Doe"})

  (def f {::fsm/name   :acme/document-fsm
          ::fsm/desc   "Simple document FSM"
          ::fsm/states {:document/unverified {::fsm.state/desc     "Unverified"
                                              ::fsm.state/initial? true
                                              ::fsm.state/enter    [on-state-enter]}
                        :document/verified   {::fsm.state/desc "Verified"}
                        :document/published  {::fsm.state/desc "Published"}
                        :document/archived   {::fsm.state/desc "Archived"}
                        :document/rejected   {::fsm.state/desc "Rejected"}}})


  (def d1 (-> d
              (fsm/assign f)
              fsm/init))
  (meta d1)
  (identity d1)
  ;; => {:document/name "Simple name", :document/author "John Doe"}
  )
