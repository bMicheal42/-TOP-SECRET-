(ns untitled.crud_http
  "all functions return http format maps with keys (status, headers, body)"
  (:require [clojure.java.jdbc :as j]
            [compojure.core :refer :all]
            [ring.util.http-response :refer :all]
            [untitled.db :refer [db]]
            [untitled.crud_util :as util])
  (:import
           (java.time LocalDate)
           (java.sql Date)
           (java.time.format DateTimeParseException)))


;; READ
(defn list-patients []
  (ok (util/list-patients)))


;; SEARCH & FIL
(defn validate-search-patients [query]
  (let [valid-errors (util/false-validations query (select-keys util/search-schema (map first query)))]
    (if (empty? valid-errors)
      (ok (util/search-patients query))
      (bad-request {:error-type :invalid-keys
                    :value valid-errors}))))


;; GET patient by id
(defn get-patient [id]
  (if-let [patient (first (util/search-patients {:id id}))]
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
  (let [p (get-patient id)]
    (when (and (not (empty? update)) p (empty? (util/false-validations (merge (:body p) update))))
      (j/update! db :patients update ["id = ?" id]))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment

  ;; VALIDATE

  (validate-map patient)

  (validate-map query {:sex is-sex?})


  (validate-map (merge Igor {:date {:method :less :value (date "2000-01-01")}})
                )

  {:errors [:birthdate :sex]}

   ;; Все збс
   nil
   ;; add, delete, update, delete
   {:error-type :invalid-keys
    :value      [:birthdate :sex]}
   ;; add
   {:error-type :already-exists}

   ;; internal validate for create
   ;(defn- valid-add? [patient]
   ;  (and
   ;    ((false-validations patient))
   ;;    (empty? (search-patients (select-keys patient [:id])))))
   ;
   ;{:error-type}
   ;
   ;{:ok false
   ; :value '()}

   )