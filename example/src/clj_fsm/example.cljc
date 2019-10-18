(ns clj-fsm.example
  "FSM usage example."
  (:require
    [clj-fsm.fsm :as fsm]))

;;
;; Example
;;

(def document-fsm
  {:fsm/name   :acme/document-fsm
   :fsm/desc   "Simple document FSM"
   :fsm/states {:document/unverified {:state/desc "Unverified", :state/initial? true}
                :document/verified   {:state/desc "Verified"}
                :document/published  {:state/desc "Published"}
                :document/archived   {:state/desc "Archived"}
                :document/rejected   {:state/desc "Rejected"}}
   :fsm/events {:document/verify    {:transition/from [:document/unverified], :transition/to [:document/verified]}
                :document/reject    {:transition/from [:document/unverified], :transition/to [:document/rejected]}
                :document/reverify  {:transition/from [:document/verified], :transition/to [:document/unverified]}
                :document/publish   {:transition/from [:document/verified], :transition/to [:document/published]}
                :document/unpublish {:transition/from [:document/published], :transition/to [:document/verified]}
                :document/archive   {:transition/from [:document/published, :document/verified, :document/unverified], :transition/to [:document/archived]}}})

(def document {:document/name   "Simple name"
               :document/author "John Doe"})



;;
;; IFSM protocol implementation
;;
;;
;;(declare -impl-map)
;;
;;(defn- -to-fsm
;;  ([fsm]
;;   (-to-fsm fsm nil))
;;
;;  ([fsm impl-map]
;;   (vary-meta fsm merge (or impl-map -impl-map))))
;;
;;
;;(defn- -get-fsm
;;  ([name]
;;   (-get-fsm nil name))
;;
;;  ([registry name]
;;   (r/get registry name)))
;;
;;
;;(defn- -get-initial-state [fsm]
;;  (->> (:fsm/states fsm)
;;       (h/find-first #(some? (:state/initial? (second %))))
;;       first))
;;
;;
;;(defn- -init
;;  ([fsm]
;;   (-init fsm nil))
;;
;;  ([fsm data]
;;   (-init nil fsm data))
;;
;;  ([registry fsm data]
;;   {:pre  [(s/valid? :fsm/map fsm) (s/valid? :fsm/data data)]
;;    :post [(s/valid? :fsm/map %)]}
;;   (let [initial-state (-get-initial-state fsm)
;;         fsm           (assoc fsm
;;                         :fsm/data data
;;                         :fsm/state initial-state)]
;;     (r/register registry fsm)
;;     fsm)))
;;
;;
;;(defn- -get-name [fsm]
;;  (:fsm/name fsm))
;;
;;
;;(defn- -get-desc [fsm]
;;  (:fsm/desc fsm))
;;
;;
;;(defn- -get-state [fsm]
;;  (:fsm/state fsm))
;;
;;
;;(defn- -get-data [fsm]
;;  (:fsm/data fsm))
;;
;;
;;(defn- -get-states [fsm]
;;  (:fsm/states fsm))
;;
;;
;;(defn- -get-events [fsm]
;;  (:fsm/events fsm))
;;
;;
;;(def -impl-map
;;  {`cp/datafy            identity
;;   `fp/to-fsm            -to-fsm
;;   `fp/get-fsm           -get-fsm
;;   `fp/init              -init
;;   `fp/get-name          -get-name
;;   `fp/get-desc          -get-desc
;;   `fp/get-initial-state -get-initial-state
;;   `fp/get-state         -get-state
;;   `fp/get-data          -get-data
;;   `fp/get-states        -get-states
;;   `fp/get-events        -get-events})
;;
;;
;;;;
;;;; FSM API
;;;;
;;
;;(defn to-fsm
;;  ([fsm]
;;   (to-fsm fsm nil))
;;
;;  ([fsm impl-map]
;;   (-to-fsm fsm impl-map)))
;;
;;
;;(defn get-fsm
;;  ([name]
;;   (get-fsm nil name))
;;
;;  ([registry name]
;;   (r/get registry name)))
;;
;;
;;(defn init
;;  ([fsm]
;;   (init fsm nil))
;;
;;  ([fsm data]
;;   (init nil fsm data))
;;
;;  ([registry fsm data]
;;   (-init registry fsm data)))



;;(comment
;;
;;  (fsm/assign document-fsm document) ;; vary meta
;;  (fsm/unassign document) ;; remove meta from document (only 'clj-fsm.protocols.fsm/*)
;;
;;  (fsm/register document-fsm)
;;  (fsm/unregister :acme/document-fsm) ;; =>
;;
;;  (fsm/get-name document-fsm) ;; => :acme/document-fsm
;;  (fsm/get-desc document-fsm) ;; => "Simple document FSM"
;;
;;
;;  (meta (f/to-fsm document-fsm))
;;
;;  (def fff (-> document-fsm
;;               f/to-fsm
;;               (f/init document)))
;;
;;  (def fff (to-fsm document-fsm))
;;
;;  (p/init (to-fsm document-fsm) document)
;;
;;  (def fsm (get-fsm :acme/document-fsm))
;;
;;
;;
;;
;;
;;
;;  (p/get-initial-state fsm) ;; => :document/unverified
;;  (p/get-name fsm) ;; => :acme/document-fsm
;;  (p/get-data fsm) ;; => {:document/name "Simple name", :document/author "John Doe"}
;;  (p/get impl/*fsm-registry (p/get-name fsm))
;;
;;  )
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;
;;(comment
;;  (meta document-fsm)
;;  (meta (to-fsm document-fsm))
;;  (impl/get-name (to-fsm document-fsm))
;;  (p/get-name (to-fsm document-fsm))
;;
;;  (binding [*print-meta* true]
;;    (prn (to-fsm document-fsm)))
;;
;;  (impl/init document-fsm))
;;
;;
;;
;;(defn with-fsm [fsm data]
;;  (with-meta
;;    data
;;    {::fsm fsm}))
;;
;;(comment
;;  (d/datafy document-fsm)
;;
;;  (def x (with-fsm document-fsm document))
;;  (meta x)
;;
;;  (meta (to-fsm document-fsm))
;;  (p/get-name document-fsm)
;;
;;  (p/get-initial-state (to-fsm document-fsm))
;;
;;  (init document-fsm document)
;;  (get-data (init document-fsm document))
;;
;;  (p/get-name document-fsm)
;;  (get-desc document-fsm)
;;  (get-current-state document-fsm)
;;  (on-enter document-fsm)
;;  (on-leave document-fsm)
;;  (on-error document-fsm)
;;  (get-states document-fsm)
;;  (get-events document-fsm)
;;  )

























(defn square [x]
  (* x x))


;;(defprotocol CustomerDatabase
;;  :extend-via-metadata true
;;  (get-customer-by-id [db id])
;;  (update-customer-by-id [db id data])
;;  (delete-customer-by-id [db id]))
;;
;;
;;(defn dynamic-delete-customer-by-id [db id]
;;  (try
;;    (delete-customer-by-id db id)
;;    (catch Exception _
;;      (println "Boom!")
;;      (when-not
;;        (get (meta db) 'clj-fsm.fsm/delete-customer-by-id)
;;        (println "Installing delete-customer-by-id onto db metadata")
;;        (alter-meta!
;;          db
;;          assoc
;;          'clj-fsm.fsm/delete-customer-by-id
;;          (fn [db id] (swap! db dissoc id)))
;;        (delete-customer-by-id db id)))))
;;
;;
;;(def mock-db
;;  (atom {} :meta {'clj-fsm.fsm/get-customer-by-id    (fn [db id] (get @db id))
;;                  'clj-fsm.fsm/update-customer-by-id (fn [db id data] (swap! db assoc id data))}))
;;
;;(identity @mock-db)
;;
;;(update-customer-by-id mock-db 123 {:name "Joe"})
;;(update-customer-by-id mock-db 456 {:name "Luke"})
;;(get-customer-by-id mock-db 123)
;;
;;(identity @mock-db)
;;
;;(get (meta mock-db) 'clj-fsm.fsm/delete-customer-by-id)
;;
;;(dynamic-delete-customer-by-id mock-db 123)
;;;;Boom!
;;;;Installing delete-customer-by-id onto db metadata
;;;;=> {456 {:name "Luke"}}
;;
;;(get (meta mock-db) 'clj-fsm.fsm/delete-customer-by-id)
;;
;;
;;
;;
;;(defprotocol Component
;;  :extend-via-metadata true
;;  (start [component]))
;;
;;(def component (with-meta {:name "db"} {`start (constantly "started")}))
;;
;;(start component)
;;
;;(def john-doe (with-meta
;;                {:name     "John Doe"
;;                 :language "us"}
;;                {`clojure.core.protocols/datafy (fn [x] (assoc x :type 'Person))}))
;;
;;(datafy/datafy john-doe)

;;
;;(def ^:private *state (atom (make-hierarchy)))
;;
;;(defn defstates [& args])
;;(defn defevents [& args])
;;
;;(defn document-checked? [])
;;
;;(defstates :unverified :initial
;;           :verified
;;           :published
;;           :archived
;;           :rejected)
;;
;;(defevents
;;  :verify [:unverified] :=> :verified :action :guard document-checked?
;;
;;  :reject
;;  [:unverified] :=> :rejected
;;
;;  :reverify
;;  [:verified] :=> :unverified
;;
;;  :publish
;;  [:verified] :=> :published
;;
;;  :unpublish
;;  [:published] :=> :verified
;;
;;  :archive
;;  [:published :verified :unverified] :=> :archived)






;;
;;(comment
;;  (def *state (atom (make-hierarchy)))
;;
;;  (identity @*state)
;;
;;  (reset! *state (derive @*state :conversation/read :conversation/unread))
;;  (reset! *state (derive @*state :conversation/closed :conversation/read))
;;
;;  ;; возращает прямых родителей
;;  ;; прямой родитель :conversation/closed - это #{:conversation/read}
;;  (parents @*state :conversation/closed)
;;
;;  ;; возвращает прямых и косвенных родителей
;;  ;; в :conversation/closed можно перейти только из #{:conversation/unread :conversation/read}
;;  (ancestors @*state :conversation/closed)
;;
;;  ;; возвращает прямых и косвенных потомков
;;  ;; из :conversation/unread можно перейти только в #{:conversation/closed :conversation/read}
;;  (descendants @*state :conversation/unread)
;;
;;  ;; проверка является ли child ребенком parent
;;  ;; является ли :conversation/closed ребенком :conversation/unread
;;  (isa? @*state :conversation/closed :conversation/unread)
;;  )
;;(comment
;;
;;
;;  (parents)
;;
;;  (descendants)
;;
;;  (ancestors)
;;
;;  (derive)
;;
;;  (underive)
;;
;;  (isa?)
;;
;;  (make-hierarchy))
