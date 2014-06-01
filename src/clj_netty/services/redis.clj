(ns clj-netty.services.redis)

(def service "redis")

(defn get
  [key]
  (prn key)
  key)

(defn set
  [& key-vals]
  nil)
