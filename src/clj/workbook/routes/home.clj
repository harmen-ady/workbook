(ns workbook.routes.home
  (:require [workbook.layout :as layout]
            [workbook.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v])
  (:import (java.util Date)))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs2.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn messages-page [{:keys [flash]}]
  (layout/render
    "messages.html"
    (merge {:messages (db/get-messages)}
           (select-keys flash [:name :message :errors]))))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/messages")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message!
        (assoc params :timestamp (Date.)))
      (response/found "/messages"))))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/about" [] (about-page))
           (POST "/message" request (save-message! request))
           (GET "/messages" request (messages-page request)))

