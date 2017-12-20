import React from 'baret'
import R from 'ramda'
import Bacon from 'baconjs'
import {addContext, modelData, modelItems, modelLookup, modelTitle, modelSetValues, pushModel} from '../editor/EditorModel.js'
import {TogglableEditor} from '../editor/TogglableEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {OpiskeluoikeudenTilaEditor} from './OpiskeluoikeudenTilaEditor'
import Versiohistoria from './Versiohistoria'
import {yearFromIsoDateString} from '../date/date'
import {ExpandablePropertiesEditor} from '../editor/ExpandablePropertiesEditor'
import {Editor} from '../editor/Editor'
import {navigateTo} from '../util/location'
import {suorituksenTyyppi, suoritusTitle} from '../suoritus/Suoritus'
import Text from '../i18n/Text'
import {assignTabNames, suoritusTabIndex, SuoritusTabs, urlForTab} from '../suoritus/SuoritusTabs'
import {Korkeakoulusuoritukset} from '../virta/Korkeakoulusuoritukset'

export const OpiskeluoikeusEditor = ({model}) => {
  let oid = modelData(model, 'oid')
  model = addContext(model, {opiskeluoikeus: model})
  return (<TogglableEditor model={model} renderChild={ (mdl, editLink) => {
    let context = mdl.context
    let excludedProperties = ['suoritukset', 'alkamispäivä', 'arvioituPäättymispäivä', 'päättymispäivä', 'oppilaitos', 'lisätiedot']

    const alkuChangeBus = Bacon.Bus()
    alkuChangeBus.onValue(v => {
      const value = v[0].value
      pushModel(modelSetValues(model, {'alkamispäivä' : value, 'tila.opiskeluoikeusjaksot.0.alku': value}))
    })
    let hasOppilaitos = !!modelData(mdl, 'oppilaitos')

    return (
      <div className="opiskeluoikeus">
        <h3>
          <span className="otsikkotiedot">
            { hasOppilaitos && <span className="oppilaitos inline-text">{modelTitle(mdl, 'oppilaitos')}{','}</span> }
            <span className="koulutus inline-text" style={hasOppilaitos ? { 'text-transform': 'lowercase' } : undefined}>{(näytettävätPäätasonSuoritukset(model)[0] || {}).title}</span>
            { modelData(mdl, 'alkamispäivä')
              ? <span className="inline-text">{'('}
                <span className="alku pvm">{yearFromIsoDateString(modelTitle(mdl, 'alkamispäivä'))}</span>{'-'}
                <span className="loppu pvm">{yearFromIsoDateString(modelTitle(mdl, 'päättymispäivä'))}{', '}</span>
                <span className="tila">{modelTitle(mdl, 'tila.opiskeluoikeusjaksot.-1.tila').toLowerCase()}{')'}</span>
                </span>
              : null
            }
          </span>
          {!model.context.kansalainen && <Versiohistoria opiskeluoikeusOid={oid} oppijaOid={context.oppijaOid}/>}
          {!model.context.kansalainen && <OpiskeluoikeudenId opiskeluoikeus={mdl}/>}
        </h3>
        <div className={mdl.context.edit ? 'opiskeluoikeus-content editing' : 'opiskeluoikeus-content'}>
          <div className="opiskeluoikeuden-tiedot">
            {editLink}
            <OpiskeluoikeudenOpintosuoritusoteLink opiskeluoikeus={mdl}/>
            {
              modelData(mdl, 'alkamispäivä') && <OpiskeluoikeudenVoimassaoloaika opiskeluoikeus={mdl}/>
            }
            <PropertiesEditor
              model={mdl}
              propertyFilter={ p => !excludedProperties.includes(p.key) }
              getValueEditor={ (prop, getDefault) => prop.key === 'tila'
                ? <OpiskeluoikeudenTilaEditor model={mdl} alkuChangeBus={alkuChangeBus}/>
                : getDefault() }
             />
            {
              modelLookup(mdl, 'lisätiedot') && <ExpandablePropertiesEditor model={mdl} propertyName="lisätiedot" propertyFilter={prop => context.edit || modelData(prop.model) !== false} />
            }
          </div>
          <Suoritukset opiskeluoikeus={mdl}/>
        </div>
      </div>)
    }
  } />)
}

