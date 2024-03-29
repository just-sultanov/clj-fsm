(ns clj-fsm.fsm.helpers
  "Helper functions."
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
    #?@(:clj [[clojure.core :as c]
              [clojure.main :as m]])
    #?@(:cljs [[clojure.string :as str]
               [goog.string :as gstr]
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


(defn fn-name
  "Returns a name of the given function."
  {:added "0.1.18"}
  [f]
  #?(:clj
     (as-> (str f) $
           (m/demunge $)
           (or (re-find #"(.+)--\d+@" $)
               (re-find #"(.+)@" $))
           (last $))

     :cljs
     (as-> (.-name f) $
           (demunge $)
           (str/split $ #"/")
           ((juxt butlast last) $)
           (update $ 0 #(str/join "." %))
           (str/join "/" $))))


(defn to-coll
  "Returns vector of `x`.
  If `x` is sequential returns `as is`, else returns `x` in vector.
  Otherwise, if `x` is nil returns an empty vector."
  {:added "0.1.18"}
  [x]
  (cond
    (sequential? x) x
    (some? x) [x]
    :else []))
