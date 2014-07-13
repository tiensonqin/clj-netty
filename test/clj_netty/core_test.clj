(ns clj-netty.core-test
  (:refer-clojure :exclude [sync])
  (:require [clojure.test :refer :all]
            [clj-netty.core :refer :all]))

(defn divide
  [service method args]
  (try
    (apply / args)
    (catch Exception e
      (prn (.getMessage e)))))

(deftest hystrix
  (let [_ (future (start-server 5555 divide))
        client (listen "localhost" 5555)]
    (are [x y] (= x y)
         5 (sync client "test" "divide" [10 2])
         nil (sync client "test" "divide" [10 0]))))
