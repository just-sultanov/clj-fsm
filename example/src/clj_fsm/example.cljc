(ns clj-fsm.example
  "FSM usage example."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm :as fsm]
    [clj-fsm.fsm.state :as fsm.state]))

(def document-fsm
  {::fsm/name          :acme/document-fsm
   ::fsm/desc          "Simple document FSM"
   ::fsm/states        {:document/unverified {::fsm.state/desc "Unverified", ::fsm.state/initial? true}
                        :document/verified   {::fsm.state/desc "Verified"}
                        :document/published  {::fsm.state/desc "Published"}
                        :document/archived   {::fsm.state/desc "Archived"}
                        :document/rejected   {::fsm.state/desc "Rejected"}}
   ::fsm/events        {:document/verify    {:transition/from [:document/unverified], :transition/to [:document/verified]}
                        :document/reject    {:transition/from [:document/unverified], :transition/to [:document/rejected]}
                        :document/reverify  {:transition/from [:document/verified], :transition/to [:document/unverified]}
                        :document/publish   {:transition/from [:document/verified], :transition/to [:document/published]}
                        :document/unpublish {:transition/from [:document/published], :transition/to [:document/verified]}
                        :document/archive   {:transition/from [:document/published, :document/verified, :document/unverified], :transition/to [:document/archived]}}

   ::fsm.state/current :document/unverified})


(def document {:document/name   "Simple name"
               :document/author "John Doe"})


(comment
  (gen/generate (s/gen ::fsm/fsm))
  )
