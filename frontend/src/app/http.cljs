(ns app.http
  (:require [cljs-http.client :as http]
            [clojure.core.async :refer [<! go]]
            [app.atoms :refer [patients search-params editable editable-params update-button-name
                               wrong-keys]]
            [app.utils :refer [parse-patients-dates render-patient! clear-search-atoms]]))

;; ______________ HTTP METHODS ______________
(defn get-patients []
  (go (reset! patients (parse-patients-dates (:body (<! (http/get "/api/v1/patients")))))))

(defn update-patient []
  (go
    (let [res (<! (http/put (str "/api/v1/patients/" @editable) {:query-params @editable-params}))]
      (if (= 200 (:status res))
        (do
          (render-patient!)
          (reset! update-button-name "Update")
          (reset! wrong-keys nil)
          (reset! editable nil)
          (reset! editable-params nil))
        (when (= "invalid-keys" (str (:error-type (:body res))))
          (reset! wrong-keys (:value (:body res))))))))

(defn add-patient [new-patient]
  (go
    (let [res (<! (http/post "/api/v1/patients/" {:form-params new-patient}))]
      (when (= 201 (:status res))
        (get-patients))
      (when (= "already-exists" (:error-type (:body res)))
        (js/alert "User with such policy already exists!"))
      (when (= "invalid-keys" (:error-type (:body res)))
          (js/alert (str "Invalid keys: " (clojure.string/join ", " (:value (:body res)))))))))

(defn delete-patient [id]
  (go
    (let [res (<! (http/delete (str "/api/v1/patients/" id)))]
      (when (= 200 (:status res))
        (get-patients)))))

(defn search-patients [query]
  (go
    (let [res (<! (http/get "/api/v1/patients/search"
                   {:query-params query}))]
      (when (= 200 (:status res))(reset! patients (parse-patients-dates (:body res)))))))