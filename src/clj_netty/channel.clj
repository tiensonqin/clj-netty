(ns clj-netty.channel
  (:require [clojure.core.async :refer [go chan]]))

(defonce read-ch (chan 128))
(defonce write-ch (chan 128))
