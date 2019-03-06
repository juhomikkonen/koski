import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelData} from '../editor/EditorModel.js'
import {
  ensureArrayKey,
  modelLookup,
  modelSet,
  modelSetData,
  modelSetTitle,
  modelSetValue,
  modelSetValues,
  pushModel
} from '../editor/EditorModel'
import * as R from 'ramda'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {toKoodistoEnumValue} from '../koodisto/koodistot'
import {isPaikallinen, koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {ift} from '../util/util'
import ModalDialog from '../editor/ModalDialog'
import {
  createTutkinnonOsanSuoritusPrototype,
  fetchLisättävätTutkinnonOsat,
  isYhteinenTutkinnonOsa,
  placeholderForNonGrouped,
  tutkinnonOsanOsaAlueenKoulutusmoduuli
} from './TutkinnonOsa'
import {elementWithLoadingIndicator} from '../components/AjaxLoadingIndicator'
import {koodistoValues} from '../uusioppija/koodisto'
import {
  isMuunAmmatillisenKoulutuksenSuoritus,
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
} from '../suoritus/SuoritustaulukkoCommon'
import TutkinnonOsaToisestaTutkinnostaPicker from './TutkinnonOsaToisestaTutkinnostaPicker'
import LisaaTutkinnonOsaDropdown from './LisaaTutkinnonOsaDropdown'

export default ({ suoritus, groupId, suoritusPrototype, suorituksetModel, setExpanded, groupTitles }) => {
  let koulutusModuuliprotos = koulutusModuuliprototypes(suoritusPrototype)
  let paikallinenKoulutusmoduuli = koulutusModuuliprotos.find(isPaikallinen)
  let valtakunnallisetKoulutusmoduulit = koulutusModuuliprotos.filter(R.complement(isPaikallinen))

  let koulutusmoduuliProto = selectedItem => selectedItem && isYhteinenTutkinnonOsa(suoritus)
    ? tutkinnonOsanOsaAlueenKoulutusmoduuli(valtakunnallisetKoulutusmoduulit, selectedItem.data)
    : valtakunnallisetKoulutusmoduulit[0]

  let diaarinumero = modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero') || modelData(suoritus, 'tutkinto.perusteenDiaarinumero')
  let suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')

  let osatP = diaarinumero
    ? fetchLisättävätTutkinnonOsat(diaarinumero, suoritustapa, groupId)
    : isYhteinenTutkinnonOsa(suoritus)
      ? koodistoValues('ammatillisenoppiaineet').map(oppiaineet => { return {osat: oppiaineet, paikallinenOsa: true, osanOsa: true} })
      : Bacon.constant({osat:[], paikallinenOsa: true})

  return (<span>
    {
      elementWithLoadingIndicator(
        osatP.map(lisättävätTutkinnonOsat => {
            return (<div>
              <LisääRakenteeseenKuuluvaTutkinnonOsa {...{ addTutkinnonOsa, lisättävätTutkinnonOsat, koulutusmoduuliProto, groupId}} />
              <LisääOsaToisestaTutkinnosta {...{addTutkinnonOsa, lisättävätTutkinnonOsat, suoritus, koulutusmoduuliProto, groupId, diaarinumero}}/>
              <LisääPaikallinenTutkinnonOsa {...{lisättävätTutkinnonOsat, addTutkinnonOsa, paikallinenKoulutusmoduuli, groupId}}/>
            </div>)
          }
        )
      )
    }
  </span>)

  function addTutkinnonOsa(koulutusmoduuli, tutkinto, liittyyTutkinnonOsaan) {
    if (groupId == placeholderForNonGrouped) groupId = undefined

    let uusiSuoritus = modelSet(createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId), koulutusmoduuli, 'koulutusmoduuli')
    if (groupId) {
      uusiSuoritus = modelSetValue(uusiSuoritus, toKoodistoEnumValue('ammatillisentutkinnonosanryhma', groupId, groupTitles[groupId]), 'tutkinnonOsanRyhmä')
    }
    if (tutkinto && modelLookup(uusiSuoritus, 'tutkinto')) {
      uusiSuoritus = modelSetData(uusiSuoritus, {
        tunniste: { koodiarvo: tutkinto.tutkintoKoodi, nimi: tutkinto.nimi, koodistoUri: 'koulutus' },
        perusteenDiaarinumero: tutkinto.diaarinumero
      }, 'tutkinto')
    }

    if (liittyyTutkinnonOsaan && modelLookup(uusiSuoritus, 'liittyyTutkinnonOsaan')) {
      uusiSuoritus = modelSetData(uusiSuoritus, liittyyTutkinnonOsaan.data, 'liittyyTutkinnonOsaan')
    }
    pushModel(ensureArrayKey(uusiSuoritus))
    setExpanded(uusiSuoritus)(true)
  }
}

