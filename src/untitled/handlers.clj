(ns untitled.handlers
  (:require [clojure.edn      :as edn]
            [untitled.crud_http    :refer :all]
            [compojure.core   :refer :all]
            [compojure.coercions :refer [as-int]])
  (:use     [hiccup.core]))


(defn http-edn-wrapper [edn]
  (-> edn
      :body
      slurp
      edn/read-string))


(defn page-404 [] {:status 404
                   :body "Not found."
                   :headers {"Content-Type" "text/plain"}})


(defn custom-page [id]
  (html [:h1 "Patients"]
        [:p (str "Patient Num: " id)]))


(defn order-view [request] (str "<h1>Request: ", request, "</h1>"))
(defn order-form [patient-id] (custom-page patient-id))
(defn order-save [patient-id] (str "<h1> ORDER SAVE Reques: ", patient-id, "</h1>"))


(defroutes myRoutes
           (context  "/api/v1/patients" []
                     (GET  "/" [] (list-patients))
                     (GET "/search" identity)
                     (GET "/create" identity)
                     (context "/:id" [id]
                              (GET  "/"  identity)
                              (PUT  "/update"  identity)
                              (DELETE  "/delete" [] (delete-patient (Integer/parseInt id)))))
           (page-404))