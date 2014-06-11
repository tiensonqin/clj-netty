(ns clj-netty.service
  (:require [curator.framework :refer [curator-framework]]
            [curator.discovery :refer (service-discovery
                                       service-instance
                                       service-provider
                                       note-error
                                       round-robin-strategy
                                       random-strategy
                                       sticky-strategy) :as cd]))

(defonce ^:private client (let [c (curator-framework "localhost:2181")]
                            (.start c)
                            c))

(defonce ^:private discovery (let [d (service-discovery client :base-path "/huaban")]
                               (.start d)
                               d))

(def ^:private providers (atom {}))

(defmulti strategy identity)
(defmethod strategy "redis" [_] (round-robin-strategy))

(defn provider
  [service-name]
  (if-let [p (@providers service-name)]
    p
    (let [p (service-provider discovery service-name :strategy (strategy service-name))]
      ;; start provider
      (.start p)
      (swap! providers #(assoc % service-name p))
      p)))

(defn instance-builder
  [name host port opts]
  (apply service-instance name host port opts))

(defn services
  []
  (cd/services discovery))

(defn instances
  [service-name]
  (cd/instances discovery service-name))

(defn instance
  [service-name]
  (cd/instance (provider service-name)))

(defn register
  [name host port & opts]
  (.registerService discovery (instance-builder name host port opts)))

(defn unregister
  [instance]
  (.unregisterService discovery instance))
