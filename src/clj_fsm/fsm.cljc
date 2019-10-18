(ns clj-fsm.fsm
  "FSM core."
  (:require
    [clojure.spec.alpha :as s]
    [clj-fsm.fsm.helpers :as helpers]))

;;
;; Specifications
;;

(s/def ::name qualified-keyword?)
(s/def ::desc string?)
(s/def ::state qualified-keyword?)
(s/def ::data any?)
(s/def ::states map?)
(s/def ::events map?)
(s/def ::enter map?)
(s/def ::leave map?)
(s/def ::error map?)

(s/def ::fsm
  (s/keys :req [::name
                ::desc]
          :opt [::state
                ::data
                ::states
                ::events
                ::enter
                ::leave
                ::error]))



;;
;; API
;;

(defn assign [data fsm]
  (let [key ::fsm]
    (if (s/valid? key fsm)
      (vary-meta data assoc key fsm)
      (throw
        (ex-info (helpers/format "The given `fsm` is not satisfied by the specification: `%s`" key)
                 {:fsm      fsm
                  :problems (s/explain-data key fsm)})))))


(defn unassign [data]
  (vary-meta data dissoc ::fsm))


(defn get-fsm [data]
  (get (meta data) ::fsm))


(defn get-fsm-name [data]
  (::name (get-fsm data)))


(defn get-fsm-desc [data]
  (::desc (get-fsm data)))



;;
;; Protocols
;;


(defprotocol IFSM
  "IFSM protocol."
  (-assign [data fsm])
  (-unassign [data])
  (-get-fsm [data])
  (-get-fsm-name [data])
  (-get-fsm-desc [data]))



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
       (get-fsm-desc data)))

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
       (get-fsm-desc data))))
