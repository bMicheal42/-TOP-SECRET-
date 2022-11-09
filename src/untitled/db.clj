(ns untitled.db
  (:require [dotenv :refer [env]]
            [compojure.core     :refer :all]))

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
