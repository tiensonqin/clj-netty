(ns clj-netty.server
  (:require [clj-netty.handler :refer [server-handler]]
            [clj-netty.initializer :refer [server-channel-initializer]])
  (:import (io.netty.bootstrap ServerBootstrap)
           (io.netty.channel ChannelOption)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioServerSocketChannel)
           (java.net InetSocketAddress)))

(defn start [port custom-handler]
  (let [boss-group (NioEventLoopGroup.)
        worker-group (NioEventLoopGroup.)]
    (try
      (let [b (ServerBootstrap.)]
        (.. b
            (group boss-group worker-group)
            (channel NioServerSocketChannel)
            (localAddress (InetSocketAddress. port))
            (childHandler (server-channel-initializer (server-handler custom-handler)))
            (option (ChannelOption/SO_BACKLOG) (int 128))
            (option (ChannelOption/SO_REUSEADDR) true)
            (childOption (ChannelOption/SO_KEEPALIVE) true))
        (let [f (.. b bind sync)]
          (prn "Netty started and listen on " (.. f channel localAddress))
          (.. f channel closeFuture sync)))
      (catch Exception e
        (prn e))
      (finally
        (.. worker-group shutdownGracefully sync)
        (.. boss-group shutdownGracefully sync)))))