const OpiskeluoikeudenId = ({opiskeluoikeus}) => {
  let selectAllText = (e) => {
    let el = e.target
    var range = document.createRange()
    range.selectNodeContents(el)
    var sel = window.getSelection()
    sel.removeAllRanges()
    sel.addRange(range)
  }
  return <span className="id"><Text name="Oid"/>{': '}<span className="value" onClick={selectAllText}>{modelData(opiskeluoikeus, 'oid')}</span></span>
}

const OpiskeluoikeudenVoimassaoloaika = ({opiskeluoikeus}) => {
  let päättymispäiväProperty = (modelData(opiskeluoikeus, 'arvioituPäättymispäivä') && !modelData(opiskeluoikeus, 'päättymispäivä')) ? 'arvioituPäättymispäivä' : 'päättymispäivä'
  return (<div className="alku-loppu opiskeluoikeuden-voimassaoloaika">
    <Text name="Opiskeluoikeuden voimassaoloaika"/>{': '}
    <span className="alkamispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path="alkamispäivä"/></span>
    {' — '}
    <span className="päättymispäivä"><Editor model={addContext(opiskeluoikeus, {edit: false})} path={päättymispäiväProperty} /></span>
    {' '}
    {päättymispäiväProperty == 'arvioituPäättymispäivä' && <Text name="(arvioitu)"/>}
  </div>)
}

const Suoritukset = ({opiskeluoikeus}) => {
  const opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo

  return (
    <div className="suoritukset">
      {opiskeluoikeusTyyppi === 'korkeakoulutus'
        ? <Korkeakoulusuoritukset opiskeluoikeus={opiskeluoikeus}/>
        : <TabulatedSuoritukset model={opiskeluoikeus}/>
       }
    </div>
  )
}

const TabulatedSuoritukset = ({model}) => {
  const suoritukset = modelItems(model, 'suoritukset')
  assignTabNames(suoritukset)

  const index = suoritusTabIndex(suoritukset)
  if (index < 0 || index >= suoritukset.length) {
    navigateTo(urlForTab(suoritukset, index))
    return null
  }
  const valittuSuoritus = suoritukset[index]

  return (
    <div className="suoritukset">
      <h4><Text name="Suoritukset"/></h4>
      <SuoritusTabs model={model} suoritukset={suoritukset}/>
      <Editor key={valittuSuoritus.tabName} model={valittuSuoritus} alwaysUpdate="true" />
    </div>
  )
}

class OpiskeluoikeudenOpintosuoritusoteLink extends React.Component {
  render() {
    let {opiskeluoikeus} = this.props
    let oppijaOid = opiskeluoikeus.context.oppijaOid
    var opiskeluoikeusTyyppi = modelData(opiskeluoikeus, 'tyyppi').koodiarvo
    if (opiskeluoikeusTyyppi === 'lukiokoulutus' || opiskeluoikeusTyyppi === 'ibtutkinto') { // lukio/ib näytetään opiskeluoikeuskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?opiskeluoikeus=' + modelData(opiskeluoikeus, 'oid')
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else if (opiskeluoikeusTyyppi === 'korkeakoulutus') { // korkeakoulutukselle näytetään oppilaitoskohtainen suoritusote
      let href = '/koski/opintosuoritusote/' + oppijaOid + '?oppilaitos=' + modelData(opiskeluoikeus, 'oppilaitos').oid
      return <a className="opintosuoritusote" href={href}><Text name="näytä opintosuoritusote"/></a>
    } else {
      return null
    }
  }
}

let näytettäväPäätasonSuoritus = s => !['perusopetuksenvuosiluokka'].includes(modelData(s).tyyppi.koodiarvo)

export const näytettävätPäätasonSuoritukset = (opiskeluoikeus) => {
  let päätasonSuoritukset = modelItems(opiskeluoikeus, 'suoritukset').filter(näytettäväPäätasonSuoritus)
  let makeGroupTitle = (suoritus) => {
    switch (suorituksenTyyppi(suoritus)) {
      case 'perusopetuksenoppiaineenoppimaara': return 'oppiainetta'
      case 'korkeakoulunopintojakso': return 'opintojaksoa'
      default: return ''
    }
  }

  let grouped = R.toPairs(R.groupBy(makeGroupTitle, päätasonSuoritukset)).map(([groupTitle, suoritukset]) => {
    let title = groupTitle && (suoritukset.length > 1)
      ? <span>{suoritukset.length}{' '}<Text name={groupTitle}/></span>
      : suoritusTitle(suoritukset[0])
    return { title, suoritukset }
  })
  return grouped
}