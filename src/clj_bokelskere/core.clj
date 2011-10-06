(ns clj-bokelskere.core
  (:require [clojure.java.io :as io])
  (:require [clojure.data.json :as json]))

(def base-url "http://bokelskere.no")
(def api-version "/api/1.0")
(def data-format "?format=json")
(def v1 (str base-url api-version))

(def ^:dynamic *last-download* (ref 0))

(defn- wait-download []
  (let [difference (- (+ @*last-download* 1000) (System/currentTimeMillis))]
    (if (pos? difference) (Thread/sleep difference))))

(defn- download [url]
  (let [page (do (wait-download) (slurp url))]
    (do (dosync (ref-set *last-download* (System/currentTimeMillis)))
        page)))

(defn- json-get [url] (json/read-json (download url)))

(defn profil [brukernavn]
  (json-get (str v1 "/bokelsker/" brukernavn data-format)))

(defn boker [brukernavn]
  (json-get (str v1 "/bokelsker/" brukernavn "/boker" data-format)))


