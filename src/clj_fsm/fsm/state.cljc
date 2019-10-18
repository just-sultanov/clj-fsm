(ns clj-fsm.fsm.state
  "FSM state."
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::name qualified-keyword?)
(s/def ::desc string?)
(s/def ::initial? boolean?)

(s/def ::state
  (s/keys :req [::desc]
          :opt [::initial?]))


;; TODO: rewrite with loop/recur for performance optimization
(defn states-valid? [states]
  (some->> states
    (map (fn [[_ v]]
           (::initial? v)))
    (filter true?)
    count
    (= 1)))


(s/def ::states
  (s/and
    (s/map-of ::name ::state :min-count 2)
    (s/conformer
      (fn [x]
        (if (states-valid? x)
          x
          ::s/invalid)))))
