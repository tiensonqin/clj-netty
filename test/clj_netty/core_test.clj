(ns clj-netty.core-test
  (:refer-clojure :exclude [sync])
  (:require [clojure.test :refer :all]
            [clj-netty.core :refer :all]
            [com.netflix.hystrix.core :refer [defcommand]]))

(defcommand divide
  {:hystrix/fallback-fn (fn [service method args]
                          (prn service method args))}
  [service method args]
  (apply / args))

(future (start-server 5555 divide))

(def client (listen "localhost" 5555))

(deftest hystrix
  (are [x y] (= x y)
       5 (sync client "test" "divide" [10 2])
       nil (sync client "test" "divide" [10 0])))
