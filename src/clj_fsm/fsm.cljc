(ns clj-fsm.fsm
  "FSM core."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm.state]
    [clj-fsm.fsm.event]))

;;
;; FSM specifications
;;

(s/def :fsm/fn
  (s/with-gen
    ifn?
    (constantly (gen/return (comp identity first)))))

(s/def :fsm/fns
  (s/coll-of :fsm/fn :kind vector? :min-count 1))


(s/def :fsm/name qualified-keyword?)
(s/def :fsm/description string?)
(s/def :fsm/current :state/name)
(s/def :fsm/previous (s/nilable :state/name))
(s/def :fsm/states :states/map)
(s/def :fsm/enter :fsm/fns)
(s/def :fsm/leave :fsm/fns)
(s/def :fsm/error :fsm/fns)
(s/def :fsm/event :event/map)
(s/def :fsm/events :events/map)


(s/def :fsm/uninitialized
  (s/keys :req [:fsm/name :fsm/description :fsm/states :fsm/events]
          :opt [:fsm/enter :fsm/leave :fsm/error]))

(s/def :fsm/initialized
  (s/keys :req [:fsm/name :fsm/description :fsm/current :fsm/previous :fsm/states :fsm/events]
          :opt [:fsm/enter :fsm/leave :fsm/error]))

(s/def :fsm/map
  (s/or :fsm/uninitialized :fsm/uninitialized
        :fsm/initialized :fsm/initialized))


;;
;; API
;;

(def meta-fsm-key ::fsm)

(defn assign
  "Assigns `fsm` to the given data metadata."
  {:added "0.1.4"}
  [data fsm]
  (if (s/valid? :fsm/map fsm)
    (vary-meta data assoc meta-fsm-key fsm)
    (throw
      (ex-info (helpers/format "The given `fsm` is not satisfied by the specification: `%s`" :fsm/map)
               {:fsm      fsm
                :problems (s/explain-data :fsm/map fsm)}))))


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


(defn fsm->initial-state
  "Returns `fsm` initial state name."
  {:added "0.1.9"}
  [fsm]
  (some->> fsm
    :fsm/states
    (helpers/find-first #(some? (:state/initial? (second %))))
    first))


(defn fsm->finish-state
  "Returns `fsm` finish state name."
  {:added "0.1.14"}
  [fsm]
  (some->> fsm
    :fsm/states
    (helpers/find-first #(some? (:state/finish? (second %))))
    first))


(defn fsm->state
  "Returns `fsm` state by the given state name."
  {:added "0.1.7"}
  [fsm name]
  (some-> fsm
    :fsm/states
    (get name)))


(defn fsm->event
  "Returns `fsm` event by the given event name."
  {:added "0.1.14"}
  [fsm name]
  (some-> fsm
    :fsm/events
    (get name)))


(defn fsm-initialized?
  "Returns `true` when `fsm` has been initialized. Otherwise `false`."
  {:added "0.1.14"}
  [fsm]
  (some? (:fsm/current fsm)))


(defn fsm-finalized?
  "Returns `true` when `fsm` has been finalized. Otherwise `false`."
  {:added "0.1.14"}
  [fsm]
  (let [current (:fsm/current fsm)
        finish  (fsm->finish-state fsm)]
    (and
      (= current finish)
      (every? some? [current finish]))))


(defn apply-fsm-on-error
  "Applies `fsm` on-error function to the given data."
  {:added "0.1.10"}
  [data name error]
  (let [fsm (get-fsm data)]
    (if-some [f (:fsm/error fsm)]
      (let [on-error (apply comp f)]
        (on-error data name error))
      data)))


(defn apply-fsm-on-leave
  "Applies `fsm` on-leave function to the given data."
  {:added "0.1.10"}
  [data name]
  (let [fsm (get-fsm data)]
    (if-some [f (:fsm/leave fsm)]
      (let [on-leave (apply comp f)]
        (try
          (on-leave data name)
          (catch
            #?@(:clj  [Throwable error]
                :cljs [js/Error error])
            (apply-fsm-on-error data name error))))
      data)))


(defn apply-fsm-on-enter
  "Applies `fsm` on-enter function to the given data."
  {:added "0.1.10"}
  [data name]
  (let [fsm (get-fsm data)]
    (if-some [f (:fsm/enter fsm)]
      (let [on-enter (apply comp f)]
        (try
          (on-enter data name)
          (catch
            #?@(:clj  [Throwable error]
                :cljs [js/Error error])
            (apply-fsm-on-error data name error))))
      data)))


