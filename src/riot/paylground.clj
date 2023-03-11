(ns riot.paylground
  (:require [riot.client.core :as riot])
  (:import (clojure.lang ExceptionInfo)))

(defonce state (atom {}))

(def client (riot/client
              :lol
              {:region   "AMERICAS"
               :platform "BR1"
               :lang     "pt_BR"
               :api-key  (System/getenv "RIOT_API_KEY")}))

(comment

  (riot/list-ops client)

  (riot/doc client :get-top-mastery)

  (try
    (riot/invoke client
                 {:op      :get-player-data
                  :payload {:game-name "YoDa"
                            :game-tag  "BR1"}})
    (catch ExceptionInfo e
      (ex-data e)))

  (try
    (let [{:keys [summoner-id]} (riot/invoke client
                                           {:op      :get-player-data
                                            :payload {:game-name "YoDa"
                                                      :game-tag  "BR1"}})
        mastery (riot/invoke client
                             {:op      :get-top-mastery
                              :payload {:summoner-id summoner-id
                                        :limit       5}})]
    mastery)
    (catch ExceptionInfo e
      (ex-data e)))

  (let [{:keys [puuid]} (riot/invoke client
                                     {:op      :get-player-data
                                      :payload {:game-name "YoDa"
                                                :game-tag  "BR1"}})
        matches (riot/invoke client
                             {:op      :list-matches
                              :payload {:puuid puuid
                                        :limit 3}})]
    matches)

  (let [match-id "BR1_2690795767"
        match (riot/invoke client
                           {:op      :get-match
                            :payload {:match-id match-id}})]
    #_(keys match)
    match
    )

  )