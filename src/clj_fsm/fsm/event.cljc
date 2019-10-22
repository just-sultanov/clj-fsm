(ns clj-fsm.fsm.event
  "FSM event."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.state]))

;;
;; Event specifications
;;

(s/def :event/fn
  (s/with-gen
    ifn?
    (constantly (gen/return (comp some? first)))))

(s/def :event/fns
  (s/coll-of :event/fn :kind vector? :min-count 1))


(s/def :event/name qualified-keyword?)

(s/def :transition/from (s/coll-of :state/name :kind vector?))
(s/def :transition/to :state/name)
(s/def :transition/guards :event/fns)

(s/def :event/map
  (s/keys :req [:transition/from :transition/to]
          :opt [:transition/guards]))

(s/def :events/map
  (s/map-of :event/name :event/map :min-count 1))
