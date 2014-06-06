(ns clj-netty.client
  (:require [clj-netty.initializer :refer [client-channel-initializer]]
            [clj-netty.isolate :refer :all]
            [clj-netty.handler :refer [client-handler]]
            [clj-netty.channel :refer :all]
            [clojure.core.async :refer [go chan >!! <!! <! >! alts!! timeout]]
            [clojure.tools.nrepl :as handler])
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
  (when-let [msg (first (<!! (go (alts!! [read-ch (timeout 1000)]))))]
    (.getResultList msg)))

(defn connect [host port]
  (let [c (start-client host port)]
    (go
      (loop []
        (when (.isActive (.channel @client))
          (let [req (<! write-ch)]
            (do-write (.channel @client) req)))
        (recur)))))

;; sync && async invoke
;; (defn sync-call
;;   [service method args]
;;   (let [req (build-msg 0 service method args)]
;;     ))