const LisääRakenteeseenKuuluvaTutkinnonOsa = ({lisättävätTutkinnonOsat, addTutkinnonOsa, koulutusmoduuliProto}) => {
  let selectedAtom = Atom(undefined)
  selectedAtom.filter(R.identity).onValue((newItem) => {
    addTutkinnonOsa(modelSetTitle(modelSetValues(koulutusmoduuliProto(newItem), { tunniste: newItem }), newItem.title))
  })
  let osat = lisättävätTutkinnonOsat.osat
  return osat.length > 0 && (<span className="osa-samasta-tutkinnosta">
      <LisaaTutkinnonOsaDropdown selectedAtom={selectedAtom} osat={osat} placeholder={lisättävätTutkinnonOsat.osanOsa ? t('Lisää tutkinnon osan osa-alue') : t('Lisää tutkinnon osa')}/>
  </span>)
}

const LisääPaikallinenTutkinnonOsa = ({lisättävätTutkinnonOsat, addTutkinnonOsa, paikallinenKoulutusmoduuli}) => {
  let lisääPaikallinenAtom = Atom(false)
  let lisääPaikallinenTutkinnonOsa = (osa) => {
    lisääPaikallinenAtom.set(false)
    if (osa) {
      addTutkinnonOsa(osa, undefined, liittyyTutkinnonOsaanAtom.get())
    }
  }
  const nameAtom = Atom('')
  const tutkintoAtom = Atom()
  const liittyyTutkinnonOsaanAtom = Atom()
  const selectedAtom = nameAtom
    .view(name => modelSetTitle(modelSetValues(paikallinenKoulutusmoduuli, { 'kuvaus.fi': { data: name}, 'tunniste.nimi.fi': { data: name}, 'tunniste.koodiarvo': { data: name } }), name))

  const tutkinnonosaaPienempiKokonaisuus = isTutkinnonosaaPienempiKokonaisuus(paikallinenKoulutusmoduuli)
  const validP = tutkinnonosaaPienempiKokonaisuus ? nameAtom.and(liittyyTutkinnonOsaanAtom.map('.data')) : nameAtom

  const texts = lisääTutkinnonOsaTexts(lisättävätTutkinnonOsat, paikallinenKoulutusmoduuli)
  return (<span className="paikallinen-tutkinnon-osa">
    {
      lisättävätTutkinnonOsat.paikallinenOsa && <a className="add-link" onClick={() => lisääPaikallinenAtom.set(true)}>
        <Text name={texts.lisääOsaLink}/>
      </a>
    }
    { ift(lisääPaikallinenAtom, (<ModalDialog className="lisaa-paikallinen-tutkinnon-osa-modal" onDismiss={lisääPaikallinenTutkinnonOsa} onSubmit={() => lisääPaikallinenTutkinnonOsa(selectedAtom.get())} okTextKey={texts.modalOk} validP={validP}>
        <h2><Text name={texts.modalHeader} /></h2>
        {
          tutkinnonosaaPienempiKokonaisuus &&
          <TutkinnonOsaToisestaTutkinnostaPicker
            tutkintoAtom={tutkintoAtom}
            tutkinnonOsaAtom={liittyyTutkinnonOsaanAtom}
            oppilaitos={modelData(paikallinenKoulutusmoduuli.context.suoritus, 'toimipiste')}
            tutkintoTitle='Liittyy tutkintoon'
            tutkinnonOsaTitle='Liittyy tutkinnon osaan'
          />
        }
        <label>
          <Text name={texts.modalFieldLabel} />
          <input className='paikallinen-koulutusmoduuli-nimi' type="text" autoFocus={!tutkinnonosaaPienempiKokonaisuus} onChange={event => nameAtom.set(event.target.value)}/>
        </label>
      </ModalDialog>)
    ) }
  </span>)
}

