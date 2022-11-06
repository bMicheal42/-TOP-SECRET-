(defproject untitled "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[migratus-lein "0.7.3"]]
  :dependencies [[lynxeyes/dotenv "1.1.0"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.xerial/sqlite-jdbc "3.39.3.0"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [metosin/malli "0.9.2"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/reitit "0.5.18"]
                 [metosin/ring-http-response "0.9.3"]
                 [org.postgresql/postgresql "42.5.0"]
                 [migratus "1.4.5"]
                 [compojure "1.6.3"]
                 [funcool/struct "1.3.0"]
                 [hiccup "1.0.5"]
                 [fogus/ring-edn "0.3.0"]
                 ]
  :repl-options {:init-ns untitled.core}
  :main untitled.core
)
