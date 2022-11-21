(ns app.atoms 
  (:require [reagent.core :as r]))

(def patients (r/atom nil))

(def editable (r/atom nil))

(def editable-params (r/atom nil))

(def search-params (r/atom nil))

(def update-button-name (r/atom "Update"))

(def wrong-keys (r/atom nil))

(def selected-search-option (r/atom "id"))

(def search-name (r/atom nil))
(def search-address (r/atom nil))
(def search-sex (r/atom nil))
(def search-birthdate (r/atom nil))
(def search-policy (r/atom nil))

(def filter-placeholder (r/atom "insert info here"))