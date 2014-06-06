(ns clj-netty.services.redis
  (:refer-clojure :exclude [get set])
  (:require [com.netflix.hystrix.core :refer [defcommand]]
            [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {}
                   :spec {:host "127.0.0.1"
                          :port 6379}})

(defmacro wcar* [conn & body] `(car/wcar '~conn ~@body))

(defcommand get
  [key]
  (wcar* server1-conn
         (car/get key)))

(defcommand set
  [& key-val]
  (wcar* server1-conn
         (apply car/set key-val)))
