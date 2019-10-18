(ns clj-fsm.fsm.helpers
  "Helper functions."
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
    #?(:clj [clojure.core :as c])
    #?@(:cljs [[goog.string :as gstr]
               [goog.string.format]])))

(defn find-first
  "Returns a first element in the collection by the given predicate."
  {:added "0.1.4"}
  [pred? coll]
  (some (fn [v]
          (when (pred? v)
            v))
        coll))


(def ^{:added "0.1.4"}
  format
  "Formats a string."
  #?(:clj  c/format
     :cljs gstr/format))
