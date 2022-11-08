(ns untitled.write-test
  (:require
    [clojure.java.jdbc :as j]
    [clojure.test :refer :all]
    [migratus.core :as migratus]
    [untitled.core :refer :all]
    [untitled.crud_http :as http]
    [untitled.crud_util :as util]
    [untitled.db :refer :all]))


(def
  ^:private
  valid-patient {:full_name      "Petr tmeizo",
        :birthdate      (util/date "1992-08-24"),
        :sex            "male",
        :address        "Yerevan",
        :medical_policy 2000020135203525})

(def
  ^:private
  invalid-patient {:full_name      "Abraham Linkoln",
                 :birthdate      (util/date "1992-08-24"),
                 :sex            "no gender",
                 :address        "Yerevan",
                 :medical_policy 525})


(defmacro with-db-rollback
  [[t-conn & bindings] & body]
  `(j/with-db-transaction [~t-conn ~@bindings]
                          (j/db-set-rollback-only! ~t-conn)
                          ~@body))

(defn fix-rollback [t]
  (with-db-rollback [tx *db*]
                    (binding [*db* tx]
                      (t))))


(defn fix-prepare-db [t]
  (with-db-rollback [tx *db*]
                    (binding [*db* tx]
                      (j/execute! *db* ["TRUNCATE TABLE patients RESTART IDENTITY"])
                      (t))))

(use-fixtures :once fix-prepare-db)
(use-fixtures :each fix-rollback)


(deftest test-http-create-update-delete-user-valid
  (testing "0 patients in db before insert"
    (is (= 0 (count (:body (http/list-patients))))))
  (testing "check user created successfully (status & params)"
    (let [response (http/add-patient valid-patient)]
     (is (= 201 (:status response)))
     (is (= "Petr tmeizo" (:full_name (:body response))))
     (is (= (util/date "1992-08-24") (:birthdate (:body response))))
     (is (= "male" (:sex (:body response))))
     (is (= "Yerevan" (:address (:body response))))
     (is (= 2000020135203525 (:medical_policy (:body response))))))
    (testing "1 patient in db after insert"
      (is (= 1 (count (:body (http/list-patients))))))
    (testing "try to create already existing user"
        (is (= :already-exists (:error-type (:body (http/add-patient valid-patient))))))
    (testing "still 1 patient in db after failed insert"
      (is (= 1 (count (:body (http/list-patients))))))
    (testing "update parameter :sex female"
      (is (= 200 (:status (http/update-patient 1 {:sex "female"})))))
    (testing "delete patient with id 1"
      (is (= 302 (:status (http/delete-patient 1)))))
    (testing "0 patient in db after failed insert"
      (is (= 0 (count (:body (http/list-patients))))))
    (testing "delete patient with id 1"
      (is (= 400 (:status (http/delete-patient 1))))
      (is (= "fail" (:body (http/delete-patient 1))))))


(deftest test-http-create-user-invalid-params
    (let [response (http/add-patient invalid-patient)]
      (testing "status 400 user not created because of invalid values"
        (is (= 400 (:status response))))
      (testing "return ivalid keys when user not created because of invalid values"
        (is (= [:sex :medical_policy] (:value (:body response)))))))


(deftest test-http-update-delete-invalid-params
  (is (= 201 (:status (http/add-patient valid-patient))))
  (testing "update non-existent patient"
    (is (= 400 (:status (http/update-patient 14 {:sex "female"})))))
  (testing "update existing patient with wrong parameter :sex 12123"
    (is (= 400 (:status (http/update-patient 1 {:sex 12123})))))
  (testing "1 patient in db"
    (is (= 1 (count (:body (http/list-patients))))))
  (testing "delete non-existent patient"
    (is (= "fail" (:body (http/delete-patient 14)))))
  (testing "delete invalid param"
    (is (= "fail" (:body (http/delete-patient "14")))))
  (testing "1 patient in db after delete"
    (is (= 1 (count (:body (http/list-patients)))))))
