# Koskeen tallennettavien tietojen validaatiosäännöt

## 30.7.2020
- Perusopetukseen valmistavissa suorituksissa ei edellytetä koulutusmoduulissa laajuutta, jos suoritustapa on 'erityinentutkinto'

## 22.5.2020
- Kaikkien aikajaksojen alkupäivämäärän pitää olla ennen tai sama kuin loppupäivämäärä

## 28.4.2020
- Jos suorituksen tyyppi on "ammatillinen perustutkinto" jonka suoritustapa ops tai reformi, rakenteessa tulee olla "tutkinnonosanryhmä" määriteltynä (Tor-982).
- Ammatillisen tutkinnon yhteisiin osa-alueisiin (koodit '101053', '101054', '101055', '101056', '400012', '400013', '400014') liittyviä validaatioita Tor-982
  - Arvioidun tutkinnon osan laajuus tulee olla sama kuin sen alle siirrettyjen osa-alueiden yhteislaajuus
  - Arvioidun tutkinnon osan alta tulee aina löytyä myös osa-alueita
  - Valmiiksi merkityn ammatillisen perustutkinnon, jonka suoritustapa on reformi, yhteisten tutkinnon osien yhteislaajuus on oltava tasan 35.
  - Valmiiksi merkityssä ammatillisessa perustutkinnossa ei voi esiintyä saman yhteisen tutkinnon osan koodi kuin kerran
  - Jos suoritustapa on reformi, ei sallita tutkinnon osia '101053', '101054', '101055', '101056' - jos suoritustapa ops ei sallita tutkinnon osia '400012', '400013', '400014' .

## 16.4.2020
- Päätason suorituksen alkamispäivä ei voi olla aiempi kuin sen sisältävän opiskeluoikeuden alkamispäivä

## 18.3.2020
- Ammatillisen osaamisen hankkimistapa ilman lisätietoja ei voi olla "oppisopimus" (Tor-868)

## 17.2.2020
- Nuorten perusopetuksen valinnaiseen valtakunnalliseen aineeseen sallitaan vain arvosanat S ja O, jos laajuus on alle kaksi vuosiviikkotuntia

## 3.2.2020
- Muun ammatillisen koulutuksen osasuorituksen arvioinnille uusi arvosanakoodisto, "arviointiasteikkomuuammatillinenkoulutus"

- Nuorten perusopetuksen oppimäärän paikallisille oppiaineille sallitaan aina arvosana S
- Nuorten perusopetuksen oppimäärän paikallisille oppiaineille sallitaan arvosana O jos oppiaineen laajuus on alle kaksi vuosiviikkotuntia

- Ammatillisen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut** tai **loma**
- IB-tutkinnon opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna** tai **valmistunut**
- Lukion opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- Lukioon valmistavan koulutuksen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- International school opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**
- Aikusten perusopetuksen opiskeluoikeuden opiskeluoikeusjaksoilta vaaditaan rahoitusmuoto (kenttä opintojenRahoitus), jos tila on **lasna**, **valmistunut**

## 30.1.2020
- Estetään käytöstä poistettujen lukiokoulutuksen erityisen koulutustehtävän koodiarvojen tallennus
  - Käytöstä poistetut koodiarvot: ib, kielijakansainvalisyys, matematiikka-luonnontiede-ymparisto-tekniikka, steiner, taide, urheilu, muu

## 28.1.2020
- Osittainen ammattitutkinto voidaan merkitä valmiiksi silloinkin kun suorituksissa on pelkkiä yleisiä aineita.

## 15.1.2020
- Ammatillisissa ops-muotoisissa tutkinnoissa voidaan antaa painotettu keskiarvosana.

## 14.1.2020
- Kun esiopetuksen opiskeluoikeuden järjrestämismuoto syötetään
  - pitää käyttäjän olla luotu organisaatioon joka on tyyppiä KOULUTUSTOIMIJA ja VARHAISKASVATUKSEN_JARJESTAJA
  - pitää päätason suorituksen tyypin olla päiväkodin esiopetus (001102)
- Jos käyttäjä on luotu kahteen tai useampaan organisaatioon jotka ovat tyyppiä KOULUTUSTOIMIJA ja VARHAISKASVATUKSEN_JARJESTAJA pitää opiskeluoikeuden koulutustoimija syöttää

## 13.11.2019
- Nuorten perusopetuksen oppimäärän valinnaisille kieli oppiaineille sallitaan arvosana S jos laajuus on 2 vuosiviikkotuntia tai yli

## 14.10.2019
- Nuorten perusopetuksen oppimäärän oppiaineelle Opinto-ohjaus (OP) sallitaan aina arvosana O

