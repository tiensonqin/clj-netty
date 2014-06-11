(ns clj-netty.services.redis
  (:refer-clojure :exclude [get set])
  (:require [com.netflix.hystrix.core :refer [defcommand]]
            [taoensso.carmine :as car :refer (wcar)]
            [clj-netty.service :refer [instance]]))

(defonce service-name "redis")

(defn conn
  []
  (let [parts (clojure.string/split (.buildUriSpec (instance service-name)) #":")]
    {:pool {}
     :spec {:host (clojure.string/replace (second parts) "//" "")
            :port (read-string (last parts))}}))

(defmacro wcar* [& body] `(car/wcar ~(conn) ~@body))

(defcommand get
  [key]
  (wcar* (car/get key)))

(defcommand set
  [& key-val]
  (wcar* (apply car/set key-val)))
