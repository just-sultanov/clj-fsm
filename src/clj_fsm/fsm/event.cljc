(ns clj-fsm.fsm.event
  "FSM event."
  (:require
    [clojure.spec.alpha :as s]
    [clj-fsm.fsm.state :as fsm.state]))

(s/def ::name qualified-keyword?)

(s/def ::transition
  (s/coll-of ::fsm.state/name :kind vector?))

(s/def ::transition-from ::transition)
(s/def ::transition-to ::transition)


(s/def ::event
  (s/keys :req [::transition-from ::transition-to]))


(s/def ::events
  (s/map-of ::name ::event :min-count 1))
