import {modelData, modelLens, modelSetValue, modelItems} from './EditorModel'
import * as L from 'partial.lenses'

export const suoritusValmis = (suoritus) => tilanKoodiarvo(suoritus) === 'VALMIS'
export const suoritusKesken = (suoritus) => tilanKoodiarvo(suoritus) === 'KESKEN'
const tilanKoodiarvo = (suoritus) => modelData(suoritus, 'tila').koodiarvo
export const hasArvosana = (suoritus) => !!modelData(suoritus, 'arviointi.-1.arvosana')
export const arvosanaLens = modelLens('arviointi.-1.arvosana')
export const lastArviointiLens = modelLens('arviointi.-1')
export const tilaLens = modelLens('tila')
export const setTila = (suoritus, koodiarvo) => {
  let t = modelSetValue(L.get(tilaLens, suoritus), createTila(koodiarvo))
  return L.set(tilaLens, t, suoritus)
}
export const onKeskeneräisiäOsasuorituksia  = (suoritus) => {
  return modelItems(suoritus, 'osasuoritukset').find(suoritusKesken) != undefined
}

const createTila = (koodiarvo) => {
  if (!tilat[koodiarvo]) throw new Error('tila puuttuu: ' + koodiarvo)
  return tilat[koodiarvo]
}

const tilat = {
  VALMIS: { data: { koodiarvo: 'VALMIS', koodistoUri: 'suorituksentila' }, title: 'Suoritus valmis' }
}