# THIS GIT REPO IS LARGE!

Contains ~250 MBs of data.

# Norsk Lovtidend

Dette er en kopi av alle kunngjøringer i Norsk Lovtidend. De er lastet ned fra Lovdata.no hvor Justisdepartementet gir ut nye lover og nye forskrifter.

Datasettet er laget i forbindelse med [Norske-postlister.no](https://norske-postlister.no/) for å lage en fullstendig liste over alle norske lover.


## Svar fra Lovdata ang fri bruk av Norsk Lovtidend

Lovdata svarte følgende 11.02.2020:

> Kunngjøringene i Norsk Lovtidend (https://lovdata.no/register/lovtidend) er offentlige dokumenter som kan benyttes fritt. Dette i motsetning til Lovdatas ajourførte regelverk. Det er en feiloppfatning at Lovdata produserer svært lite av innholdet selv og at innhold på åpne sider kan benyttes fritt. Selv om mye av arbeidet isolert sett ikke har verkshøyde, vil sammenstillingene innebære at materiellet har vern etter åndsverkloven. Kunngjøringene i Norsk Lovtidend kan dermed benyttes fritt, men ikke det ajourførte/konsoliderte regelverket.


## Oppdatere informasjon (laste ned Norsk Lovtidend)

Først må avhengigheter hentes ned:

> composer install

Følgende kommando henter ned alle i år med 4 dager cache og resten med 365 dager cache. HTML, CSV og JSON vil oppdateres.

> php get.php

Krever PHP 8.

## TODO

- [x] Basis HTML-side for publisering på Github pages
- [x] Liste over alle lover (JSON + HTML)
- [x] Liste over alle lover og deres endringer (JSON + HTML)
