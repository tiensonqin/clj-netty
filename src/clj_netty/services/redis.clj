(ns clj-netty.services.redis
  (:refer-clojure :exclude [get set])
  (:require [com.netflix.hystrix.core :refer [defcommand]]
            [taoensso.carmine :as car :refer (wcar)]
            [clj-netty.service :refer [instance unregister]]))

(defonce service-name "redis")

(defn conn
  [inst]
  (let [parts (clojure.string/split (.buildUriSpec inst) #":")]
    {:pool {}
     :spec {:host (clojure.string/replace (second parts) "//" "")
            :port (read-string (last parts))}}))

(defmacro wcar* [inst & body] `(car/wcar (conn ~inst) ~@body))

(defcommand hystrix-get
  {:hystrix/fallback-fn (fn [inst key]
                          ;; unregister service
                          (unregister inst)
                          ;; TODO logger

                          ;; recall it
                          (get key)
                          )}
  [inst key]
  (wcar* inst (car/get key)))

(defn get
  [key]
  (when-let [inst (instance service-name)]
    (hystrix-get inst key)))

(defcommand set
  [& key-val]
  (wcar* (apply car/set key-val)))