const lisääTutkinnonOsaTexts = (lisättävätTutkinnonOsat, paikallinenKoulutusmoduuli) => {
  if (lisättävätTutkinnonOsat.osanOsa) {
    return {
      lisääOsaLink: 'Lisää paikallinen tutkinnon osan osa-alue',
      modalHeader: 'Paikallisen tutkinnon osan osa-alueen lisäys',
      modalFieldLabel: 'Tutkinnon osan osa-alueen nimi',
      modalOk: 'Lisää tutkinnon osan osa-alue'
    }
  } else if (paikallinenKoulutusmoduuli && isMuunAmmatillisenKoulutuksenSuoritus(paikallinenKoulutusmoduuli.context.suoritus)) {
    return {
      lisääOsaLink: 'Lisää osasuoritus',
      modalHeader: 'Osasuorituksen lisäys',
      modalFieldLabel: 'Osasuorituksen nimi',
      modalOk: 'Lisää osasuoritus'
    }
  } else if (paikallinenKoulutusmoduuli && isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(paikallinenKoulutusmoduuli.context.suoritus)) {
    return {
      lisääOsaLink: 'Lisää tutkinnon osaa pienemmän kokonaisuuden suoritus',
      modalHeader: 'Tutkinnon osaa pienemmän kokonaisuuden lisäys',
      modalFieldLabel: 'Tutkinnon osaa pienemmän kokonaisuuden nimi',
      modalOk: 'Lisää tutkinnon osaa pienempi kokonaisuus'
    }
  } else {
    return {
      lisääOsaLink: 'Lisää paikallinen tutkinnon osa',
      modalHeader: 'Paikallisen tutkinnon osan lisäys',
      modalFieldLabel: 'Tutkinnon osan nimi',
      modalOk: 'Lisää tutkinnon osa'
    }
  }
}

const LisääOsaToisestaTutkinnosta = ({lisättävätTutkinnonOsat, suoritus, koulutusmoduuliProto, addTutkinnonOsa, diaarinumero}) => {
  let oppilaitos = modelData(suoritus, 'toimipiste')
  let lisääOsaToisestaTutkinnostaAtom = Atom(false)
  let lisääOsaToisestaTutkinnosta = (tutkinto, osa) => {
    lisääOsaToisestaTutkinnostaAtom.set(false)
    if (osa) {
      addTutkinnonOsa(modelSetTitle(modelSetValues(koulutusmoduuliProto(), { tunniste: osa }), osa.title), tutkinto.diaarinumero != diaarinumero && tutkinto)
    }
  }
  let tutkintoAtom = Atom()
  let tutkinnonOsaAtom = Atom()
  tutkintoAtom.onValue(() => tutkinnonOsaAtom.set(undefined))

  return (<span className="osa-toisesta-tutkinnosta">
    {
      lisättävätTutkinnonOsat.osaToisestaTutkinnosta && <a className="add-link" onClick={() => lisääOsaToisestaTutkinnostaAtom.set(true)}>
        <Text name="Lisää tutkinnon osa toisesta tutkinnosta"/>
      </a>
    }
    { ift(lisääOsaToisestaTutkinnostaAtom, <ModalDialog className="lisaa-tutkinnon-osa-toisesta-tutkinnosta-modal" onDismiss={lisääOsaToisestaTutkinnosta} onSubmit={() => lisääOsaToisestaTutkinnosta(tutkintoAtom.get(), tutkinnonOsaAtom.get())} okTextKey="Lisää tutkinnon osa" validP={tutkinnonOsaAtom} submitOnEnterKey="false">
      <h2><Text name="Tutkinnon osan lisäys toisesta tutkinnosta"/></h2>
      <TutkinnonOsaToisestaTutkinnostaPicker {...{tutkintoAtom, tutkinnonOsaAtom, oppilaitos}} />
    </ModalDialog>)
    }
  </span>)
}

const isTutkinnonosaaPienempiKokonaisuus = k => k && k.value && k.value.classes[0] === 'tutkinnonosaapienempikokonaisuus'
