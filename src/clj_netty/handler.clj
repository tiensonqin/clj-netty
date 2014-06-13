(ns clj-netty.handler
  (:require [clj-netty.channel :refer :all]
            [clojure.core.async :refer [>!!]]
            [cheshire.core :refer [generate-string parse-string]])
  (:import (io.netty.channel ChannelFutureListener ChannelHandler
                             ChannelHandler$Sharable
                             ChannelHandlerContext
                             ChannelInboundHandlerAdapter
                             SimpleChannelInboundHandler)
           (java.util.concurrent TimeUnit)
           (com.google.protobuf ByteString)))

(defn- reconnect [ctx host port]
  ((resolve 'clj-netty.isolate/reconnect) ctx host port))

;; TODO fixed issue of non sharable
(defn ^ChannelHandler server-handler
  [custom-handler]
  (proxy [ChannelInboundHandlerAdapter] []
    (channelRead [^ChannelHandlerContext ctx ^Object msg]
      ;; (prn "Server received: " msg)
      (let [type (.getType msg)
            service (.getService msg)
            method (.getMethod msg)
            args (parse-string (.toStringUtf8 (.getArgs msg)))
            result (custom-handler service method args)]
        (when (zero? type)                ; sync call
          (.writeAndFlush ctx (.. (Rpc$Response/newBuilder) (setResult (ByteString/copyFromUtf8 (generate-string result))) build)))))
    (channelReadComplete [^ChannelHandlerContext ctx]
      ;; (.. ctx
      ;;     (writeAndFlush Unpooled/EMPTY_BUFFER)
      ;;     (addListener ChannelFutureListener/CLOSE))
      )
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printStackTrace cause)
      (.close ctx))
    (isSharable [] true)))

(defn ^ChannelHandler client-handler [host port]
  (proxy [SimpleChannelInboundHandler] []
    (channelActive [^ChannelHandlerContext ctx])

    (channelRead0 [^ChannelHandlerContext ctx ^Object in]
      ;; (prn "Client received: " in)
      (>!! read-ch in))
    (channelInactive [^ChannelHandlerContext ctx]
      ;; (reconnect ctx host port)
      (reconnect ctx host port))
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printSTackTrace cause)
      (.close ctx))
    (isSharable [] true)))