## 9.10.2019
- Nuorten perusopetuksen oppimäärän oppiaineelle Opinto-ohjaus (OP) sallitaan aina arvosana S
- validointi "Nuorten perusopetuksen oppimäärän oppiaineiden arvioinnit saavat olla 'S' ja 'O' vain valinnaisilla oppiaineilla joiden laajus on alle kaksi tai oppiaineilla jotka suoritetaan yksilöllistettynä" suoritetaan vain jos oppimäärä on vahvistettu

## 7.10.2019
- Näyttötutkintoon valmistavan suorituksen voi merkitä valmiiksi vaikka sillä ei ole osasuorituksia tai opiskeluoikeus ei ole linkitetty

## 3.10.2019
- Sallitaan lukion oppimäärän alla vain yksi matematiikan oppiainesuoritus

## 2.10.2019
- Nuorten perusopetuksen oppimäärän oppiaineiden arvioinnit saavat olla 'S' ja 'O' vain valinnaisilla oppiaineilla joiden laajus on alle kaksi tai oppiaineilla jotka suoritetaan yksilöllistettynä

## 24.9.2019
- Ammatillinen opiskeluoikeus, jonka suorituksella ei ole osasuorituksia, voidaan merkitä valmiiksi jos opiskeluoikeus on ostettu ja valmistunut ennen vuotta 2019

## 3.9.2019
- Oppiaineen `NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa` suorituksella tulee olla laajuus

## 2.9.2019
- `PerusopetukseenValmistavaOpetuksen` diaarinumeron validointi lisätty
- Diaarinumeron validoinnit:
  - `AikuistenPerusopetuksenAlkuvaihe`
  - `PerusopetuksenLisäopetus`
  - `LukioonValmistavaKoulutus`
  - `ValmaKoulutus`
  - `TelmaKoulutus`
  - `PerusopetukseenValmistavaOpetus`
- Perusopetuksen valmistavassa opetuksessa luokka-aste vaaditaan jos viimeisin arviointi on muuta kuin 0

## 26.8.2019
- Perusopetuksen vahvistetulla suorituksella tulee olla osasuorituksena vähintään yksi oppiaine
- Valmiiksi merkityllä ei-perusopetuksen päätason suorituksella tulee olla osasuorituksia tai opiskeluoikeuden pitää olla linkitetty

## 27.6.2019
- Suoritukselle tulee olla merkitty alkamispäivä
- Perusopetuksen vuosiluokan suorituksella tulee olla alkamispäivä

## 25.6.2019
- Kahdella saman IB-oppiaineen suorituksella ei molemmilla voi olla numeerista arviointia

## 20.6.2019
- Sisältävän opiskeluoikeuden tulee löytyä `oid`-arvolla

## 14.6.2019
- Valmiiksi merkityllä suorituksella ei voi olla keskeneräisiä osasuorituksia
- Sallitaan keskeneräinen yhteinen tutkinnon osa valmiissa osatutkintotavoitteisessa ammatillisen koulutuksen suorituksessa

## 31.5.2019
- Sallitaan tilan asettaminen valmiiksi mikäli opiskelijalla on yksikin suoritettu aineopinto
- Opiskeluoikeutta aikuistenperusopetus ei voi merkitä valmiiksi jos siltä puuttuu suoritus `aikuistenperusopetuksenoppimaara` tai `perusopetuksenoppiaineenoppimaara`

## 29.5.2019
- Sallitaan korkeakouluopinnon lisääminen ilman arviointia

## 28.5.2019
- Aikuisten perusopetuksen opiskeluoikeuden voi asettaa `valmistunut`-tilaan vaikka alkuvaiheen suorituksella ei ole vahvistusta

## 17.5.2019
- Näyttötutkintoon valmistavan tutkinnon diaarinumeron validointi lisätty

## 29.3.2019
- `Ei tiedossa`-oppiainetta (koulutusmoduulin tunnisteen koodiarvo = `XX`) ei voi merkitä valmiiksi

## 26.3.2019
- LukionOppiaineenOppimääränSuoritus: `Ei tiedossa`-oppiainetta (koulutusmoduulin tunnisteen koodiarvo = `XX`) ei voi merkitä valmiiksi

## 7.3.2019
- Opiskeluoikeuden päättymispäiväksi katsotaan myös päättävän jakson päättymispäivä

## 7.2.2019
- Esiopetuksen diaarinumeron validointi lisätty

