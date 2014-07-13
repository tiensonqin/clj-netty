(defproject clj-netty "0.1.0-SNAPSHOT"
  :description "Clojure rpc based on netty and core.async"
  :url "http://github.com/tiensonqin/clj-netty"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [io.netty/netty-all "4.0.19.Final"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [cheshire "5.3.1"]
                 [com.google.protobuf/protobuf-java "2.5.0"]]
  ;; :plugins [[lein-protobuf "0.4.1"]]
  :main clj-netty.core
  :aot [clj-netty.core]
  :java-source-paths ["src/jvm"])
