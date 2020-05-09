(ns workbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET]]
            [clojure.string :refer [join]]
            [workbook.ws :as ws]))

(defn errors-component [error id]
  (when-let [error (id @error)]
    [:div.alert.alert-danger (join error)]))

(defn message-form [fields errors]
  [:div.content
   [:div.form-group
    [errors-component errors :name]
    [:p "Name:"
     [:input.form-control
      {:type      :text
       :value     (:name @fields)
       :on-change #(swap! fields assoc :name (-> % .-target .-value))}]]
    [errors-component errors :message]
    [:p "Message:"
     [:textarea.form-control
      {:rows      4
       :cols      50
       :value     (:message @fields)
       :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
    [:input.btn.btn-primary
     {:type  :submit
      :on-click #(ws/send-message! @fields)
      :value "comment"}]]])

(defn get-messages [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (vec %))}))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:cite " - " name]])])

(defn response-handler [messages fields errors]
  (fn [message]
    (if-let [response-errors (:errors message)]
      (reset! errors response-errors)
      (do
        (reset! errors nil)
        (reset! fields nil)
        (swap! messages conj message)))))

(defn home []
  (let [messages (atom nil)
        errors (atom nil)
        fields (atom nil)]
    (ws/connect! (str "ws://" (.-host js/location) "/ws")
                 (response-handler messages fields errors))
    (get-messages messages)
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list messages]]]
       [:div.row
        [:div.span12
         [message-form fields errors]]]])))

(reagent/render
  [home]
  (.getElementById js/document "content"))