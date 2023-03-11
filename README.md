# Lib of Legends

API for interact with [Riot Games API](https://developer.riotgames.com)

It's HEAVILY inspired by [cognitect/aws-api](https://github.com/cognitect-labs/aws-api)

## Usage

### Create a client

```clojure
(require '[riot.client.core :as riot])
(def client (riot/client
              :lol
              {:region   "AMERICAS"
               :platform "BR1"
               :lang     "pt_BR"
               :api-key  (System/getenv "RIOT_API_KEY")}))
```

## List operations

```clojure
(riot/list-ops client)
;; =>  (:list-matches :get-match :get-player-data ...)
```

## Doc for an operation

It'll bring you a description and malli schemas for request (and response sometime in the future)

```clojure
(riot/doc client :get-top-mastery)
;=> {:description "Get sumoner's top masteries (desc order)", :request [:map [:summoner-id :string]]}
```

## Invoking an operation

```clojure
(riot/invoke client
             {:op      :get-player-data
              :payload {:game-name "YoDa"
                        :game-tag  "BR1"}})
;; => {:summoner-id "ntboUGWmDpjIlCXWW7UVW7CThmxuSBFQ7LnP6rIbXv9y",
;; => :summoner-name "YoDa",
;; => :game-name "YoDa",
;; => :puuid "bD_BI9wYtrf4VcvRMwi32KrUiuSBSXiLscfOPiAJeHWIo2APKJbJ9EuSfTymRqvh3q-5Jn6Qk1rZVg",
;; => :account-id "PPup_AZlr1Q1xdyrOcIBtrdwMt00W4KKxGocrG2H6LY",
;; => :profile-icon-id 676,
;; => :tag-line "BR1",
;; => :revision-date 1678381961000,
;; => :summoner-level 537}
```