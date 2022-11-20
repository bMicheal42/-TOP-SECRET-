(ns untitled.core
  (:gen-class)
  (:require
            [ring.adapter.jetty :as ring]
            [migratus.core      :as migratus]
            [compojure.core     :refer :all]
            [untitled.db :refer :all]
            [untitled.crud_http :refer :all]
            [untitled.handlers :refer :all]
            [dotenv :refer [env app-env]]
            [compojure.coercions :refer :all]
            [ring.middleware.json :refer :all])
  (:use [hiccup.core]))


;; DONE 1. CRUD (UPDATE)
;; DONE: validate that there is no unnecesarry keys.
;; DONE 2. SQLite -> Postgres
;; TODO 3. TESTS
;; DONE 4. API
;; DONE 5. FRONT
;; DONE 6. CI/CD
;; DONE 7. K8s

(defonce server (atom nil))

(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main [& args]
  (migratus/migrate config)
  (reset! server
         (let [port (or (Integer/parseInt (env :PORT)) 80)]
           (ring/run-jetty #'app
                           {:port port
                            :join? false}))))
