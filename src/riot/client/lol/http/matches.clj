(ns riot.client.lol.http.matches
  (:require [riot.client.http :as riot-client.http]
            [riot.client.lol.dataset :as lol-client.dataset]))

(defn- normalize-team [client team]
  (let [bans (->> team
                  :bans
                  (map #(update % :champion-id (partial
                                                 lol-client.dataset/champion-key->champion
                                                 client))))]
    (-> team
        (dissoc :team-id
                :queue-id)
        (clojure.set/rename-keys {:win :win?})
        (assoc
          :bans bans
          :side (-> team
                    :team-id
                    lol-client.dataset/team-id->team-side
                    :side)))))

(defn normalize-participants [client participant]
  (let [
        items (-> participant
                  (select-keys [:item-1
                                :item-2
                                :item-3
                                :item-4
                                :item-5
                                :item-6])
                  vals
                  (into []))
        champion (-> participant
                     :champion-id
                     ((partial
                        lol-client.dataset/champion-key->champion
                        client)))]
    (-> participant
        (dissoc
          :challenges
          :item-1
          :item-2
          :item-3
          :item-4
          :item-5
          :item-6)
        (select-keys
          [:puuid
           :lane
           :team-position
           :individual-position
           :summoner-name
           :summoner-id
           :role
           :wards-placed])
        (assoc :items items
               :champion champion))))

(defn- normalize-match [client match]
  (let [info (:info match)
        map-data (->> info
                      :map-id
                      (lol-client.dataset/map-id->map client))
        game (-> info
                 (select-keys [:game-type
                               :game-mode
                               :game-name
                               :game-duration
                               :game-id])
                 (clojure.set/rename-keys {:game-type     :type
                                           :game-mode     :mode
                                           :game-name     :name
                                           :game-duration :duration
                                           :game-id       :id}))
        teams (->> info
                   :teams
                   (map (partial normalize-team client))
                   (reduce #(assoc %1 (:side %2) %2) {}))

        participants (->> info
                          :participants
                          (take 1)
                          (map (partial normalize-participants client)))]
    (-> info
        (dissoc :map-id
                :game-type
                :game-mode
                :game-name
                :game-duration
                :game-id
                :game-creation
                :game-version
                :game-start-timestamp
                :game-end-timestamp)
        (assoc
          :participants participants
          :map map-data
          :teams teams
          :game game))))

(defn- get-match [client]
  (fn [{:keys [match-id]}]
    (let [path (format "/lol/match/v5/matches/%s" match-id)]
      (-> (riot-client.http/request client {:path     path
                                            :method   :get
                                            :strategy :region})
          :body
          ((partial normalize-match client))))))

(defn- list-matches [client]
  (fn [{:keys [puuid limit offset]
        :or   {limit  10
               offset 0}}]
    (let [path (format "/lol/match/v5/matches/by-puuid/%s/ids" puuid)]
      (-> (riot-client.http/request client {:path         path
                                            :query-params {:count limit
                                                           :start offset}
                                            :method       :get
                                            :strategy     :region})
          :body))))

(def ops {:list-matches list-matches
          :get-match    get-match})