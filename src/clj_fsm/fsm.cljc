(ns clj-fsm.fsm
  "FSM core."
  (:require
    [clojure.spec.alpha :as s]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm.state :as fsm.state]
    [clj-fsm.fsm.event :as fsm.event]))

;;
;; Internal specifications
;;

(s/def ::name qualified-keyword?)
(s/def ::description string?)
(s/def ::current ::fsm.state/name)
(s/def ::previous (s/nilable ::fsm.state/name))
(s/def ::states ::fsm.state/states)

(s/def ::enter ::fsm.state/enter)
(s/def ::leave ::fsm.state/leave)
(s/def ::error ::fsm.state/error)

(s/def ::event ::fsm.event/name)
(s/def ::events ::fsm.event/events)


;; Aliases

(s/def :fsm/name ::name)
(s/def :fsm/description ::description)
(s/def :fsm/current ::current)
(s/def :fsm/previous ::previous)
(s/def :fsm/states ::states)
(s/def :fsm/enter ::enter)
(s/def :fsm/leave ::leave)
(s/def :fsm/error ::error)
(s/def :fsm/event ::event)
(s/def :fsm/events ::events)


;;
;; FSM specifications
;;

(s/def ::uninitialized
  (s/keys :req [:fsm/name :fsm/description :fsm/states :fsm/events]
          :opt [:fsm/enter :fsm/leave :fsm/error]))

(s/def ::initialized
  (s/keys :req [:fsm/name :fsm/description :fsm/current :fsm/previous :fsm/states :fsm/events]
          :opt [:fsm/enter :fsm/leave :fsm/error]))

(s/def ::fsm
  (s/or ::uninitialized ::uninitialized
        ::initialized ::initialized))


;;
;; API
;;

(def meta-fsm-key ::fsm)

(defn assign
  "Assigns `fsm` to the given data metadata."
  {:added "0.1.4"}
  [data fsm]
  (if (s/valid? ::fsm fsm)
    (vary-meta data assoc meta-fsm-key fsm)
    (throw
      (ex-info (helpers/format "The given `fsm` is not satisfied by the specification: `%s`" ::fsm)
               {:fsm      fsm
                :problems (s/explain-data ::fsm fsm)}))))


(defn unassign
  "Removes `fsm` from the given data metadata."
  {:added "0.1.4"}
  [data]
  (vary-meta data dissoc meta-fsm-key))


(defn get-fsm
  "Returns `fsm` from the given data metadata."
  {:added "0.1.4"}
  [data]
  (get (meta data) meta-fsm-key))


(defn get-fsm-name
  "Returns `fsm` name."
  {:added "0.1.4"}
  [data]
  (:fsm/name (get-fsm data)))


(defn get-fsm-description
  "Returns `fsm` description."
  {:added "0.1.4"}
  [data]
  (:fsm/description (get-fsm data)))


(defn get-fsm-enter
  "Returns `fsm` enter function."
  {:added "0.1.9"}
  [data]
  (:fsm/enter (get-fsm data)))


(defn get-fsm-leave
  "Returns `fsm` leave function."
  {:added "0.1.9"}
  [data]
  (:fsm/leave (get-fsm data)))


(defn get-fsm-error
  "Returns `fsm` error function."
  {:added "0.1.9"}
  [data]
  (:fsm/error (get-fsm data)))


(defn get-fsm-states
  "Returns `fsm` states."
  {:added "0.1.4"}
  [data]
  (:fsm/states (get-fsm data)))


(defn get-fsm-states-names
  "Returns `fsm` states names."
  {:added "0.1.9"}
  [data]
  (keys (get-fsm-states data)))


(defn get-fsm-current-state
  "Returns `fsm` current state name."
  {:added "0.1.14"}
  [data]
  (:fsm/current (get-fsm data)))


(defn get-fsm-previous-state
  "Returns `fsm` previous state name."
  {:added "0.1.14"}
  [data]
  (:fsm/previous (get-fsm data)))


