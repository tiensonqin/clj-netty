(ns clj-netty.handler
  (:require [clojure.core.async :refer [>!!]]
            [clj-netty.channel :refer :all])
  (:import (io.netty.channel ChannelFutureListener ChannelHandler
                             ChannelHandler$Sharable
                             ChannelHandlerContext
                             ChannelInboundHandlerAdapter
                             SimpleChannelInboundHandler)
           (io.netty.util CharsetUtil)
           com.tiensonqin.redis.Redis$RedisResponse))

(defn ^ChannelHandler server-handler []
  (proxy [ChannelInboundHandlerAdapter ChannelHandler$Sharable] []
    (channelRead [^ChannelHandlerContext ctx ^Object msg]
      (prn "Server received: " msg)
      (.writeAndFlush ctx (.. (com.tiensonqin.redis.Redis$RedisResponse/newBuilder) (addResult "world") build)))
    (channelReadComplete [^ChannelHandlerContext ctx]
      ;; (.. ctx
      ;;     (writeAndFlush Unpooled/EMPTY_BUFFER)
      ;;     (addListener ChannelFutureListener/CLOSE))
      )
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printStackTrace cause)
      (.close ctx))))

(defn ^ChannelHandler client-handler []
  (proxy [SimpleChannelInboundHandler ChannelHandler$Sharable] []
    (channelActive [^ChannelHandlerContext ctx])

    (channelRead0 [^ChannelHandlerContext ctx ^Object in]
      (prn "Client received: " in)
      (>!! read-ch in))

    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printSTackTrace cause)
      (.close ctx))))
