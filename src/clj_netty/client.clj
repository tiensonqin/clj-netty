(ns clj-netty.client
  (:require [clj-netty.handler :refer [client-handler]]
            [clj-netty.initializer :refer [client-channel-initializer]]
            [clj-netty.channel :refer :all]
            [clojure.core.async :refer [go chan >!! <!! <! >! alts!! timeout]])
  (:import (io.netty.bootstrap Bootstrap)
           (io.netty.buffer ByteBuf ByteBufUtil Unpooled)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioSocketChannel)
           (java.net InetSocketAddress)
           (io.netty.util CharsetUtil)
           com.tiensonqin.redis.Redis$RedisGetRequest))

(defn- start-client [host port]
  (try
    (.. (Bootstrap.)
        (group (NioEventLoopGroup.))
        (channel NioSocketChannel)
        (remoteAddress (InetSocketAddress. host port))
        (handler (client-channel-initializer client-handler))
        connect
        sync)
    ;; (finally
    ;;   (.. group shutdownGracefully sync))
    (catch Exception e
      (prn e))
    ))

(defn do-write
  [channel req]
  (when (.isOpen channel)
    (.writeAndFlush channel (.. (com.tiensonqin.redis.Redis$RedisGetRequest/newBuilder) (setKey req) build))))

(defn write!
  [req]
  (go (>! write-ch req)))

(defn read! []
  (first (<!! (go (alts!! [read-ch (timeout 1000)])))))

(defn client [host port]
  (go
    (loop [client (start-client host port)]
      (let [req (<! write-ch)]
        ;; (prn req)
        (do-write (.channel client) req))
      (recur client))))
