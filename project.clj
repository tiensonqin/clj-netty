(defproject clj-netty "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [io.netty/netty-all "4.0.19.Final"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [com.netflix.hystrix/hystrix-clj "1.4.0-RC4"]
                 [org.flatland/protobuf "0.8.1"]
                 [com.taoensso/carmine "2.6.0"]
                 [cheshire "5.3.1"]
                 [curator "0.0.2"]]
  :plugins [[lein-protobuf "0.4.1"]]
  :java-source-paths ["src/jvm"]
  :main clj-netty.core
  :aot [clj-netty.core])
