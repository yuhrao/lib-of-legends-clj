(ns riot.core
  (:require [riot.client.lol.core :as lol-client]))

(defonce state (atom {}))

(def client (lol-client/http-client {:region   "AMERICAS"
                                     :platform "BR1"
                                     :lang     "pt_BR"}
                                    {:api-key (System/getenv "RIOT_API_KEY")}))

(comment

  (lol-client/list-ops client)

  (lol-client/invoke client
                     {:op      :get-player-data
                      :payload {:game-name "YoDa"
                                :game-tag  "BR1"}})

  (let [{:keys [summoner-id]} (lol-client/invoke client
                                                 {:op      :get-player-data
                                                  :payload {:game-name "YoDa"
                                                            :game-tag  "BR1"}})
        mastery (lol-client/invoke client
                                   {:op      :get-top-mastery
                                    :payload {:summoner-id summoner-id
                                              :limit       5}})]
    mastery)

  (let [{:keys [puuid]} (lol-client/invoke client
                                           {:op      :get-player-data
                                            :payload {:game-name "YoDa"
                                                      :game-tag  "BR1"}})
        matches (lol-client/invoke client
                                   {:op :list-matches
                                    :payload {:puuid puuid
                                     :limit 3}})]
    matches)

  (let [match-id "BR1_2690795767"
        match (lol-client/invoke client
                                 {:op      :get-match
                                  :payload {:match-id match-id}})]
    #_(keys match)
    match
    )

  )