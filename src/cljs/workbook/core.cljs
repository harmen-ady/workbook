(ns workbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn send-message! [fields errors]
  (POST "/message"
        {:format        :json
         :headers       {"Accept"       "application/transit+json"
                         "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params        @fields
         :handler       #(do
                           (.log js/console (str %))
                           (reset! fields {})
                           (reset! errors nil))
         :error-handler #(do
                           (.log js/console (str %))
                           (reset! errors (get-in % [:response :errors])))}))

(defn errors-component [error id]
  (when-let [error (id @error)]
    [:div.alert.alert-danger (clojure.string/join error)]))

(defn message-form []
  (let [fields (atom {})
        errors (atom nil)]
    (fn []
      [:div.content
       [:div.form-group
        [errors-component errors :name]
        [:p "Name:"
         [:input.form-control
          {:type      :text
           :name      :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value     (:name @fields)}]]
        [errors-component errors :message]
        [:p "Message:"
         [:textarea.form-control
          {:rows      4
           :cols      50
           :name      :message
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}
          (:message @fields)]]
        [:input.btn.btn-primary
         {:type     :submit
          :on-click #(send-message! fields errors)
          :value    "comment"}]]])))

(defn home []
  [:div.row
   [:div.span12
    [message-form]]])

(reagent/render
  [home]
  (.getElementById js/document "content"))