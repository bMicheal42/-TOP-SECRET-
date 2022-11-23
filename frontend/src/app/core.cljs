(ns app.core
  (:require 
            [app.http :refer [get-patients]]
            [app.views :refer [render]]
            [reagent.dom :as rdom]))


(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [render] root-el)))


(defn ^:export init []
  (get-patients)
  (mount-root))