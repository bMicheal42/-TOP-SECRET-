(ns untitled.crud_http
  "all functions return http format maps with keys (status, headers, body)"
  (:require [clojure.java.jdbc :as j]
            [compojure.core :refer :all]
            [ring.util.http-response :refer :all]
            [untitled.db :refer [db]]
            [untitled.crud_util :as util]))

;; READ
(defn list-patients []
  (ok (util/list-patients)))


;; SEARCH / FILTER
(defn validate-search-patients [query]
  (let [valid-errors (util/false-validations query (select-keys util/search-schema (map first query)))]
    (if (empty? valid-errors)
      (ok (util/search-patients query))
      (bad-request {:error-type :invalid-keys
                    :value valid-errors}))))


;; GET patient by id
(defn get-patient [id]
  (if-let [patient (util/get-patient id)]
    (ok patient)
    (bad-request "fail")))


;; CREATE PATIENT
(defn add-patient "description" [patient]
  (let [valid-errors (util/false-validations patient)]
    (if (empty? valid-errors)
      (if (empty? (util/search-patients patient))
       (do
         (let [new-patient (first (j/insert! db :patients patient))]
           (created "/patient" new-patient)))
       (bad-request {:error-type :already-exists}))
      (bad-request {:error-type :invalid-keys
                    :value      valid-errors}))))


;; DELETE
(defn delete-patient [id]
  "Deletes a patient given an medical policy. Returns true if deletion was successful."
  (if (not (= 0 (first (j/delete! db :patients ["id = ?" id]))))
    (found "/")
    (bad-request "fail")))


; UPDATE
(defn update-patient [id update]
  (if (util/update-patient id update)
    (ok)
    (bad-request "fail")))
