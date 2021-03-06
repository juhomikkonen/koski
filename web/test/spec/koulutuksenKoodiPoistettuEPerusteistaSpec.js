describe('Koulutuksen koodi poistettu ePerusteista', function () {
  before(
    Authentication().login(),
    resetFixtures
  )

  function suorituksenKoulutusKenttä() {
    return extractAsText(S('.suoritus > .properties > table > tbody > .koulutusmoduuli'))
  }

  function valitseIdeksistä(indeksi) {
    return seq(
      click('.tunniste-koodiarvo > span > span > .dropdown > .select'),
      click('.tunniste-koodiarvo > span > span > .dropdown > .options li:nth-child(' + indeksi + ')')
    )
  }

  function vaihtoehdot() {
    return extractAsText(findSingle('.tunniste-koodiarvo > span > span > .dropdown'))
  }

  var page = KoskiPage()
  var opinnot = OpinnotPage()
  var editor = opinnot.opiskeluoikeusEditor()

  describe('Vanhentunut koulutuskoodi, ei löydy enää ePerusteista', function () {

    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('161097-132N')
    )

    describe('Ennen muokkaamista', function () {
      it('Näytetään vanha koodi', function () {
        expect(suorituksenKoulutusKenttä()).to.equalIgnoreNewlines(
          'Koulutus Autoalan perustutkinto 123456 39/011/2014'
        )
      })
    })

    describe('Editointi-tilasssa', function () {
      before(editor.edit, editor.saveChanges)

      it('Koodiarvo päivitetään automaattisesti', function () {
        expect(suorituksenKoulutusKenttä()).to.equalIgnoreNewlines(
          'Koulutus Autoalan perustutkinto 351301 39/011/2014'
        )
      })
    })
  })

  describe('Monta koulutuskoodia', function () {

    before(
      Authentication().login(),
      page.openPage,
      page.oppijaHaku.searchAndSelect('151099-036E')
    )

    describe('Katselutilassa', function () {
      it('Näytetään valittu', function () {
        expect(suorituksenKoulutusKenttä()).to.equalIgnoreNewlines(
          'Koulutus Puuteollisuuden perustutkinto 351741 OPH-2455-2017'
        )
      })
    })

    describe('Muokkaustilassa', function () {
      before(editor.edit)

      describe('Näytetään vaihtehdot', function () {
        it('toimii', function () {
          expect(vaihtoehdot()).to.equalIgnoreNewlines(
            '351741\n' +
            '351741\n' +
            '451741'
          )
        })
      })

      describe('Voidaan vaihtaa koulutuskoodia', function () {

        before(
          valitseIdeksistä(2),
          editor.saveChanges
        )

        it('koulutuskoodi on vaihtunut', function () {
          expect(suorituksenKoulutusKenttä()).to.equalIgnoreNewlines(
            'Koulutus Puuteollisuuden perustutkinto 451741 OPH-2455-2017'
          )
        })
      })
    })
  })
})
