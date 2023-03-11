(ns riot.client.lol.http.core
  (:require
    [clojure.string :as string]
    [riot.client.lol.dataset :as lol-client.dataset]
    [riot.client.lol.http.summoner :as lol-http.summoner]
    [riot.client.lol.http.matches :as lol-http.matches]
    [riot.client.http :as riot-client.http]))

(defn- get-latest-version []
  (-> "https://ddragon.leagueoflegends.com/api/versions.json"
      slurp
      riot-client.http/parse-json
      first))

(defn- region-or-platform->host [region-or-platform]
  (->> region-or-platform
       string/lower-case
       (format "https://%s.api.riotgames.com")))

(defn- generate-specs [{:keys [region platform lang api-key]}]
  {:hosts    {:region   (region-or-platform->host region)
              :platform (region-or-platform->host platform)}
   :version  (get-latest-version)
   :api-key  api-key
   :region   region
   :platform platform
   :lang     lang})

(defn client [{:keys [_region _platform _lang _api-key] :as config}]
  (let [base-client {:specs (generate-specs config)
                     :ops   (merge
                              lol-http.matches/ops
                              lol-http.summoner/ops)}]
    (-> base-client
        lol-client.dataset/inject-datasets)))