(defn get-fsm-initial-state
  "Returns `fsm` initial state name."
  {:added "0.1.9"}
  [data]
  (some->> data
    get-fsm-states
    (helpers/find-first #(some? (::fsm.state/initial? (second %))))
    first))


(defn get-fsm-finish-state
  "Returns `fsm` finish state name."
  {:added "0.1.14"}
  [data]
  (some->> data
    get-fsm-states
    (helpers/find-first #(some? (::fsm.state/finish? (second %))))
    first))


(defn get-fsm-state
  "Returns `fsm` state by the given state name."
  {:added "0.1.7"}
  [data name]
  (some-> data
    get-fsm-states
    (get name)))


(defn get-fsm-state-enter
  "Returns `fsm` state enter function."
  {:added "0.1.10"}
  [data name]
  (::fsm.state/enter (get-fsm-state data name)))


(defn get-fsm-state-leave
  "Returns `fsm` state leave function."
  {:added "0.1.10"}
  [data name]
  (::fsm.state/leave (get-fsm-state data name)))


(defn get-fsm-state-error
  "Returns `fsm` state error function."
  {:added "0.1.10"}
  [data name]
  (::fsm.state/error (get-fsm-state data name)))


(defn get-fsm-events
  "Returns `fsm` events."
  {:added "0.1.14"}
  [data]
  (:fsm/events (get-fsm data)))


(defn get-fsm-events-names
  "Returns `fsm` events names."
  {:added "0.1.14"}
  [data]
  (keys (get-fsm-events data)))


(defn get-fsm-event
  "Returns `fsm` event by the given event name."
  {:added "0.1.14"}
  [data name]
  (some-> data
    get-fsm-events
    (get name)))


(defn init?
  "Returns `true` when `fsm` has been initialized. Otherwise `false`."
  {:added "0.1.14"}
  [data]
  (and
    (get-fsm-current-state data)
    (get-fsm-initial-state data)
    true))


(defn finish?
  "Returns `true` when `fsm` has been finished. Otherwise `false`."
  {:added "0.1.14"}
  [data]
  (let [current (get-fsm-current-state data)
        finish  (get-fsm-finish-state data)]
    (and
      (= current finish)
      (every? some? [current finish]))))


(defn apply-fsm-on-error
  "Applies `fsm` on-error function to the given data."
  {:added "0.1.10"}
  [data name error]
  (if-some [f (get-fsm-error data)]
    (let [on-error (apply comp f)]
      (on-error data name error))
    data))


(defn apply-fsm-on-leave
  "Applies `fsm` on-leave function to the given data."
  {:added "0.1.10"}
  [data name]
  (if-some [f (get-fsm-leave data)]
    (let [on-leave (apply comp f)]
      (try
        (on-leave data name)
        (catch
          #?@(:clj  [Throwable error]
              :cljs [js/Error error])
          (apply-fsm-on-error data name error))))
    data))


(defn apply-fsm-on-enter
  "Applies `fsm` on-enter function to the given data."
  {:added "0.1.10"}
  [data name]
  (if-some [f (get-fsm-enter data)]
    (let [on-enter (apply comp f)]
      (try
        (on-enter data name)
        (catch
          #?@(:clj  [Throwable error]
              :cljs [js/Error error])
          (apply-fsm-on-error data name error))))
    data))


(defn apply-state-on-error
  "Applies `fsm` state on-error function to the given data."
  {:added "0.1.10"}
  [data name error]
  (if-some [f (get-fsm-state-error data name)]
    (let [on-error (apply comp f)]
      (on-error data name error))
    data))


(defn apply-state-on-leave
  "Applies `fsm` state on-leave function to the given data."
  {:added "0.1.10"}
  [data name]
  (if-some [f (get-fsm-state-leave data name)]
    (let [on-leave (apply comp f)]
      (try
        (on-leave data name)
        (catch
          #?@(:clj  [Throwable error]
              :cljs [js/Error error])
          (apply-state-on-error data name error))))
    data))


(defn apply-state-on-enter
  "Applies `fsm` state on-enter function to the given data."
  {:added "0.1.10"}
  [data name]
  (if-some [f (get-fsm-state-enter data name)]
    (let [on-enter (apply comp f)]
      (try
        (on-enter data name)
        (catch
          #?@(:clj  [Throwable error]
              :cljs [js/Error error])
          (apply-state-on-error data name error))))
    data))


;; TODO: Write doc about lifecycle
;; - assign fsm to data
;; - init (transit to initial state):
;;   - invoke global fsm enter fn
;;   - invoke initial state on enter fn
;;   - set fsm current state to initial

(defn init
  "Initializes `fsm`. If `fsm` has been initialized, then returns the given data without any changes."
  {:added "0.1.9"}
  [data]
  (cond
    (init? data)
    data

    (nil? (get-fsm data))
    (throw
      (ex-info "Not exists assigned `fsm` in the given data"
               {:data data
                :meta (meta data)}))

    :else
    (let [fsm           (get-fsm data)
          initial-state (get-fsm-initial-state data)
          data'         (-> data
                            (apply-fsm-on-enter initial-state)
                            (apply-state-on-enter initial-state))
          fsm'          (assoc fsm :fsm/current initial-state)]
      (assign data' fsm'))))


