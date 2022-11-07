(ns untitled.core-test
  (:require
    [clojure.java.jdbc :as j]
    [clojure.test :refer :all]
    [untitled.core :refer :all]
    [untitled.crud_http :as http]
    [untitled.crud_util :as util]
    [untitled.db :refer :all]
    [migratus.core :as migratus])
  (:import (org.eclipse.jetty.server.session Session)))


(def db-data
  [{:full_name "Ivan Ivanov" :sex "male" :address "Moscow" :birthdate (util/date "1990-04-03") :medical_policy 1234563123232341}
  {:full_name "Kirill Strebkov" :sex "male" :address "New-York" :birthdate (util/date "1991-01-01") :medical_policy 1234502312334341}
  {:full_name "Alexandra Ershova" :sex "female" :address "Moscow" :birthdate (util/date "1991-01-01") :medical_policy 1000563123232341}
  {:full_name "Marina Nikitina" :sex "female" :address "New-York" :birthdate (util/date "1959-12-31") :medical_policy 1234563123230001}])

(defn fix-db-prepare-data [t]
  (j/execute! *db* ["TRUNCATE TABLE patients RESTART IDENTITY"])
  (doseq [rows db-data]
    (j/insert! *db* :patients rows))
  (t))


(use-fixtures :once fix-db-prepare-data)


(def
  ;^{:private true}
  petr {:full_name      "Petr tmeizo",
        :birthdate      (util/date "1992-08-24"),
        :sex            "male",
        :address        "Yerevan",
        :medical_policy 2000020135203525})


(defmacro with-db-rollback
  [[t-conn & bindings] & body]
  `(j/with-db-transaction [~t-conn ~@bindings]
                          (j/db-set-rollback-only! ~t-conn)
                          ~@body))

; READ TESTS
(deftest test-http-list-patients
  (is (= 200
         (:status (http/list-patients))))
  (is (not-empty
        (:body (http/list-patients))))
  (is (= 4
         (count (:body (http/list-patients))))))


(deftest test-http-get-patient-valid
  (is (= 200
         (:status (http/get-patient 1))))
  (is (= "Ivan Ivanov"
         (:full_name (:body (http/get-patient 1))))))

(deftest test-http-get-patient-invalid
  (is (= 400
         (:status (http/get-patient 13))))
  (is (= "fail"
         (:body (http/get-patient 13))))
  (is (= 400
         (:status (http/get-patient "13"))))
  (is (= "fail"
         (:body (http/get-patient "13")))))


(deftest test-http-validate-search-patients-valid
  (is (= 200
         (:status (http/validate-search-patients {:sex "male"}))))
  (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:sex "male"}))))))
  (is (= 1234502312334341
         (:medical_policy (first (:body (http/validate-search-patients {:full_name "Kirill Strebkov"}))))))
  (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:address "Moscow"}))))))
  (is (= "Alexandra Ershova"
         (:full_name (first (:body (http/validate-search-patients {:medical_policy 1000563123232341}))))))
  (is (= "Alexandra Ershova"
         (:full_name (first (:body (http/validate-search-patients {:id 3}))))))
  (is (= "Kirill Strebkov"
         (:full_name (first (:body (http/validate-search-patients {:sex "male" :address "New-York"}))))))
  (is (= 2
         (count (:body (http/validate-search-patients {:sex "male"})))))
  (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:birthdate (util/date "1990-04-03")}))))))
  (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients
                                     {:birthdate {:method :less :value (util/date "1990-04-10")}}))))))
  (is (= 2
         (count (:body (http/validate-search-patients
                         {:birthdate {:method :less :value (util/date "1990-04-10")}}))))))

(deftest test-http-validate-search-patients-valid-no-results
  (is (= 200
         (:status (http/validate-search-patients {:address "KAZANTIP"}))))
  (is (empty?
        (:body (http/validate-search-patients {:address "KAZANTIP"})))))

(deftest test-http-validate-search-patients-invalid-params
  (is (= 400
         (:status (http/validate-search-patients {:address 1323}))))
  (is (= :address
         (first (:value (:body (http/validate-search-patients {:address 1323}))))))
  (is (= :sex
         (first (:value (:body (http/validate-search-patients {:sex "nogender"}))))))
  (is (= :full_name
         (first (:value (:body (http/validate-search-patients {:full_name 1323}))))))
  (is (= :medical_policy
         (first (:value (:body (http/validate-search-patients {:medical_policy 123}))))))
  (is (= :birthdate
         (first (:value (:body (http/validate-search-patients {:birthdate 124412})))))))
  (is (= :invalid-keys
         (:error-type (:body (http/validate-search-patients
                               {:birthdate {:method :invalid-method :value (util/date "1990-04-10")}})))))
  (is (= :birthdate
       (first (:value (:body (http/validate-search-patients
                               {:birthdate {:method :invalid-method :value (util/date "1990-04-10")}}))))))
