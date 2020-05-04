(ns workbook.test.db.core
  (:require [workbook.db.core :as db]
            [workbook.db.migrations :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [config.core :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (migrations/migrate ["migrate"])
    (f)))

(deftest test-messages
  (jdbc/with-db-transaction [t-conn db/conn]
                            (jdbc/db-set-rollback-only! t-conn)
                            (let [timestamp (java.util.Date.)]
                              (is (= 1 (db/save-message!
                                         {:name      "Bob"
                                          :message   "Hello World"
                                          :timestamp timestamp} {:connection t-conn})))
                              (is (= {:name      "Bob"
                                      :message   "Hello World"
                                      :timestamp timestamp}
                                     (-> (db/get-messages {} {:connection t-conn})
                                         first
                                         (select-keys [:name :message :timestamp])))))))
