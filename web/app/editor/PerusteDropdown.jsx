import React from 'baret'
import Http from '../http'
import Bacon from 'baconjs'
import Dropdown from '../Dropdown.jsx'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'
import {t} from '../i18n'
import {koulutustyyppiKoodi} from './Suoritus'

const preferred = ['OPH-1280-2017', '104/011/2014']

export const PerusteDropdown = ({suoritusTyyppiP, perusteAtom}) => {
  let diaarinumerotP = suoritusTyyppiP.doLog('suoritustyyppi').flatMapLatest(tyyppi =>  !tyyppi ? Bacon.never() : diaarinumerot(tyyppi)).toProperty().doLog('diaarinumerot')
  let selectedOptionP = Bacon.combineWith(diaarinumerotP, perusteAtom, (options, selected) => options.find(o => o.koodiarvo == selected))
  let selectOption = (option) => {
    perusteAtom.set(option && option.koodiarvo)
  }

  diaarinumerotP.onValue(options => {
    let current = perusteAtom.get()
    if (!current || !options.map(k => k.koodiarvo).includes(current)) {
      selectOption(options.find(k => preferred.includes(k.koodiarvo)) || options[0])
    }
  })

  return (<span>
    { elementWithLoadingIndicator(diaarinumerotP.map(diaarinumerot => diaarinumerot.length > 1
        ? <Dropdown
            options={diaarinumerotP}
            keyValue={option => option.koodiarvo}
            displayValue={option => option.koodiarvo + ' ' + t(option.nimi)}
            onSelectionChanged={selectOption}
            selected={selectedOptionP}/>
        : <input type="text" disabled value={perusteAtom}></input>
    ))}
  </span>)
}

export const diaarinumerot = suoritusTyyppi => {
  let koulutustyyppi = koulutustyyppiKoodi(suoritusTyyppi.koodiarvo)
  return koulutustyyppi ? Http.cachedGet(`/koski/api/tutkinnonperusteet/diaarinumerot/koulutustyyppi/${koulutustyyppi}`) : []
}
