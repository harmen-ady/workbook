(ns workbook.routes.ws
  (:require [compojure.core :refer [GET POST defroutes]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [workbook.db.core :as db]
            [mount.core :refer [defstate]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant
             :refer [sente-web-server-adapter]]))

(let [connection (sente/make-channel-socket!
                  sente-web-server-adapter
                  {:user-id-fn
                   (fn [ring-req]
                     (get-in ring-req [:params :client-id]))})]
  (def ring-ajax-post (:ajax-post-fn connection))
  (def ring-ajax-get-or-ws-handshake (:ajax-get-or-ws-handshake-fn connection))
  (def ch-chsk (:ch-recv connection))
  (def chsk-send! (:send-fn connection))
  (def connected-uids (:connected-uids connection)))

(defn validate-message [params]
  (first
   (b/validate
    params
    :name v/required
    :message [v/required [v/min-count 10]])))

(defn save-message! [message]
  (if-let [errors (validate-message message)]
    {:errors errors}
    (do
      (db/save-message! message)
      message)))

(defn handle-message! [{:keys [id client-id ?data]}]
  (println "\n\n+++++++ GOT MESSAGE:" id (keys ?data))
  (when (= id :workbook/add-message)
    (let [response (-> ?data
                       (assoc :timestamp (java.util.Date.))
                       save-message!)]
      (if (:errors response)
        (chsk-send! client-id [:workbook/error response])
        (doseq [uid (:any @connected-uids)]
          (chsk-send! uid [:workbook/add-message response]))))))

(defn stop-router! [stop-fn]
  (when stop-fn (stop-fn)))

(defn start-router! []
  (println "\n\n+++++++ STARTING ROUTER! +++++++\n")
  (sente/start-chsk-router! ch-chsk handle-message!))

(defstate router
  :start (start-router!)
  :stop (stop-router! router))

(defroutes websocket-routes
  (GET "/ws" req (ring-ajax-get-or-ws-handshake req))
  (POST "/ws" req (ring-ajax-post req)))
