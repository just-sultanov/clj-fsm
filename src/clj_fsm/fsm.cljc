(ns clj-fsm.fsm
  "FSM core."
  (:require
    [clojure.spec.alpha :as s]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm.state :as fsm.state]))

;;
;; FSM specifications
;;

(s/def ::name qualified-keyword?)
(s/def ::desc string?)

(s/def ::fsm
  (s/keys :req [::name ::desc ::fsm.state/states]))



;;
;; API
;;

(def meta-fsm-key ::fsm)

(defn assign [data fsm]
  (if (s/valid? ::fsm fsm)
    (vary-meta data assoc meta-fsm-key fsm)
    (throw
      (ex-info (helpers/format "The given `fsm` is not satisfied by the specification: `%s`" ::fsm)
               {:fsm      fsm
                :problems (s/explain-data ::fsm fsm)}))))


(defn unassign [data]
  (vary-meta data dissoc meta-fsm-key))


(defn get-fsm [data]
  (get (meta data) meta-fsm-key))


(defn get-fsm-name [data]
  (::name (get-fsm data)))


(defn get-fsm-desc [data]
  (::desc (get-fsm data)))


(defn get-fsm-states [data]
  (::fsm.state/states (get-fsm data)))


(defn get-fsm-states-names [data]
  (keys (get-fsm-states data)))


(defn get-fsm-initial-state [data]
  (some->> data
    get-fsm-states
    (helpers/find-first #(some? (::fsm.state/initial? (second %))))
    first))


;;
;; Protocols
;;


(defprotocol IFSM
  "IFSM protocol."
  (-assign [data fsm])
  (-unassign [data])
  (-get-fsm [data])
  (-get-fsm-name [data])
  (-get-fsm-desc [data])
  (-get-fsm-states [data])
  (-get-fsm-states-names [data])
  (-get-fsm-initial-state [data]))



#?(:clj
   (extend-protocol IFSM
     Object
     (-assign [data fsm]
       (assign data fsm))

     (-unassign [data]
       (unassign data))

     (-get-fsm [data]
       (get-fsm data))

     (-get-fsm-name [data]
       (get-fsm-name data))

     (-get-fsm-desc [data]
       (get-fsm-desc data))

     (-get-fsm-states [data]
       (get-fsm-states data))

     (-get-fsm-states-names [data]
       (get-fsm-states-names data))

     (-get-fsm-initial-state [data]
       (get-fsm-initial-state data)))

   :cljs
   (extend-protocol IFSM
     js/Object
     (-assign [data fsm]
       (assign data fsm))

     (-unassign [data]
       (unassign data))

     (-get-fsm [data]
       (get-fsm data))

     (-get-fsm-name [data]
       (get-fsm-name data))

     (-get-fsm-desc [data]
       (get-fsm-desc data))

     (-get-fsm-states [data]
       (get-fsm-states data))

     (-get-fsm-states-names [data]
       (get-fsm-states-names data))

     (-get-fsm-initial-state [data]
       (get-fsm-initial-state data))))
