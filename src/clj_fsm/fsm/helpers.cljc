(ns clj-fsm.fsm.helpers
  "Helper functions."
  #?(:clj (:refer-clojure :exclude [format]))
  (:require
    [clojure.string :as str]
    #?@(:clj [[clojure.core :as c]
              [clojure.main :as m]])
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
