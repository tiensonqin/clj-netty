(ns clj-netty.core
  (:require [clj-netty.server :refer [start-server]]))

(defn -main []
  (future (start-server 8080)))