## 15.1.2019
- Opiskeluoikeuden tilojen `valmistunut` sekä `eronnut` jälkeen ei voi esiintyä muita tiloja
- Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila
- Opiskeluoikeusjaksojen on oltava päivämääräjärjestyksessä
- Opiskeluoikeuden tila ei saa muuttua lopullisen tilan jälkeen
- Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila
- Opiskeluoikeusjaksojen on oltava päivämääräjärjestyksessä

## 14.1.2019
- Tutkinnon osalle ei saa merkitä tutkintoa samalla diaarinumerolla kuin tutkinnon suoritukselle
- Jos tutkinnon osa kuuluu päätason suorituksen tutkinnon eri perusteeseen saa siinä välittää tutkintotiedon

## 13.12.2018
- Valmiilla DIA:n päätason suorituksella tulee olla laajuus

## 27.8.2018
- Diaarinumero on pakollinen päätason suorituksilla
- Suorituksella lista hyväksyttyjä perusteiden koulutustyyppejä

## 23.8.2018
- Sallitaan arvioimattomat oppiaineet aikuisten perusopetuksen alkuvaiheessa

## 15.8.2018
- `KorkeakouluopinnotTutkinnonOsan` sekä `JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsan` rakenteet hyväksytään validoimatta

## 1.6.2018
- Sallitaan VALMA-koulutuksen opiskeluoikeuden päättymispäivä suorituksen vahvistuspäivän jälkeen

## 13.3.2018
- `NuortenPerusopetuksenOppiaineen` diaarinumeron validointi lisätty

## 21.2.2018
- Jos tutkintosuoritukselta puuttuu alkamispäivä, päättele siirtymäaika ensimmäisestä läsnä-opiskelujaksosta
- Ammatillinen päätason suoritus voi käyttää vain yhtä numeerista arviointiasteikkoa

## 20.2.2018
- `AikuistenPerusopetuksenOppiaineen` diaarinumeron validointi lisätty

## 12.2.2018
- Kun suoritetaan ammatillista tutkintoa näyttönä, voi tutkinnon vahvistus tulla opiskeluoikeuden päättymisen jälkeen
- Opiskeluoikeuden päättymispäivän ei voi olla ennen suorituksen vahvistuksen päivämäärää (pl. ammatillisen näyttötutkinto)

## 6.2.2018
- Reformin mukaisen suoritustavan siirtymäajan tarkistuksessa käytetään ensimmäistä läsnä-jaksoa tutkintosuorituksen alkamispäivän sijaan

## 18.1.2018
- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin sen arviointipäivä
- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin sen alkamispäivä

## 26.10.2017
- Päätason suoritus ei saa sisältää duplikaatteja osasuorituksia

## 5.10.2017
- Opiskeluoikeuden alkamispäivän tulee olla sama kuin ensimmäisen opiskeluoikeusjakson alkupäivä

## 4.10.2017
- Suorituksen vahvistuksen päivämäärä ei voi olla aiempi kuin suorituksen osan vahvistuksen päivämäärä

## 29.9.2017
- Suorituksella tulee olla vahvistus mikäli se on tilassa `valmistunut`
- Vahvistetulla suorituksella tulee olla arviointi
- Vahvistetulle 9. vuosiluokan suoritukselle ei voi syöttää oppiaineita mikäli oppilas ei ole jäänyt luokalle

## 24.8.2017
- Tutkinnon osan ryhmä on pakollinen ammatillisen perustutkinnon tutkinnon osille (pl. näyttötutkinto)

## 3.7.2017
- Tutkinnon perusteen tulee löytyä diaarinumerolla
- Tutkinnon osan ryhmä voidaan määritellä vain ammatillisen perustutkinnon tutkinnon osille

## 26.6.2017
- Sisältävän opiskeluoikeuden oppilaitoksen `oid`:n tulee olla oikea
- Sisältävän opiskeluoikeuden henkilö-`oid`:n tulee vastata syötettyjä henkilötietoja tai henkilö tulee löytyä annetulla henkilötunnuksella

## 29.5.2017
- Opiskeluoikeuden päättymispäivän tulee olla alkamispäivän jälkeen
- Opiskeluoikeuden arvioidun päättymispäivän tulee olla alkamispäivän jälkeen
- Suorituksen arviointipäivä ei voi olla aiempi kuin sen alkamispäivä

## 31.5.2016
- Osasuorituksilla tulee olla sama laajuusyksillö kuin ylemmän tason suorituksella
- Osasuoritusten laajuuksien summan tulee olla sama kuin suorituksen laajuuden

## 25.5.2016
- Osaamisalan koodiarvon tulee löytyä tutkintorakenteesta annetulle perusteelle

## 12.4.2016
- Tutkinnon osan tulee löytyä tutkintorakenteesta annetulle perusteelle
