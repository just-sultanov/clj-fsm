(ns clj-fsm.fsm
  "FSM core."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]
    [clj-fsm.fsm.helpers :as helpers]
    [clj-fsm.fsm.state :as fsm.state]))

;;
;; FSM specifications
;;

(s/def ::name qualified-keyword?)
(s/def ::desc string?)
(s/def ::state ::fsm.state/name)
(s/def ::states ::fsm.state/states)

(s/def ::fn
  (s/with-gen
    ifn?
    (constantly (gen/return identity))))

(s/def ::fns
  (s/coll-of ::fn :kind vector? :min-count 1))

(s/def ::enter ::fns)
(s/def ::leave ::fns)
(s/def ::error ::fns)


(s/def ::fsm
  (s/keys :req [::name ::desc ::states]
          :opt [::enter ::leave ::error]))



;;
;; API
;;

(def meta-fsm-key ::fsm)

(defn assign
  "Assigns `fsm` to the given data metadata."
  [data fsm]
  (if (s/valid? ::fsm fsm)
    (vary-meta data assoc meta-fsm-key fsm)
    (throw
      (ex-info (helpers/format "The given `fsm` is not satisfied by the specification: `%s`" ::fsm)
               {:fsm      fsm
                :problems (s/explain-data ::fsm fsm)}))))


(defn unassign
  "Removes `fsm` from the given data metadata."
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
  (::name (get-fsm data)))


(defn get-fsm-desc
  "Returns `fsm` desc."
  {:added "0.1.4"}
  [data]
  (::desc (get-fsm data)))


(defn get-fsm-enter
  "Returns `fsm` enter function."
  {:added "0.1.9"}
  [data]
  (::enter (get-fsm data)))


(defn get-fsm-leave
  "Returns `fsm` leave function."
  {:added "0.1.9"}
  [data]
  (::leave (get-fsm data)))


(defn get-fsm-error
  "Returns `fsm` error function."
  {:added "0.1.9"}
  [data]
  (::error (get-fsm data)))


(defn get-fsm-states
  "Returns `fsm` states."
  {:added "0.1.4"}
  [data]
  (::states (get-fsm data)))


(defn get-fsm-states-names
  "Returns `fsm` states names."
  {:added "0.1.9"}
  [data]
  (keys (get-fsm-states data)))


(defn get-fsm-initial-state
  "Returns `fsm` initial state name."
  {:added "0.1.9"}
  [data]
  (some->> data
    get-fsm-states
    (helpers/find-first #(some? (::fsm.state/initial? (second %))))
    first))


(defn get-fsm-state
  "Returns `fsm` current state or state by the given state name."
  {:added "0.1.7"}
  ([data]
   (::state (get-fsm data)))

  ([data name]
   (some-> data
     get-fsm-states
     (get name))))


(defn apply-state
  "Apply state by the given state name."
  {:added "0.1.9"}
  ([data]
   (let [name (get-fsm-initial-state data)]
     (if-some [enter (get-fsm-enter data)]
       (let [on-enter (apply comp enter)]
         (try
           (apply-state (on-enter data) name) ;; TODO: add guard or enter fn is enough?
           (catch
             #?@(:clj  [Throwable e]
                 :cljs [js/Error e])
             (if-some [error (get-fsm-error data)]
               (let [on-error (apply comp error)]
                 (on-error data e)))))) ;; TODO: return data + error?
       (apply-state data name))))

  ([data name]
   (let [fsm (get-fsm data)]
     (if-some [_ (get-fsm-state data name)]
       (let [fsm' (assoc fsm ::state name)]
         (assign data fsm')) ;; TODO: add enter/leave/error/guard fns to the ::fsm/state and rewrite
       (throw
         (ex-info (helpers/format "No `fsm` state with the given name: `%s`" name)
                  {:fsm  fsm
                   :name name}))))))


(defn init
  "Initializes `fsm` and apply the initial state."
  {:added "0.1.9"}
  [data]
  (if (get-fsm-state data)
    data
    (if-some [_ (get-fsm data)]
      (apply-state data)
      (throw
        (ex-info "No assigned `fsm` to this data"
                 {:data data
                  :meta (meta data)})))))



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
  (-get-fsm-enter [data])
  (-get-fsm-leave [data])
  (-get-fsm-error [data])
  (-get-fsm-states [data])
  (-get-fsm-states-names [data])
  (-get-fsm-initial-state [data])
  (-get-fsm-state [data] [data name])
  (-init [data]))



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

     (-get-fsm-state
       ([data]
        (get-fsm-state data))

       ([data name]
        (get-fsm-state data name)))

     (-init [data]
       (init data)))

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

     (-get-fsm-state
       ([data]
        (get-fsm-state data))

       ([data name]
        (get-fsm-state data name)))

     (-init [data]
       (init data))))
