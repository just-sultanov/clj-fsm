(ns clj-fsm.fsm.state
  "FSM state."
  (:require
    [clojure.spec.alpha :as s]))

;;
;; FSM state specifications
;;

(s/def ::name qualified-keyword?)
(s/def ::desc string?)
(s/def ::initial? boolean?)
