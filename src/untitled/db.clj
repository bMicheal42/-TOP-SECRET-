(ns untitled.db
  (:require [dotenv :refer [env]]
            [compojure.core     :refer :all]))


(def ^:dynamic *db* {:dbtype "postgresql"
         :dbname   "postgres"
         :host     "34.142.30.44"
         :port     "5432"
         :user     "postgres"
         :password "password"})

(def config
  {:store         :database
   :migration-dir "migrations"
   :db            *db*})