(defn finish
  "Finalizes `fsm` and apply the finish state.
  If `fsm` has been finalized, then returns the given data without any changes."
  {:added "0.1.14"}
  [data]
  (cond
    (finish? data)
    data

    (nil? (get-fsm data))
    (throw
      (ex-info "Not exists assigned `fsm` in the given data"
               {:data data
                :meta (meta data)}))

    (not (init? data))
    (throw
      (ex-info "Not initialized `fsm` in the given data"
               {:data data
                :meta (meta data)}))

    :else
    (let [fsm            (get-fsm data)
          previous-state (get-fsm-current-state data)
          finish-state   (get-fsm-finish-state data)
          data'          (-> data
                             (apply-state-on-leave previous-state)
                             (apply-state-on-enter finish-state)
                             (apply-fsm-on-leave finish-state))
          fsm'           (assoc fsm :fsm/current finish-state
                                    :fsm/previous previous-state)]
      (assign data' fsm'))))


(defn apply-state
  "Apply state by the given state name."
  {:added "0.1.9"}
  [data next]
  (cond
    (nil? (get-fsm-state data next))
    (throw
      (ex-info (helpers/format "Not exists `fsm` state with the given name: `%s`" next)
               {:fsm  (get-fsm data)
                :name next}))


    (= next (get-fsm-initial-state data))
    (init data)

    (= next (get-fsm-finish-state data))
    (finish data)

    :else
    (let [fsm     (get-fsm data)
          current (get-fsm-current-state data)
          data'   (cond-> data
                    (some? current) (apply-state-on-leave current)
                    :always (apply-state-on-enter next))
          fsm'    (assoc fsm
                    :fsm/current next
                    :fsm/previous current)]
      (assign data' fsm'))))



;;
;; Protocols
;;


(defprotocol IFSM
  "IFSM protocol."
  (-assign [data fsm])
  (-unassign [data])
  (-get-fsm [data])
  (-get-fsm-name [data])
  (-get-fsm-description [data])
  (-get-fsm-enter [data])
  (-get-fsm-leave [data])
  (-get-fsm-error [data])
  (-get-fsm-states [data])
  (-get-fsm-states-names [data])
  (-get-fsm-initial-state [data])
  (-get-fsm-previous-state [data])
  (-get-fsm-current-state [data])
  (-get-fsm-finish-state [data])
  (-get-fsm-state [data name])
  (-get-fsm-state-enter [data name])
  (-get-fsm-state-leave [data name])
  (-get-fsm-state-error [data name])
  (-get-fsm-events [data])
  (-get-fsm-events-names [data])
  (-get-fsm-event [data name])
  (-init? [data])
  (-finish? [data])
  (-init [data]))



(extend-protocol IFSM
  #?(:clj Object :cljs js/Object)
  (-assign [data fsm]
    (assign data fsm))

  (-unassign [data]
    (unassign data))

  (-get-fsm [data]
    (get-fsm data))

  (-get-fsm-name [data]
    (get-fsm-name data))

  (-get-fsm-description [data]
    (get-fsm-description data))

  (-get-fsm-enter [data]
    (get-fsm-enter data))

  (-get-fsm-leave [data]
    (get-fsm-leave data))

  (-get-fsm-error [data]
    (get-fsm-error data))

  (-get-fsm-states [data]
    (get-fsm-states data))

  (-get-fsm-states-names [data]
    (get-fsm-states-names data))

  (-get-fsm-initial-state [data]
    (get-fsm-initial-state data))

  (-get-fsm-previous-state [data]
    (get-fsm-previous-state data))

  (-get-fsm-current-state [data]
    (get-fsm-current-state data))

  (-get-fsm-finish-state [data]
    (get-fsm-finish-state data))

  (-get-fsm-state [data name]
    (get-fsm-state data name))

  (-get-fsm-state-enter [data name]
    (get-fsm-state-enter data name))

  (-get-fsm-state-leave [data name]
    (get-fsm-state-leave data name))

  (-get-fsm-state-error [data name]
    (get-fsm-state-error data name))

  (-get-fsm-events [data]
    (get-fsm-events data))

  (-get-fsm-events-names [data]
    (get-fsm-events-names data))

  (-get-fsm-event [data name]
    (get-fsm-event data name))

  (-init? [data]
    (init? data))

  (-finish? [data]
    (finish? data))

  (-init [data]
    (init data)))
