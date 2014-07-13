(ns clj-netty.client
  (:refer-clojure :exclude [sync])
  (:require [clj-netty.initializer :refer [client-channel-initializer]]
            [clj-netty.isolate :refer :all]
            [clj-netty.handler :refer [client-handler]]
            [clj-netty.channel :refer :all]
            [clojure.core.async :refer [go chan >!! <!! <! >! alts!! timeout]]
            [cheshire.core :refer [parse-string generate-string]])
  (:import (io.netty.bootstrap Bootstrap)
           (io.netty.channel ChannelOption)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioSocketChannel)
           (java.net InetSocketAddress)
           (com.google.protobuf ByteString)))

(defn do-write
  [channel req]
  (when (.isActive channel)
    (.writeAndFlush channel req)))

(defn build-msg
  [type service method args]
  (.. (Rpc$Request/newBuilder)
      (setType type)
      (setService service)
      (setMethod method)
      (setArgs (ByteString/copyFromUtf8 (generate-string args)))
      build))

(defn write!
  [channel type service method args]
  (let [req (build-msg type service method args)]
    (do-write channel req)))

(defn read! []
  (when-let [msg (first (<!! (go (alts!! [read-ch (timeout 300)]))))]
    (parse-string (.toStringUtf8 (.getResult msg)))))

(defn sync-call
  [channel service method args]
  (write! channel 0 service method args)
  (read!))

(defn async-call
  [channel service method args]
  (write! channel 1 service method args)
  nil)

(defn connect [host port]
  (.channel (start-client host port)))
