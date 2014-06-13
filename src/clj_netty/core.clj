(ns clj-netty.core
  (:refer-clojure :exclude [sync])
  (:require [clj-netty.server :refer [start]]
            [clj-netty.client :refer [sync-call async-call connect]]))

(defn start-server
  [port handler]
  (start port handler))

(defn listen
  [host port]
  (connect host port))

(defn sync
  [channel service method args]
  (sync-call channel service method args))

(defn async
  [channel service method args]
  (async-call channel service method args))