(defn apply-state-on-error
  "Applies `fsm` state on-error function to the given data."
  {:added "0.1.10"}
  [data name error]
  (let [fsm   (get-fsm data)
        state (fsm->state fsm name)]
    (if-some [f (:state/error state)]
      (let [on-error (apply comp f)]
        (on-error data name error))
      data)))


(defn apply-state-on-leave
  "Applies `fsm` state on-leave function to the given data."
  {:added "0.1.10"}
  [data name]
  (let [fsm   (get-fsm data)
        state (fsm->state fsm name)]
    (if-some [f (:state/leave state)]
      (let [on-leave (apply comp f)]
        (try
          (on-leave data name)
          (catch
            #?@(:clj  [Throwable error]
                :cljs [js/Error error])
            (apply-state-on-error data name error))))
      data)))


(defn apply-state-on-enter
  "Applies `fsm` state on-enter function to the given data."
  {:added "0.1.10"}
  [data name]
  (let [fsm   (get-fsm data)
        state (fsm->state fsm name)]
    (if-some [f (:state/enter state)]
      (let [on-enter (apply comp f)]
        (try
          (on-enter data name)
          (catch
            #?@(:clj  [Throwable error]
                :cljs [js/Error error])
            (apply-state-on-error data name error))))
      data)))


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
  (let [fsm (get-fsm data)]
    (cond
      (nil? fsm)
      (throw
        (ex-info "Not exists assigned `fsm` in the given data"
                 {:data data
                  :meta (meta data)}))

      (fsm-initialized? fsm)
      data

      :else
      (let [initial-state (fsm->initial-state fsm)
            data'         (-> data
                              (apply-fsm-on-enter initial-state)
                              (apply-state-on-enter initial-state))
            fsm'          (assoc fsm :fsm/current initial-state)]
        (assign data' fsm')))))


(defn finish
  "Finalizes `fsm` and apply the finish state.
  If `fsm` has been finalized, then returns the given data without any changes."
  {:added "0.1.14"}
  [data]
  (let [fsm (get-fsm data)]
    (cond
      (nil? fsm)
      (throw
        (ex-info "Not exists assigned `fsm` in the given data"
                 {:data data
                  :meta (meta data)}))

      (fsm-finalized? fsm)
      data

      (not (fsm-initialized? fsm))
      (throw
        (ex-info "Not initialized `fsm` in the given data"
                 {:data data
                  :meta (meta data)}))

      :else
      (let [previous-state (:fsm/current fsm)
            finish-state   (fsm->finish-state fsm)
            data'          (-> data
                               (apply-state-on-leave previous-state)
                               (apply-state-on-enter finish-state)
                               (apply-fsm-on-leave finish-state))
            fsm'           (assoc fsm :fsm/current finish-state
                                      :fsm/previous previous-state)]
        (assign data' fsm')))))


(defn apply-state
  "Apply state by the given state name."
  {:added "0.1.9"}
  [data next]
  (let [fsm (get-fsm data)]
    (cond
      (nil? (fsm->state fsm next))
      (throw
        (ex-info (helpers/format "Not exists `fsm` state with the given name: `%s`" next)
                 {:fsm  (fsm data)
                  :name next}))

      (= next (fsm->initial-state fsm))
      (init data)

      (= next (fsm->finish-state fsm))
      (finish data)

      :else
      (let [current (:fsm/current fsm)
            data'   (cond-> data
                      (some? current) (apply-state-on-leave current)
                      :always (apply-state-on-enter next))
            fsm'    (assoc fsm
                      :fsm/current next
                      :fsm/previous current)]
        (assign data' fsm')))))



;;
;; Protocols
;;


(defprotocol IFSM
  "IFSM protocol."
  (-assign [data fsm])
  (-unassign [data])
  (-get-fsm [data])
  (-fsm->state [fsm name])
  (-fsm->initial-state [fsm])
  (-fsm->finish-state [fsm])
  (-fsm->event [fsm name])
  (-fsm-initialized? [fsm])
  (-fsm-finalized? [fsm])
  (-init [data]))



(extend-protocol IFSM
  #?(:clj Object :cljs js/Object)
  (-assign [data fsm]
    (assign data fsm))

  (-unassign [data]
    (unassign data))

  (-get-fsm [data]
    (get-fsm data))

  (-fsm->state [fsm name]
    (fsm->state fsm name))

  (-fsm->initial-state [fsm]
    (fsm->initial-state fsm))

  (-fsm->finish-state [fsm]
    (fsm->finish-state fsm))

  (-fsm->event [fsm name]
    (fsm->event fsm name))

  (-fsm-initialized? [fsm]
    (fsm-initialized? fsm))

  (-fsm-finalized? [fsm]
    (fsm-finalized? fsm))

  (-init [data]
    (init data)))
