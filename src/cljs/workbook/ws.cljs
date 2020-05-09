(ns workbook.ws
  (:require [cognitect.transit :as t]))

(defonce ws-chan (atom nil))
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

(defn receive-message! [handler]
  (fn [msg]
    (->> msg .-data (t/read json-reader) handler)))

(defn send-message! [msg]
  (if @ws-chan
    (->> msg (t/write json-writer) (.send @ws-chan))
    (throw (js/Error. "Websocket is not available!"))))

(defn connect! [url handler]
  (let [channel (js/WebSocket. url)]
    (set! (.-onmessage channel) (receive-message! handler))
    (reset! ws-chan channel)))