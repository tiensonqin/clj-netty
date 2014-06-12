(ns clj-netty.handler
  (:require [clj-netty.channel :refer :all]
            [clojure.core.async :refer [>!!]]
            [cheshire.core :refer [generate-string]])
  (:import (io.netty.channel ChannelFutureListener ChannelHandler
                             ChannelHandler$Sharable
                             ChannelHandlerContext
                             ChannelInboundHandlerAdapter
                             SimpleChannelInboundHandler)
           (java.util.concurrent TimeUnit)
           (com.google.protobuf ByteString)))

(defn- reconnect [ctx host port]
  ((resolve 'clj-netty.isolate/reconnect) ctx host port))

;; (defn invoke
;;   [service method args]
;;   (try
;;     (apply (intern
;;             (symbol (str "clj-netty.services." service))
;;             (symbol method))
;;            args)
;;     (catch Exception e
;;       (prn (.getMessage e)))))

(defn ^ChannelHandler server-handler
  [custom-handler]
  (proxy [ChannelInboundHandlerAdapter ChannelHandler$Sharable] []
    (channelRead [^ChannelHandlerContext ctx ^Object msg]
      ;; (prn "Server received: " msg)
      (let [type (.getType msg)
            service (.getService msg)
            method (.getMethod msg)
            args (.getArgsList msg)
            result (custom-handler service method args)
            result (if (nil? result) "" result)]
        (when (zero? type)                ; sync call
          (.writeAndFlush ctx (.. (Rpc$Response/newBuilder) (setResult (ByteString/copyFromUtf8 (generate-string result))) build)))))
    (channelReadComplete [^ChannelHandlerContext ctx]
      ;; (.. ctx
      ;;     (writeAndFlush Unpooled/EMPTY_BUFFER)
      ;;     (addListener ChannelFutureListener/CLOSE))
      )
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printStackTrace cause)
      (.close ctx))))

(defn ^ChannelHandler client-handler [host port]
  (proxy [SimpleChannelInboundHandler ChannelHandler$Sharable] []
    (channelActive [^ChannelHandlerContext ctx])

    (channelRead0 [^ChannelHandlerContext ctx ^Object in]
      ;; (prn "Client received: " in)
      (>!! read-ch in))
    (channelInactive [^ChannelHandlerContext ctx]
      ;; (reconnect ctx host port)
      (reconnect ctx host port))
    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (.printSTackTrace cause)
      (.close ctx))))
