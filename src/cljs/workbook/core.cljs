(ns workbook.core
    (:require [reagent.core :as reagent :refer [atom]]))

(defn message-form []
    (let [fields (atom {})]
        (fn []
            [:div.content
             [:div.form-group
              [:p "name: " (:name @fields)]
              [:p "message: " (:message @fields)]
              [:p "Name:"
               [:input.form-control
                {:type :text
                 :name :name
                 :on-change #(swap! fields assoc :name (-> % .-target .-value))
                 :valule (:name @fields)}]]]
             [:p "Message:"
              [:textarea.form-control
               {:rows 4
                :cols 50
                :name :message
                :on-change #(swap! fields assoc :message (-> % .-target .-value))}
               (:message @fields)]]
             [:input.btn.btn-primary {:type :submit :value "comment"}]])))

(defn home []
    [:div.row
     [:div.span12
      [message-form]]])

(reagent/render
    [home]
    (.getElementById js/document "content"))