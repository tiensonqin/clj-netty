(ns clj-netty.handler
  (:require [clojure.core.async :refer [>!!]]
            [clj-netty.channel :refer :all]
            [clj-netty.services.redis])
  (:import (io.netty.channel ChannelFutureListener ChannelHandler
                             ChannelHandler$Sharable
                             ChannelHandlerContext
                             ChannelInboundHandlerAdapter
                             SimpleChannelInboundHandler)
           (io.netty.util CharsetUtil)
           Rpc$Request
           Rpc$Response))

;; TODO remove hard-coded namespace
(defn invoke
  [service method args]
  (apply (intern (symbol (str "clj-netty.services." service))
                 (symbol method))
         args))

(defn ^ChannelHandler server-handler []
  (proxy [ChannelInboundHandlerAdapter ChannelHandler$Sharable] []
    (channelRead [^ChannelHandlerContext ctx ^Object msg]
      (prn "Server received: " msg)
      (let [type (.getType msg)
            service (.getService msg)
            method (.getMethod msg)
            args (.getArgsList msg)
            result (invoke service method args)]
        (when (zero? type)                ; sync call
          (.writeAndFlush ctx (.. (Rpc$Response/newBuilder) (addResult result) build)))))
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
