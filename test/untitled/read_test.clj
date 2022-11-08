(ns untitled.read-test
  (:require
    [clojure.java.jdbc :as j]
    [clojure.test :refer :all]
    [migratus.core :as migratus]
    [untitled.core :refer :all]
    [untitled.crud_http :as http]
    [untitled.crud_util :as util]
    [untitled.db :refer :all]))


(def db-data
  [{:full_name "Ivan Ivanov" :sex "male" :address "Moscow" :birthdate (util/date "1990-04-03") :medical_policy 1234563123232341}
  {:full_name "Kirill Strebkov" :sex "male" :address "New-York" :birthdate (util/date "1991-01-01") :medical_policy 1234502312334341}
  {:full_name "Alexandra Ershova" :sex "female" :address "Moscow" :birthdate (util/date "1991-01-01") :medical_policy 1000563123232341}
  {:full_name "Marina Nikitina" :sex "female" :address "New-York" :birthdate (util/date "1959-12-31") :medical_policy 1234563123230001}])


(defmacro with-db-rollback
  [[t-conn & bindings] & body]
  `(j/with-db-transaction [~t-conn ~@bindings]
                          (j/db-set-rollback-only! ~t-conn)
                          ~@body))

(defn fix-prepare-db [t]
  (with-db-rollback [tx *db*]
                    (binding [*db* tx]
                      (j/execute! *db* ["TRUNCATE TABLE patients RESTART IDENTITY"])
                      (doseq [rows db-data]
                            (j/insert! *db* :patients rows))
                      (t))))


(use-fixtures :once fix-prepare-db)


; READ TESTS
(deftest test-http-list-patients
  (testing "ok status"
    (is (= 200
          (:status (http/list-patients)))))

  (testing "not empty body"
    (is (not-empty
         (:body (http/list-patients)))))

  (testing "4 patients in db"
    (is (= 4
          (count (:body (http/list-patients)))))))


(deftest test-http-get-patient-valid
  (testing "ok status"
    (is (= 200
          (:status (http/get-patient 1)))))

  (testing "name of patient with id = 1"
    (is (= "Ivan Ivanov"
          (:full_name (:body (http/get-patient 1)))))))


(deftest test-http-get-patient-invalid
  (testing "400 status - no patient with such id in db"
    (is (= 400
          (:status (http/get-patient 13)))))

  (testing "fail in body - no patient with such id in db"
    (is (= "fail"
          (:body (http/get-patient 13)))))

  (testing "400 status - id not an integer"
    (is (= 400
          (:status (http/get-patient "13")))))

  (testing "fail in body - id not an integer"
    (is (= "fail"
         (:body (http/get-patient "13"))))))


(deftest test-http-validate-search-patients-valid
  (testing "search {:sex male}"
    (is (= 200
         (:status (http/validate-search-patients {:sex "male"})))))

  (testing "search {:sex male} first patient - fullname"
    (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:sex "male"})))))))

  (testing "search {:fullname Kirill Strebkov} first patient - medecal_policy"
    (is (= 1234502312334341
         (:medical_policy (first (:body (http/validate-search-patients {:full_name "Kirill Strebkov"})))))))

  (testing "search {:address Moscow} first patient - fullname"
    (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:address "Moscow"})))))))

  (testing "search {:medical_policy 1000563123232341} first patient - fullname"
    (is (= "Alexandra Ershova"
         (:full_name (first (:body (http/validate-search-patients {:medical_policy 1000563123232341})))))))

  (testing "search {:id 3} first patient - fullname"
    (is (= "Alexandra Ershova"
         (:full_name (first (:body (http/validate-search-patients {:id 3})))))))

  (testing "search {:sex male} first patient - fullname"
    (is (= "Kirill Strebkov"
         (:full_name (first (:body (http/validate-search-patients {:sex "male" :address "New-York"})))))))

  (testing "search {:sex male} count all patients"
    (is (= 2
         (count (:body (http/validate-search-patients {:sex "male"}))))))

  (testing "search {:birthdate 1990-04-03} first patient - fullname"
    (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients {:birthdate (util/date "1990-04-03")})))))))

  (testing "search {:birthdate {:method :less :value 1990-04-03}} first patient - fullname"
    (is (= "Ivan Ivanov"
         (:full_name (first (:body (http/validate-search-patients
                                     {:birthdate {:method :less :value (util/date "1990-04-10")}})))))))

  (testing "search {:birthdate {:method :less :value 1990-04-03}} count all patients"
    (is (= 2
         (count (:body (http/validate-search-patients
                         {:birthdate {:method :less :value (util/date "1990-04-10")}})))))))


(deftest test-http-validate-search-patients-valid-no-results
  (testing "status of unmatched search {:address KAZANTIP}"
    (is (= 200
         (:status (http/validate-search-patients {:address "KAZANTIP"})))))

  (testing "empty body of unmatched search {:address KAZANTIP}"
    (is (empty?
        (:body (http/validate-search-patients {:address "KAZANTIP"}))))))


(deftest test-http-validate-search-patients-invalid-params
  (testing "status of invalid address value search"
    (is (= 400
         (:status (http/validate-search-patients {:address 1323})))))

  (testing "error value of invalid address value search"
    (is (= :address
         (first (:value (:body (http/validate-search-patients {:address 1323})))))))

  (testing "error value of invalid sex value search"
    (is (= :sex
         (first (:value (:body (http/validate-search-patients {:sex "nogender"})))))))

  (testing "error value of invalid fullname value search"
    (is (= :full_name
         (first (:value (:body (http/validate-search-patients {:full_name 1323})))))))

  (testing "error value of invalid medical_policy value search"
    (is (= :medical_policy
         (first (:value (:body (http/validate-search-patients {:medical_policy 123})))))))

  (testing "error value of invalid birthdate value search"
    (is (= :birthdate
         (first (:value (:body (http/validate-search-patients {:birthdate 124412})))))))

  (testing "error-type of invalid birthdate method search"
    (is (= :invalid-keys
         (:error-type (:body (http/validate-search-patients
                               {:birthdate {:method :invalid-method :value (util/date "1990-04-10")}}))))))

  (testing "error value of invalid birthdate method search"
    (is (= :birthdate
       (first (:value (:body (http/validate-search-patients
                               {:birthdate {:method :invalid-method :value (util/date "1990-04-10")}}))))))))
