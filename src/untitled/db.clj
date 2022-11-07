(ns untitled.db
  (:require [dotenv :refer [env]]
            [migratus.core      :as migratus]
            [compojure.core     :refer :all]
            ))

;(defmacro with-db-rollback
;  [[t-conn & bindings] & body]
;  `(jdbc/with-db-transaction [~t-conn ~@bindings]
;                             (jdbc/db-set-rollback-only! ~t-conn)
;                             ~@body))

(def ^:dynamic *db* {:dbtype "postgresql"
         :dbname   "postgres"
         :host     (env :DB_HOST)
         :port     (env :DB_PORT)
         :user     (env :DB_USER)
         :password (env :DB_PASSWORD)})

(def config
  {:store         :database
   :migration-dir "migrations"
   :db            *db*})


;(migratus/init config)
;(migratus/migrate config)
;(migratus/rollback config)