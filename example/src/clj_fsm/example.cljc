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
  (throw (ex-message "Boom!"))
  data)

(defn on-error [data error]
  (println :on-error)
  data)

(comment
  (gen/generate (s/gen ::fsm/fsm))

  (def f {::fsm/name   :acme/document-fsm
          ::fsm/desc   "Simple document FSM"
          ::fsm/enter  [on-enter]
          ::fsm/error  [on-error]
          ::fsm/states {:document/unverified {::fsm.state/desc "Unverified", ::fsm.state/initial? true}
                        :document/verified   {::fsm.state/desc "Verified"}
                        :document/published  {::fsm.state/desc "Published"}
                        :document/archived   {::fsm.state/desc "Archived"}
                        :document/rejected   {::fsm.state/desc "Rejected"}}})


  (def f1 (fsm/assign document f))
  (meta f1)

  (def f2 (fsm/init f1))
  (meta f2)

  (def f3 (fsm/init f2))
  (meta f3)

  (fsm/init {})
  ;; => error


  (def fns [str/upper-case str/trim :document/name])
  (def f (apply comp fns))
  (f document)
  )
