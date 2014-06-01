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
           Rpc$Request))

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
    (.writeAndFlush channel req)))

(defn write!
  [type service method args]
  (let [req (.. (Rpc$Request/newBuilder)
                (setType type)
                (setService service)
                (setMethod method)
                (addArgs args)
                build)]
    (go (>! write-ch req))))

(defn read! []
  (when-let [msg (first (<!! (go (alts!! [read-ch (timeout 1000)]))))]
    (.getResultList msg)))

(defn call
  [type service method args]
  (write! type service method args))

(defn client [host port]
  (go
    (loop [client (start-client host port)]
      (let [req (<! write-ch)]
        ;; (prn req)
        (do-write (.channel client) req))
      (recur client))))
