(ns clj-fsm.fsm.state
  "FSM state."
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test.check.generators :as gen]))

(s/def ::name qualified-keyword?)
(s/def ::desc string?)
(s/def ::initial? boolean?)
(s/def ::finish? boolean?)

(s/def ::fn
  (s/with-gen
    ifn?
    (constantly (gen/return (comp identity first)))))

(s/def ::fns
  (s/coll-of ::fn :kind vector? :min-count 1))

(s/def ::enter ::fns)
(s/def ::leave ::fns)
(s/def ::error ::fns)


(s/def ::state
  (s/keys :req [::desc]
          :opt [::initial? ::finish? ::enter ::leave ::error]))


;; TODO: rewrite with loop/recur for performance optimization
(defn states-valid?
  "Returns `true` if the given states contains only one initial state and only one finish state.
  Otherwise `false`."
  {:added "0.1.4"}
  [states]
  (->> states
       (reduce-kv (fn [acc _ v]
                    (let [initial? (::initial? v)
                          finish?  (::finish? v)]
                      (cond
                        (and initial?
                             (not finish?)) (update acc 0 inc)
                        (and finish?
                             (not initial?)) (update acc 1 inc)
                        :else acc)))
                  [0 0])
       (= [1 1])))


(s/def ::states
  (s/and
    (s/map-of ::name ::state :min-count 2)
    (s/conformer
      (fn [x]
        (if (states-valid? x)
          x
          ::s/invalid)))))
