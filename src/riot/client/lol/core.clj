(ns riot.client.lol.core
  (:require [riot.client.lol.http.core :as riot-http]))

(def http-client riot-http/client)

(defn- client+ops->executor [client executor-key]
  (let [handler-fn (-> client
                       :ops
                       (get executor-key))]
    {:handler (handler-fn client)}))

(defn invoke
  "Invoke a client operation `op` using provided arguments

  To see all available operations, use `list-ops` function"
  [client {:keys [op payload]}]
  (let [{:keys [handler]} (client+ops->executor client op)]
    (handler payload)))

(defn list-ops
  "Lists client operations"
  [client]
  (-> client
      :ops
      keys))
