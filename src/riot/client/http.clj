(ns riot.client.http
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]
            [clj-http.client :as http]
            [clojure.data.json :as json]))

(defn get-host [specs strategy]
  (-> specs
      :hosts
      strategy))

(defn parse-json [raw-value]
  (json/read-str raw-value :key-fn csk/->kebab-case-keyword))

(defn parse-response [response]
  (cond-> response
          (:body response) (update :body parse-json)))

(defn request [{:keys [specs]} {:keys [strategy path method query-params] :as _opts}]
  (let [headers {"X-Riot-Token" (:api-key specs)}
        -query-params (cske/transform-keys csk/->camelCaseString query-params)
        host (get-host specs strategy)]
    (-> (http/request {:url          (str host path)
                       :method       method
                       :query-params -query-params
                       :headers      headers})
        parse-response)))
