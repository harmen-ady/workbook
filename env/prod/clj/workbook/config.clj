(ns workbook.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[workbook started successfully]=-"))
   :middleware identity})
