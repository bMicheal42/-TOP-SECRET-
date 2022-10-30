(ns untitled.core
  (:gen-class)
  (:require [clojure.java.jdbc :as j]
            [ring.adapter.jetty :as ring])
  (:import (java.time LocalDate)
           (java.time.format DateTimeParseException)))


;; Global db
(def db {:dbtype "postgresql"
         :dbname "gke_test_regional"
         :host "35.246.185.186"
         :port "5432"
         :user "postgres"
         :password "password"})

;; global keywords
(def keywords [:address :sex :birthdate :full_name :medical_policy])

;; READ
(defn list-patients  []
  (j/query db ["select * from patients"]))

;; FILTER SUB-FUNCTION
(defn- birthdate-search-method [query]
  (let [birthdate (:birthdate query)]
    (cond
      (map? birthdate) ({:less "<" :more ">" :equals "="} (:method birthdate))
      (string? birthdate) "=")))

;; SEARCH & FILTER
;; (by birthday)
(defn search-patients  [params]
  (j/query db
           (do
             (def query
               [(->>
                  (for [keyword keywords]
                    (when (keyword params)
                      (str
                        (name keyword)
                        " "
                        (if (= keyword :birthdate) (birthdate-search-method params) "=")
                        " ?")))
                  (apply vector)
                  (remove nil?)
                  (interpose " and ")
                  (apply str)
                  (str "select * from patients where ")
                  )])

             (def values
               (->>
                 (for [keyword keywords]
                   (when (keyword params)
                     (if (= keyword :birthdate)
                       (let [birthdate (:birthdate params)]
                         (if (map? birthdate)
                           (:value birthdate)
                           birthdate))
                       (keyword params))))
                 (remove nil?)
                 (into [])))

             (def full (concat query values))

             full)))

(defn get-patient [medical_policy]
  (first (search-patients {:medical_policy medical_policy})))

;; VALIDATE
;; TODO: return in format {:valid false :reason "Invalid address."}
(defn valid? [query] "Validates query. Returns nil if query is invalid."
  (and
    (or (= (:sex query) "male") (= (:sex query) "female"))
    (try (LocalDate/parse (:birthdate query))
         (catch DateTimeParseException _ nil))
    (:address query)
    (:full_name query)
    (:medical_policy query)))


;; INTERNAL VALIDATE ADD
(defn- valid-add? [query]
  (and
    (valid? query)
    (empty? (search-patients (select-keys query [:medical_policy])))))


;; CREATE PATIENT
(defn add-patient "description" [patient]
  (if (and
        (valid-add? patient)
        (empty? (search-patients patient)))
    (do
      (j/insert! db :patients {:address        (patient :address)
                               :sex            (patient :sex)
                               :birthdate      (patient :birthdate)
                               :full_name      (patient :full_name)
                               :medical_policy (patient :medical_policy)})
      true)))


;; DELETE
(defn delete-patient [medical_policy]
  "Deletes a patient given an medical policy. Returns true if deletion was successful."
  (not (= 0 (first (j/delete! db :patients ["medical_policy = ?" medical_policy])))))

;; UPDATE
(defn update-patient [medical_policy update]
  (let [p (get-patient medical_policy)]
    (when (and (not (empty? update)) p (valid? (merge p update)))
      (j/update! db :patients update ["medical_policy = ?" medical_policy]))))


;; DONE 1. CRUD (UPDATE)
;; TODO 2. SQLite -> Postgres
;; TODO 3. TESTS
;; TODO 4. API (Authorization)
;; TODO 5. FRONT
;; DONE 6. CI/CD
;; DONE 7. K8s
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "<h1> TEST TEST TEST </h1>"})

(defn -main [& args]
  (ring/run-jetty handler {:port 80 :join? false}))
;(comment
;  (list-patients)
;
;  (update-patient 123 nil)
;
;  (print p)
;
;  (get-patient 123 )
;
;  (get-patient 12234)
;
;  (merge p {:sex "female"}))
;
;(comment
;  (def p {:sex            "male"
;    :birthdate      "1949-06-01"
;    :address        "Moscow"
;    :medical_policy 123
;    })
;  (search-patients p)
;  (search-patients {:birthdate      {:method :more :value "1949-06-01"}
;                    :address "Moscow"})
;  (search-patients {:birthdate {:method :less :value "1996-01-01"}})
;
;  patient
;  let defn loop
;
;  (merge {:a :b :c :d} {:c :e})
;
;  (pp/pprint patient)
;  (def patient-with-method (merge patient {:birthdate {:method :more :value "2000-01-01"}})))
;  " and " "birthdate < 2000-01-01" " and "
;;; (if (map? (:birthdate query)))
;  (search-patients {:birthdate
;                    {:method :less :than "2000-01-01"}})   ;;
;
;(comment (for [a [:a :b :c]] (str a))
;         (let [& {:keys a}])
;         (let [a (:a {:a 1})])
;         (let [{:keys birthdate} {:birthdate :value}] birthdate)
;         (get-in patient-with-method [:birthdate :value])
;         (get-in {:a {:b {:c :d}}} [:a :b :c]))
;
;(comment
;  ;(defn by_id [patient_id]
;  ;  (j/query db ["SELECT * FROM patients WHERE id = ?" patient_id]))
;  ;)
;  (def patient {:sex "female" :birthdate "1998-10-23" :full_name "Lola2" :address "USA" :medical_policy "42"})
;  (add-patient patient)
;  (delete-patient (:medical_policy patient)))
;
;  (def patient {:sex "female" :birthdate "1998-10-23" :full_name "Lola2" :address "USA" :medical_policy "13123123"})
;
;(comment
;  (search-patients patient)
;  (search-patients (let [p (first (list-patients))]
;     (merge p {:birthdate {:method :more :value "1950-06-01"}})))
;  (add-patient patient)
;  (valid? patient)
;  (empty?
;    (search-patients
;      {:full_name "Abraham Linkoln", :sex "male", :birthdate "1998-10-21", :address "USA", :medical_policy 13123123})))
;
;(comment
;  (valid? patient)
;  (and (:sex params) (:address params))
;  )
