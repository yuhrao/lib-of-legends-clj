(ns riot.client.lol.dataset
  (:require
    [riot.client.http :as riot-client.http]))

(defn- fetch-champions-assets [{{:keys [version lang]} :specs}]
  (->> (format "http://ddragon.leagueoflegends.com/cdn/%s/data/%s/champion.json" version lang)
       slurp
       riot-client.http/parse-json
       :data
       (map (fn [[_ data]]
              (let [new-key (Integer/parseInt (:key data))
                    new-value (-> (select-keys data
                                               [:key
                                                :name
                                                :id])
                                  (update :key #(Integer/parseInt %)))]
                [new-key new-value])))
       (into {})))

(defn- fetch-map-assets [{{:keys [version lang]} :specs}]
  (->> (format "http://ddragon.leagueoflegends.com/cdn/%s/data/%s/map.json" version lang)
       slurp
       riot-client.http/parse-json
       :data
       (map (fn [[k data]]
              (let [new-key (-> k
                                name
                                Integer/parseInt)
                    new-value (-> (select-keys data
                                               [:map-name
                                                :map-id]))]
                [new-key new-value])))
       (into {})))

(defn inject-datasets [client]
  (assoc client
    :datasets
    {:champions (fetch-champions-assets client)
     :maps      (fetch-map-assets client)}))

(defn champion-key->champion [client k]
  (-> client
      :datasets
      :champions
      (get k)))

(def team-id->team-side {100 {:side :blue
                              :id   100}
                         200 {:side :red
                              :id   200}})

(defn map-id->map [client id]
  (-> client
      :datasets
      :maps
      (get id)
      (assoc :map-id id)))