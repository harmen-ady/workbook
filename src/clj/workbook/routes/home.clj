(ns workbook.routes.home
  (:require [workbook.layout :as layout]
            [workbook.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [response status]])
  (:import (java.util Date)))

(defn home-page [{:keys [flash]}]
  (layout/render "home.html"))

(defn about-page []
  (layout/render "about.html"))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> {:errors errors} response (status 400))
    (do
      (db/save-message!
        (assoc params :timestamp (Date.)))
      (response {:status :ok}))))

(defroutes home-routes
           (GET "/" request (home-page request))
           (GET "/messages" [] (response (db/get-messages)))
           (POST "/message" request (save-message! request))
           (GET "/about" [] (about-page)))


