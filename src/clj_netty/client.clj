(ns clj-netty.client
  (:refer-clojure :exclude [sync])
  (:require [clj-netty.initializer :refer [client-channel-initializer]]
            [clj-netty.isolate :refer :all]
            [clj-netty.handler :refer [client-handler]]
            [clj-netty.channel :refer :all]
            [clojure.core.async :refer [go chan >!! <!! <! >! alts!! timeout]]
            [clojure.tools.nrepl :as handler]
            [cheshire.core :refer [parse-string]])
  (:import (io.netty.bootstrap Bootstrap)
           (io.netty.channel ChannelOption)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioSocketChannel)
           (java.net InetSocketAddress)))

(defn do-write
  [channel req]
  (.writeAndFlush channel req))

(defn build-msg
  [type service method args]
  (.. (Rpc$Request/newBuilder)
      (setType type)
      (setService service)
      (setMethod method)
      (addAllArgs args)
      build))

(defn write!
  [type service method args]
  (let [req (build-msg type service method args)]
    (go (>! write-ch req))))

(defn read! []
  (when-let [msg (first (<!! (go (alts!! [read-ch (timeout 300)]))))]
    (parse-string (.toStringUtf8 (.getResult msg)))))

(defn sync-call
  [service method args]
  (write! 0 service method args)
  (read!))

(defn async-call
  [service method args]
  (write! 1 service method args))

(defn connect [host port]
  (let [c (start-client host port)]
    (go
      (loop []
        (when (.isActive (.channel @client))
          (let [req (<! write-ch)]
            (do-write (.channel @client) req)))
        (recur)))))
