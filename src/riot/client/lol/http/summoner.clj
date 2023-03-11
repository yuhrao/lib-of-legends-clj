(ns riot.client.lol.http.summoner
  (:require [riot.client.http :as riot-client.http]
            [riot.client.lol.dataset :as lol-client.dataset]))

(defn- get-game-data [cli]
  (fn [{:keys [game-name game-tag]}]
    (let [path (str "/riot/account/v1/accounts/by-riot-id/" game-name "/" game-tag)]
      (->> (riot-client.http/request
             cli {:path     path
                     :method   :get
                     :strategy :region})
           :body))))

(defn- get-summoner-data [cli]
  (fn [{:keys [puuid]}]
    (let [path (str "/lol/summoner/v4/summoners/by-puuid/" puuid)]
      (-> (riot-client.http/request
            cli {:path     path
                    :method   :get
                    :strategy :platform})
          :body
          (clojure.set/rename-keys {:id   :summoner-id
                                    :name :summoner-name})))))

(defn get-player-data [cli]
  (fn [{:as payload}]
    (let [{:keys [puuid]
           :as   game-data} ((get-game-data cli) payload)
          summonder-data ((get-summoner-data cli) {:puuid puuid})]
      (merge game-data summonder-data))))

(defn get-top-mastery [cli]
  (fn [{:keys [summoner-id limit]
        :or   {limit 3}}]
    (let [path (str "/lol/champion-mastery/v4/champion-masteries/by-summoner/" summoner-id "/top")]
      (->> (riot-client.http/request
             cli {:path         path
                     :query-params {:count limit}
                     :method       :get
                     :strategy     :platform})
           :body
           (map (fn [{:keys [champion-id] :as mastery}]
                  (-> mastery
                      (assoc :champion (lol-client.dataset/champion-key->champion cli champion-id))
                      (dissoc :champion-id))))))))

(def ops {:get-player-data   {:doc        {:description "Get player's basic data by its game name and tag"}
                              :spec       {:request [:map
                                                     [:game-name :string]
                                                     [:game-tag :string]]}
                              :handler-fn get-player-data}
          :get-summoner-data {:doc        {:description "Get sumoner's data by its puuid"}
                              :spec       {:request [:map
                                                     [:puuid :string]]}
                              :handler-fn get-summoner-data}

          :get-top-mastery   {:doc        {:description "Get sumoner's top masteries (desc order)"}
                              :spec       {:request [:map
                                                     [:summoner-id :string]]}
                              :handler-fn get-top-mastery}})