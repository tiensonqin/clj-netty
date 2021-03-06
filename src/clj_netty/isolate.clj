(ns clj-netty.isolate
  (:require [clj-netty.handler :refer [client-handler]]
            [clj-netty.initializer :refer [client-channel-initializer]])
  (:import (io.netty.bootstrap Bootstrap)
           (io.netty.channel ChannelFutureListener ChannelOption)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioSocketChannel)
           (java.net InetSocketAddress)
           (java.util.concurrent TimeUnit)))

(declare start-client)

(defn reconnect
  [ctx host port]
  (.. ctx
      channel
      eventLoop
      (schedule (reify Runnable
                  (run [this]
                    (start-client host port)))
                (long 1)
                TimeUnit/SECONDS)))

(defn reconnect-listener
  [host port]
  (reify
    ChannelFutureListener
    (operationComplete [this f]
      (when (not (.isSuccess f))
        ;; (prn "Reconnect")
        (reconnect f host port)))))

(defn start-client
  [host port]
  (try
    (.. (Bootstrap.)
        (group (NioEventLoopGroup.))
        (channel NioSocketChannel)
        (remoteAddress (InetSocketAddress. host port))
        (handler (client-channel-initializer (client-handler host port)))
        (option (ChannelOption/SO_KEEPALIVE) true)
        connect
        (addListener (reconnect-listener host port)))
    ;; (finally
    ;;   (.. group shutdownGracefully sync))
    (catch Exception e
      (prn e))))
