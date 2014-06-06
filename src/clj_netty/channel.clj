(ns clj-netty.channel
  (:require [clojure.core.async :refer [chan]]
            [clojure.core.async.impl.protocols :as impl])
  (:import [java.util LinkedList Queue]))

(deftype FixedBuffer [^LinkedList buf ^long n]
  impl/Buffer
  (full? [this]
    (= (.size buf) n))
  (remove! [this]
    (.removeLast buf))
  (add! [this itm]
    (assert (not (impl/full? this)) "Can't add to a full buffer")
    (.addFirst buf itm))
  clojure.lang.Counted
  (count [this]
    (.size buf))
  clojure.lang.IDeref
  (deref [this]
    (seq buf)))

(defn fixed-buffer [^long n]
  (FixedBuffer. (LinkedList.) n))

(def read-buf (fixed-buffer 128))
(def write-buf (fixed-buffer 128))
(def read-ch (chan read-buf))
(def write-ch (chan write-buf))
