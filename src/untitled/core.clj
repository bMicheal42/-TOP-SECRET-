(ns untitled.core
  (:gen-class)
  (:require
            [ring.adapter.jetty :as ring]
            [migratus.core      :as migratus]
            [compojure.core     :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [untitled.handlers :refer [myRoutes]]
            [untitled.db :refer :all]
            [untitled.crud_http :refer :all]
            [untitled.handlers :refer :all]
            [dotenv :refer [env app-env]]
            )
  (:use [hiccup.core]))


;; DONE 1. CRUD (UPDATE)
;; DONE: validate that there is no unnecesarry keys.
;; DONE 2. SQLite -> Postgres
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
    (.stop s)
    (reset! server nil)))


(def app
  (-> (fn [request] (myRoutes request))
      wrap-keyword-params
      wrap-params))

(defn start [port]
  (ring/run-jetty (fn [req] (app req))
                  {:port port
                   :join? false}))

(defn -main [& args]
  (migratus/migrate config)
  (reset! server
         (let [port (or (Integer/parseInt (env :PORT)) 80)]
           (start port))))
