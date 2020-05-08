(ns workbook.routes.home
  (:require [workbook.layout :as layout]
            [workbook.db.core :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response]]))

(defn home-page []
  (layout/render "home.html"))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/messages" [] (response (db/get-messages)))
           (GET "/about" [] (about-page)))
