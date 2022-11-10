(ns untitled.handlers
  (:require
    [compojure.core     :refer :all]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [untitled.crud_http :refer :all]
    [compojure.route :as route]
    [compojure.coercions :refer :all]
    [ring.middleware.json :refer [wrap-json-response]]
    [ring.middleware.json :refer :all])
  (:use     [hiccup.core]))

(defroutes my-routes
           (ANY "/health" _ "ok")
           (context  "/api/v1/patients" request
             (GET  "/" [] (list-patients))
             (GET "/search" [] (validate-search-patients (:params request)))
             (GET "/create" []  "create method")
             (context "/:id" [id :<< as-int]
               (GET "/" [] (get-patient id))
               (PUT  "/" [] "ok")
               (DELETE  "/" [] (delete-patient id))))
           (route/not-found "page-404"))

(def app
  (-> (fn [request] (my-routes request))
      (wrap-json-response)
      (wrap-keyword-params)
      (wrap-params)))