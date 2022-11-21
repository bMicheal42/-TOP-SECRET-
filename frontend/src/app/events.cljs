(ns app.events
  (:require
   [app.atoms :refer [search-params editable editable-params update-button-name
                      wrong-keys selected-search-option filter-placeholder]]
   [app.utils :refer [merge-all-params]]
   [app.http :refer [add-patient update-patient delete-patient get-patients search-patients]]))

(defn click-cancel-button []
  (reset! editable nil)
  (reset! wrong-keys nil)
  (reset! update-button-name "Update")
  (reset! editable-params nil))


(defn click-update-button [patient-id]
  (if (= @editable patient-id)
    (update-patient) ;; Confirm logic
    (do
      ;; Edit logic
      (reset! update-button-name "Confirm")
      (reset! editable patient-id))))

(defn click-delete-button [id]
  (when (js/confirm "Are you sure?") (delete-patient id)))

(defn click-search-button []
  (if (empty? @search-params)
    (get-patients)
    (let [search-query {(keyword @selected-search-option) @search-params}]
      (search-patients search-query)))
  (reset! search-params nil))

(defn click-filter-button []
  (reset! filter-placeholder "insert info here")
  (search-patients (merge-all-params))
  (js/console.log (pr-str @search-params))
  (if (empty? @search-params) (get-patients) (reset! search-params nil)))

(defn click-add-button []
  (merge-all-params) 
  (if (= 5 (count @search-params))
    (let [new-patient @search-params]
      (add-patient new-patient)) 
    (reset! filter-placeholder "REQUIRED FIELD"))
  (reset! search-params nil))

(defn enter-in-input [event key]
  (let [new-value (-> event .-target .-value)]
    (swap! editable-params merge {key new-value})))
