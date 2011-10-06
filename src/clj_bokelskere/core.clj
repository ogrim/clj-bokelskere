(ns clj-bokelskere.core
  (:require [clojure.java.io :as io])
  (:require [clojure.data.json :as json])
  (import [java.net URLEncoder]))

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

(defn- encode [parameter]
  (URLEncoder/encode parameter "UTF-8"))

(defn- generic-bokelsker [brukernavn node antall sidetall & args]
  (let [a (str "&antall_per_side=" (if (or (< antall 1) (> antall 10)) 10 antall))
        s (str "&side=" (if (pos? sidetall) sidetall 1))]
    (json-get (str v1 "/bokelsker/" brukernavn node data-format a s (apply str args)))))

(defn- generic-boker 
  ([brukernavn resource] (generic-bokelsker brukernavn "/boker" 10 1 resource))
  ([brukernavn antall resource] (generic-bokelsker brukernavn "/boker" antall 1 resource))
  ([brukernavn antall sidetall resource] (generic-bokelsker brukernavn "/boker" antall sidetall resource)))

(defn profil [brukernavn]
  (json-get (str v1 "/bokelsker/" brukernavn data-format)))

(defmacro defboker [name parameter]
  `(defn ~name
     ([brukernavn] (generic-boker brukernavn ~parameter))
     ([brukernavn antall] (generic-boker brukernavn antall ~parameter))
     ([brukernavn antall sidetall] (generic-boker brukernavn antall sidetall ~parameter))))

(defboker favoritter "&favoritt=1")

(defn boker
  ([brukernavn] (generic-boker brukernavn ""))
  ([brukernavn antall] (generic-boker brukernavn antall ""))
  ([brukernavn antall sidetall] (generic-boker brukernavn antall sidetall "")))

(defn favoritter
  ([brukernavn] (generic-boker brukernavn "&favoritt=1"))
  ([brukernavn antall] (generic-boker brukernavn antall "&favoritt=1"))
  ([brukernavn antall sidetall] (generic-boker brukernavn antall sidetall "&favoritt=1")))

(defn folger [brukernavn]
  (json-get (str v1 "/bokelsker/" brukernavn "/folger" data-format)))

(defn bokhyller [antall]
  (let [a (if (or (< antall 1) (> antall 100)) 100 antall)]
    (json-get (str v1 "/bokhyller" data-format "&antall_per_side=" a))))

(defn bokhylle->boker [bokhylle & sidetall]
  (let [side (if sidetall (str "&side=" (first sidetall)))]
    (json-get (str v1 "/bokhyller/" bokhylle data-format side))))

(defn emneord [antall & sidetall]
  (let [a (str "&antall_per_side=" (if (or (< antall 1) (> antall 100)) 100 antall))
        s (if sidetall (str "&side=" (first sidetall)) )]
   (json-get (str v1 "/boker/emneord" data-format a s))))

(defn emneord->boker
  ([emneord]
     (json-get (str v1 "/boker/emneord/" (encode emneord) data-format)))
  ([emneord antall & sidetall]
     (let [a (str "&antall_per_side=" (if (or (< antall 1) (> antall 20)) 20 antall))
           s (if sidetall (str "&side=" (first sidetall)) )]
       (json-get (str v1 "/boker/emneord/" (encode emneord) data-format a s)))))

(defn tilstander []
  (json-get (str v1 "/tilstander" data-format)))

(defn isbn->bok [isbn]
  (json-get (str v1 "/boker/info/" isbn data-format)))
