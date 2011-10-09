(ns clj-bokelskere.tools
  (:require [clojure.java.io :as io])
  (:require [clj-json.core :as json])
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

(defn json-get [url] (json/parse-string (download url) true))

(defn- encode [parameter]
  (URLEncoder/encode parameter "UTF-8"))

(defn- validate-antall [antall max-results]
  (if (zero? antall) ""
      (str "&antall_per_side="
           (if (or (< antall 1) (> antall max-results)) max-results antall))))

(defn- validate-sidetall [sidetall]
  (if (zero? sidetall) "" (str "&side=" (if (pos? sidetall) sidetall 1))))

(defn generic [node resource max-results antall sidetall parameter & args]
  (let [a (validate-antall antall max-results)
        s (validate-sidetall sidetall)]
    (json-get (str v1 "/" node (encode parameter) resource data-format a s (apply str args)))))

(defmacro def-api
  "Generates code for all arities of generic-boker function.
  Don't call this macro directly, use the implemented functions to get data."
  [name node-name api-function & params]
  `(defn ~name
     ([~node-name]
        (~@api-function 0 0 ~node-name ~@params))
     ([~node-name ~'antall]
        (~@api-function ~'antall 0 ~node-name ~@params))
     ([~node-name ~'antall ~'sidetall]
        (~@api-function ~'antall ~'sidetall ~node-name ~@params))))

(defmacro api-bokelskere [name params]
  `(def-api ~name ~'brukernavn (generic "bokelsker/" "/boker" 10) ~params))

(defmacro api-folger [name params]
  `(def-api ~name ~'brukernavn (generic "bokelsker/" "/folger" 10) ~params))

(defmacro api-folges-av [name params]
  `(def-api ~name ~'brukernavn (generic "bokelsker/" "/folges_av" 10) ~params))

(defmacro api-bokhylle [name params]
  `(def-api ~name ~'_ (generic "" "bokhyller" 100) ~params))

(defmacro api-bokhylle->bok [name params]
  `(def-api ~name ~'bokhyllenavn (generic "bokhyller/"  ""  100) ~params))

(defmacro api-emneord [name params]
  `(def-api ~name ~'emneord (generic "boker/" "emneord" 100) ~params))

(defmacro api-emneord->boker [name params]
  `(def-api ~name ~'emneord (generic "boker/emneord/" "" 20) ~params))
