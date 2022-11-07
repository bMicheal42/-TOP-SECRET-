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
  (is (= (:status (http/list-patients)) 200))
  (is (not-empty (:body (http/list-patients))))
  (is (= (count (:body (http/list-patients))) 4)))


(deftest test-get-patient-valid
  (is (= (:status (http/get-patient 1)) 200))
  (is (= (:full_name (:body (http/get-patient 1))) "Ivan Ivanov")))

(deftest test-get-patient-invalid
  (is (= (:status (http/get-patient 13)) 400))
  (is (= (:body (http/get-patient 13)) "fail"))
  (is (= (:status (http/get-patient "13")) 400))
  (is (= (:body (http/get-patient "13")) "fail")))

;(deftest test-added (with-db-rollback [tx *db*]
;                  (binding [*db* tx]
;                    (untitled.crud_http/add-patient petr)
;                    (is (untitled.crud_http/validate-search-patients {:medical_policy 2000020135203525})))))

;(defn fix-factory [type number]
;  (fn [t]
;    (println (format "%s %s starts" type number))
;    (t)
;    (println (format "%s %s ends" type number))))
;
;(use-fixtures :once
;              (fix-factory :once 1)
;              (fix-factory :once 2))
;
;(use-fixtures :each
;              (fix-factory :each 3)
;              (fix-factory :each 4))


