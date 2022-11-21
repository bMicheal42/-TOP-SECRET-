(ns app.views
  (:require [app.atoms :refer [patients editable filter-placeholder search-address
                               search-birthdate search-name search-params
                               search-policy search-sex selected-search-option update-button-name wrong-keys]]
            [app.events :refer [click-add-button click-cancel-button
                                click-delete-button click-filter-button
                                click-search-button click-update-button enter-in-input]]
            [app.utils :refer [key->string]]))


(def draw-cancel-button
  [:td [:a.more {:href "#" :on-click #(click-cancel-button)} "Cancel"]])

(defn draw-update-button [id]
  [:td [:a.more {:href "#" :on-click #(click-update-button id)}
        (if (= @editable id) @update-button-name "Update")]])

(defn draw-delete-button [id]
  [:td [:a.delete {:href "#" :on-click #(click-delete-button id)} "Delete"]])

(defn draw-add-button []
  [:td [:a.more {:href "#" :on-click #(click-add-button)}
        "Add patient"]])

(defn draw-filter-button []
  [:td [:a.more {:href "#" :on-click #(click-filter-button)}
        "Filter"]])

(defn draw-input [key value]
  [:td [:input {:type "text"
                :style {:color (if (some #(= (key->string key) %) @wrong-keys) "red" "black")}
                :defaultValue value :on-change #(enter-in-input % key)}]])

(defn draw-filter-input [param]
  [:td [:input {:placeholder @filter-placeholder
                :on-change #(reset! param (-> % .-target .-value))}]])

(defn draw-patient-data [patient]
  (into [:tr {:scope "row"}]
        (if (= @editable (:id patient))
          ;; if patient editable draw inputs for params
          (map (fn [[key value]]
                 (if (= key :id)
                   [:td value]
                   (draw-input key value))) patient)
          ;; if not - dras just table
          (map (fn [[_ value]] [:td value]) patient))))

(defn draw-search []
  [:div.form-group
   [:input
    {:type "text" :placeholder "Search by ..."
     :on-change #(reset! search-params (-> % .-target .-value))}]
   [:select.form-control
    {:on-change #(reset! selected-search-option (-> % .-target .-value))}
    {:field :list :id :many.options}
    [:option {:value :id} "Id"]
    [:option {:value :full_name} "Full name"]
    [:option {:value :birthdate} "Birthdate"]
    [:option {:value :sex} "Sex"]
    [:option {:value :address} "Address"]
    [:option {:value :medical_policy} "Medical Policy"]]
   [:button {:type "submit" :on-click #(click-search-button)} "Search"]])


(defn fill-patients-info []
  (for [patient @patients]
    (let [row (conj
               (draw-patient-data patient)
               (draw-update-button (:id patient))
               (draw-delete-button (:id patient)))]
      (if @editable (conj row draw-cancel-button) row)))) ;; draw Cancel button


(defn render []
  [:div.content
   (draw-search)
   [:br] [:hr]
   [:div.container
    [:h2.mb-5]
    [:div.table-responsive
     [:table.table.table-striped.custom-table
      [:thead
       [:tr
        [:th {:scope "col"} "Id"]
        [:th {:scope "col"} "Full name"]
        [:th {:scope "col"} "Birthdate"]
        [:th {:scope "col"} "Sex"]
        [:th {:scope "col"} "Address"]
        [:th {:scope "col"} "Medical Policy"]]]
      [:tbody
       [:tr {:scope "row"} [:td]
        (draw-filter-input search-name)
        (draw-filter-input search-birthdate)
        (draw-filter-input search-sex)
        (draw-filter-input search-address)
        (draw-filter-input search-policy)
        (draw-add-button)
        (draw-filter-button)]]
      (into [:tbody] (fill-patients-info))]]]])