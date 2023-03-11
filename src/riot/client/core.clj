(ns riot.client.core
  (:require
    [riot.client.lol.http.core :as lol.http]
    [malli.core :as malli]
    [malli.error :as malli.err]))

(defn client [api config]
  (condp = api
    :lol (lol.http/client config)))

(defn list-ops
  "Lists client operations"
  [client]
  (-> client
      :ops
      keys))

(defn- valid-op-or-throw [cli {:keys [op]}]
  (when-not (contains? (:ops cli) op)
    (throw (ex-info "Invalid operation" {:invalid-op           op
                                         :available-operations (list-ops cli)}))))

(defn- valid-req-or-throw [cli {:keys [op payload] :as req}]
  (valid-op-or-throw cli req)
  (let [schema (-> cli
                   :ops
                   (get op)
                   :spec
                   :request)]
    (when-not (malli/validate schema payload)
      (let [humanized-errors (-> (malli/explain schema payload)
                                 (malli.err/humanize))]
        (throw (ex-info "Invalid Payload" humanized-errors))))))

(defn- client+ops->handler [cli executor-key]
  (let [{:keys [handler-fn]} (-> cli
                                 :ops
                                 (get executor-key))]
    (handler-fn cli)))

(defn doc [cli op]
  (let [{:keys [doc spec]} (-> cli :ops (get op))]
    (merge doc spec)))

(defn invoke
  "Invoke a client operation `op` using provided arguments

  To see all available operations, use `list-ops` function"
  [cli {:keys [op payload] :as req}]
  (valid-req-or-throw cli req)
  (let [handler (client+ops->handler cli op)]
    (handler payload)))