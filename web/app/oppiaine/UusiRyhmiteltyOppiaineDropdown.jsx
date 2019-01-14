import React from 'baret'
import Bacon from 'baconjs'
import {t} from '../i18n/i18n'
import Http from '../util/http'
import {UusiOppiaineDropdown} from './UusiOppiaineDropdown'
import {
  ensureArrayKey, modelData, modelItems, modelLookup, modelSet, modelSetData, modelSetTitle, modelSetValue,
  pushModel
} from '../editor/EditorModel'
import {createOppiaineenSuoritus} from '../lukio/lukio'

const resolveRyhmäFieldName = model => {
  const tyyppi = modelData(model, 'tyyppi').koodiarvo
  return ['diavalmistavavaihe', 'diatutkintovaihe'].includes(tyyppi) ? 'osaAlue' : 'ryhmä'
}

export const UusiRyhmiteltyOppiaineDropdown = ({model, aineryhmä, optionsFilter}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = oppiaine => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const ryhmäFieldName = resolveRyhmäFieldName(model)
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const oppiaineWithAineryhmä = modelLookup(oppiaineWithTitle, ryhmäFieldName) ? modelSetData(oppiaineWithTitle, aineryhmä, ryhmäFieldName) : oppiaineWithTitle
    const oppiaineenOsasuoritukset = (suoritustyyppi) =>
      suoritustyyppi ? Http.cachedGet(`/koski/api/editor/koodit/oppiaineetdia/201101/suoritukset/prefill?tyyppi=${suoritustyyppi.koodiarvo}`) : Bacon.constant([])
    oppiaineenOsasuoritukset(modelData(model, 'tyyppi'))
      .onValue(osasuorituksetTemplate => {
        const suoritusUudellaOppiaineella = modelSet(
          oppiaine.parent || createOppiaineenSuoritus(model),
          oppiaineWithAineryhmä,
          'koulutusmoduuli'
        )
        const osasuorituksilla = modelSetValue(suoritusUudellaOppiaineella, osasuorituksetTemplate.value, 'osasuoritukset')

        pushModel(osasuorituksilla, model.context.changeBus)
        ensureArrayKey(osasuorituksilla)
      })
  }

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritukset={[createOppiaineenSuoritus(model)]}
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
      optionsFilter={optionsFilter}
    />
  )
}
