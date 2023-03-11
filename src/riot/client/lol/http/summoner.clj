(ns riot.client.lol.http.summoner
  (:require [riot.client.http :as riot-client.http]
            [riot.client.lol.dataset :as lol-client.dataset]))

(defn- get-game-data [client]
  (fn [{:keys [game-name game-tag]}]
    (let [path (str "/riot/account/v1/accounts/by-riot-id/" game-name "/" game-tag)]
      (->> (riot-client.http/request
             client {:path     path
                     :method   :get
                     :strategy :region})
           :body))))

(defn- get-summoner-data [client]
  (fn [{:keys [puuid]}]
    (let [path (str "/lol/summoner/v4/summoners/by-puuid/" puuid)]
      (-> (riot-client.http/request
            client {:path     path
                    :method   :get
                    :strategy :platform})
          :body
          (clojure.set/rename-keys {:id   :summoner-id
                                    :name :summoner-name})))))

(defn get-player-data [client]
  (fn [{:keys [game-name game-tag]
        :as payload}]
    (let [{:keys [puuid]
           :as   game-data} ((get-game-data client) payload)
          summonder-data ((get-summoner-data client) {:puuid puuid})]
      (merge game-data summonder-data))))

(defn get-top-mastery [client]
  (fn [{:keys [summoner-id limit]
        :or   {limit 3}}]
    (let [path (str "/lol/champion-mastery/v4/champion-masteries/by-summoner/" summoner-id "/top")]
      (->> (riot-client.http/request
             client {:path         path
                     :query-params {:count limit}
                     :method       :get
                     :strategy     :platform})
           :body
           (map (fn [{:keys [champion-id] :as mastery}]
                  (-> mastery
                      (assoc :champion (lol-client.dataset/champion-key->champion client champion-id))
                      (dissoc :champion-id))))))))

(def ops {:get-player-data   get-player-data
          :get-summoner-data get-summoner-data
          :get-top-mastery   get-top-mastery})