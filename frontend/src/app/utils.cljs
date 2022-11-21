(ns app.utils
  (:require 
            [app.atoms :refer [patients search-address search-birthdate search-name search-params
                               search-policy search-sex editable editable-params
                               ]]
            [clojure.string :as str]))


(defn truncate-string "Take n chars from string." [string n] (apply str (take n string)))

(defn clear-search-atoms []
  (reset! search-params nil) (reset! search-name nil) (reset! search-address nil)
  (reset! search-sex nil) (reset! search-birthdate nil) (reset! search-policy nil))

(defn key->string [key]
  (clojure.string/join (take-last (- (count (str key)) 1) (str key))))

(defn id->index [id]
  (first (keep-indexed (fn [index item] (when (= id (:id item)) index)) @patients)))

(defn get-patient-by-id [id]
  (first (filter #(= id (:id %)) @patients)))

(defn put-patient! [patient]
  (swap! patients assoc-in [(id->index (:id patient))] patient))

(defn render-patient! []
  (let [updated_patient (merge (get-patient-by-id @editable) @editable-params)]
    (put-patient! updated_patient)))

(defn merge-all-params []
  (let [new-map (merge {}
                       (when (and @search-name (-> @search-name str/blank? not)) {:full_name @search-name})
                       (when (and @search-address (-> @search-address str/blank? not)) {:address @search-address})
                       (when (and @search-sex (-> @search-sex str/blank? not)) {:sex @search-sex})
                       (when (and @search-birthdate (-> @search-birthdate str/blank? not)) {:birthdate @search-birthdate})
                       (when (and @search-policy (-> @search-policy str/blank? not)) {:medical_policy @search-policy}))]
    (when (not-empty new-map)(reset! search-params new-map))))

(defn parse-patients-dates [patients]
  (into [] (map
            (fn [patient]
              (update
               patient
               :birthdate
               (fn [birthdate]
                 (truncate-string birthdate 10))))
            patients)))
