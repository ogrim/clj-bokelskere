(ns clj-bokelskere.core
  (:use [clj-bokelskere.tools]))

(api-bokelskere boker "")
(api-bokelskere har-lest "&tilstand=har+lest")
(api-bokelskere leser "&tilstand=leser")
(api-bokelskere skal-lese "&tilstand=skal+lese")
(api-bokelskere avbrot "&tilstand=avbr%C3%B8t")
(api-bokelskere onsker-deg "&tilstand=%C3%B8nsker%20deg")
(api-bokelskere oppslagsverk "&tilstand=oppslagsverk")
(api-bokelskere favoritter "&favoritt=1")

(api-folger folger "")
(api-folges-av folges-av "")
(api-bokhylle bokhylle "")
(api-bokhylle->bok bokhylle->boker "")

(api-emneord emneord "")
(api-emneord->boker emneord->boker "")

(defn profil [brukernavn]
  (json-get (str v1 "/bokelsker/" brukernavn data-format)))

(defn tilstander []
  (json-get (str v1 "/tilstander" data-format)))

(defn isbn->bok [isbn]
  (json-get (str v1 "/boker/info/" isbn data-format)))
