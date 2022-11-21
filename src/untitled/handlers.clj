(ns untitled.handlers
  (:require
    [compojure.core     :refer :all]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [untitled.crud_http :refer :all]
    [compojure.route :as route]
    [compojure.coercions :refer :all]
    [ring.middleware.json :refer [wrap-json-response]]
    [ring.middleware.json :refer :all]
    [clojure.java.io :as io]
    [ring.util.response :as resp])
  (:use     [hiccup.core]))

(defn wrap-empty-params [handler]
  (fn [request] (handler (:params request))))

(defroutes my-routes
           (GET "/" [] (resp/redirect "/index.html"))
           (route/resources "/" {:root "public"})
           (ANY "/health" _ "ok")
           (context  "/api/v1/patients" request
             (GET  "/" [] (list-patients))
             (POST "/" [] (add-patient (:params request)))
             (GET "/search" [] (validate-search-patients (:params request)))
             (context "/:id" [id :<< as-int]
               (GET "/" [] (get-patient id))
               (PUT "/" [] (update-patient id (:params request)))
               (DELETE  "/" [] (delete-patient id))))
           (route/not-found "page-404"))

(def app
  (-> #'my-routes ;; use var as function (fn [request] (my-routes request))
      (wrap-json-response)
      (wrap-keyword-params)
      (wrap-params)))