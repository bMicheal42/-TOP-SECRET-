(ns untitled.core
  (:gen-class)
  (:require [clojure.java.jdbc  :as j]
            [clojure.edn :as edn]
            [ring.adapter.jetty :as ring]
            [migratus.core      :as migratus]
            [compojure.core     :refer :all]
            [compojure.route    :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.edn :as ring-edn]
            [untitled.handlers :refer [myRoutes]]
            [untitled.db :refer :all]
            [untitled.crud_http :refer :all]
            [untitled.handlers :refer :all]
            [dotenv :refer [env]]
            )
  (:use [hiccup.core]))


;; DONE 1. CRUD (UPDATE)
;; FIXME: validate that there is no unnecesarry keys.
;; TODO 2. SQLite -> Postgres
;; TODO 3. TESTS
;; DONE 4. API
;; TODO 4.1 AUTH
;; TODO 5. FRONT
;; DONE 6. CI/CD
;; DONE 7. K8s
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce server (atom nil))

(defn stop-server []
  (when-some [s @server]
    (.stop (:jetty s))
    (reset! server nil)))


(def app
  (-> (fn [request] (myRoutes request))
      wrap-keyword-params
      wrap-params))

(defn -main [& args]
  (swap! server
         assoc
         :jetty
         (ring/run-jetty (fn [req] (app req))
                          {:port (Integer/parseInt (env :PORT))
                           :join? false})))
