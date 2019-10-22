(ns clj-fsm.fsm.event
  "FSM event."
  (:require
    [clojure.spec.alpha :as s]
    [clj-fsm.fsm.state]))

;;
;; Event specifications
;;

(s/def :event/name qualified-keyword?)

(s/def :event/transition
  (s/coll-of :state/name :kind vector?))

(s/def :transition/from :event/transition)
(s/def :transition/to :event/transition)

(s/def :event/map
  (s/keys :req [:transition/from :transition/to]))

(s/def :events/map
  (s/map-of :event/name :event/map :min-count 1))
