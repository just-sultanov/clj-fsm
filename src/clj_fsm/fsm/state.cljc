(ns clj-fsm.fsm.state
  "FSM state."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]))

;;
;; Helper functions
;;

;; TODO: rewrite with loop/recur for performance optimization
(defn states-valid?
  "Returns `true` if the given states contains only one initial state and only one finish state.
  Otherwise `false`."
  {:added "0.1.4"}
  [states]
  (->> states
       (reduce-kv (fn [acc _ v]
                    (let [initial? (:state/initial? v)
                          finish?  (:state/finish? v)]
                      (cond
                        (and initial?
                             (not finish?)) (update acc 0 inc)
                        (and finish?
                             (not initial?)) (update acc 1 inc)
                        :else acc)))
                  [0 0])
       (= [1 1])))



;;
;; State specifications
;;

(s/def :state/fn
  (s/with-gen
    ifn?
    (constantly (gen/return (comp identity first)))))

(s/def :state/fns
  (s/coll-of :state/fn :kind vector? :min-count 1))

(s/def :state/name qualified-keyword?)
(s/def :state/description string?)
(s/def :state/initial? boolean?)
(s/def :state/finish? boolean?)
(s/def :state/enter :state/fns)
(s/def :state/leave :state/fns)
(s/def :state/error :state/fns)

(s/def :state/map
  (s/keys :req [:state/description]
          :opt [:state/initial? :state/finish? :state/enter :state/leave :state/error]))

(s/def :states/map
  (s/and
    (s/map-of :state/name :state/map :min-count 2)
    (s/conformer
      (fn [x]
        (if (states-valid? x)
          x
          ::s/invalid)))))
