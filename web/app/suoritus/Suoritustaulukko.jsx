import React from 'baret'
import {modelData, modelLookup} from '../editor/EditorModel.js'
import {Editor} from '../editor/Editor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {modelErrorMessages, modelItems, modelTitle, pushRemoval} from '../editor/EditorModel'
import {buildClassNames} from '../components/classnames'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import {suoritusValmis, tilaText} from './Suoritus'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {fetchLaajuudet, YhteensäSuoritettu} from './YhteensaSuoritettu'
import UusiTutkinnonOsa from '../ammatillinen/UusiTutkinnonOsa'
import {
  isValinnanMahdollisuus,
  isVälisuoritus,
  isYhteinenTutkinnonOsa,
  osanOsa,
  selectTutkinnonOsanSuoritusPrototype,
  tutkinnonOsaPrototypes
} from '../ammatillinen/TutkinnonOsa'
import {sortLanguages} from '../util/sorting'
import {isKieliaine} from './Koulutusmoduuli'
import {flatMapArray} from '../util/util'
import {
  ArvosanaColumn,
  getLaajuusYksikkö,
  groupSuoritukset,
  isAmmatillinentutkinto,
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus,
  isNäyttötutkintoonValmistava,
  isYlioppilastutkinto,
  KoepisteetColumn,
  LaajuusColumn,
  suoritusProperties,
  TutkintokertaColumn
} from './SuoritustaulukkoCommon'
import LiittyyTutkinnonOsaanEditor from '../ammatillinen/LiittyyTutkinnonOsaanEditor'
import {UusiTutkinnonOsaMuuAmmatillinen} from '../muuammatillinen/UusiTutkinnonOsaMuuAmmatillinen'
import {isMuutaAmmatillistaPäätasonSuoritus} from '../muuammatillinen/MuuAmmatillinen'

const MAX_NESTED_LEVEL = 2

export class Suoritustaulukko extends React.Component {
  render() {
    let {suorituksetModel, parentSuoritus, nestedLevel = 0} = this.props
    let context = suorituksetModel.context
    parentSuoritus = parentSuoritus || context.suoritus
    let suoritukset = modelItems(suorituksetModel) || []

    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel)
    let suoritusProto = context.edit ? selectTutkinnonOsanSuoritusPrototype(suoritusProtos) : suoritukset[0]
    let suoritustapa = modelData(parentSuoritus, 'suoritustapa')
    if (suoritukset.length === 0 && !context.edit) return null

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0 || isVälisuoritus(s),
      component: this
    })

    const groupsP = groupSuoritukset(parentSuoritus, suoritukset, context, suoritusProto)


    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProto)
    let showTila = !isNäyttötutkintoonValmistava(parentSuoritus)
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)
    let columns = [TutkintokertaColumn, SuoritusColumn, LaajuusColumn, KoepisteetColumn, ArvosanaColumn].filter(column => column.shouldShow({parentSuoritus, suorituksetModel, suoritukset, context}))

    return !suoritustapa && context.edit && isAmmatillinentutkinto(parentSuoritus)
        ? <Text name="Valitse ensin tutkinnon suoritustapa" />
        : (suoritukset.length > 0 || context.edit) && (
          <div className="suoritus-taulukko">
            <table>
              <thead>
              <tr>
                <th className="suoritus">
                  {showExpandAll &&
                  <div>
                    {allExpandedP.map(allExpanded => (
                      <button className={'expand-all koski-button' + (allExpanded ? ' expanded' : '')} onClick={toggleExpandAll}>
                        <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
                      </button>)
                    )}
                  </div>
                  }
                </th>
              </tr>
              </thead>
              {
                groupsP.map(groups => flatMapArray(groups.groupIds, (groupId, i) => suoritusGroup(groups, groupId, i)))
              }
            </table>
          </div>)

    function suoritusGroup(groups, groupId, i) {
      const items = (groups.grouped[groupId] || [])
      const groupTitles = groups.groupTitles
      const UusiTutkinnonOsaComponent = isMuutaAmmatillistaPäätasonSuoritus(parentSuoritus)
        ? UusiTutkinnonOsaMuuAmmatillinen
        : UusiTutkinnonOsa

      return [
        <tbody key={'group-' + i} className={`group-header ${groupId}`}>
          <tr>
            { nestedLevel > 0 && items.length === 0 ? null : columns.map(column => column.renderHeader({parentSuoritus, suoritusProto, laajuusYksikkö, groupTitles, groupId})) }
          </tr>
        </tbody>,
        items.map((suoritus, j) => suoritusEditor(suoritus, i * 100 + j, groupId)),
        context.edit && nestedLevel < MAX_NESTED_LEVEL && <tbody key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId}>
          <tr>
            <td colSpan="4">
              <UusiTutkinnonOsaComponent suoritus={parentSuoritus}
                                         suoritusPrototypes={suoritusProtos}
                                         suorituksetModel={suorituksetModel}
                                         groupId={groupId}
                                         setExpanded={setExpanded}
                                         groupTitles={groupTitles}
              />
            </td>
          </tr>
        </tbody>,
        nestedLevel === 0 && !isNäyttötutkintoonValmistava(parentSuoritus) && !isYlioppilastutkinto(parentSuoritus) && <tbody key={'group- '+ i + '-footer'} className="yhteensä">
          <tr><td>
            <YhteensäSuoritettu suoritukset={items} laajuusP={fetchLaajuudet(parentSuoritus, groups.groupIds).map(l => l[groupId])} laajuusYksikkö={laajuusYksikkö}/>
          </td></tr>
        </tbody>
      ]
    }

    function suoritusEditor(suoritus, key, groupId) {
      return (<TutkinnonOsanSuoritusEditor baret-lift
                                           model={suoritus} showScope={!samaLaajuusYksikkö} showTila={showTila}
                                           expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                           groupId={groupId} columns={columns} nestedLevel={nestedLevel + 1} />)
    }
  }
}

export class TutkinnonOsanSuoritusEditor extends React.Component {
  render() {
    let {model, showScope, showTila, onExpand, expanded, groupId, columns, nestedLevel} = this.props
    let properties = suoritusProperties(model)
    let displayProperties = properties.filter(p => p.key !== 'osasuoritukset')
    let osasuoritukset = modelLookup(model, 'osasuoritukset')
    let showOsasuoritukset = (osasuoritukset && osasuoritukset.value) || isYhteinenTutkinnonOsa(model) || isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(model) || isValinnanMahdollisuus(model)
    return (<tbody className={buildClassNames(['tutkinnon-osa', (expanded && 'expanded'), (groupId)])}>
    <tr>
      {columns.map(column => column.renderData({model, showScope, showTila, onExpand, hasProperties: properties.length > 0 || showOsasuoritukset, expanded}))}
      {
        model.context.edit && (
          <td className="remove">
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="42" className="error">{error}</td></tr>)
    }
    {
      expanded && displayProperties.length > 0 && (<tr className="details" key="details">
        <td colSpan="4">
          <PropertiesEditor
            model={model}
            properties={displayProperties}
            getValueEditor={(p, getDefault) => p.key === 'liittyyTutkinnonOsaan' ? <LiittyyTutkinnonOsaanEditor model={p.model} /> : getDefault()}
          />

        </td>
      </tr>)
    }
    {
      expanded && showOsasuoritukset && (<tr className="osasuoritukset" key="osasuoritukset">
        <td colSpan="4">
          <Suoritustaulukko parentSuoritus={model} nestedLevel={nestedLevel} suorituksetModel={ osasuoritukset }/>
        </td>
      </tr>)
    }
    </tbody>)
  }
}

const SuoritusColumn = {
  shouldShow : () => true,
  renderHeader: ({parentSuoritus, groupTitles, groupId}) => (<td key="suoritus" className="tutkinnon-osan-ryhma">{isValinnanMahdollisuus(parentSuoritus) ? t('Osasuoritus') : groupTitles[groupId]}</td>),
  renderData: ({model, showTila, onExpand, hasProperties, expanded}) => {
    let koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')
    let titleAsExpandLink = hasProperties && (!osanOsa(koulutusmoduuli) || !model.context.edit)
    let kieliaine = isKieliaine(koulutusmoduuli)

    return (<td key="suoritus" className="suoritus">
      <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
         onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
      {showTila && <span className="tila" title={tilaText(model)}>{suorituksenTilaSymbol(model)}</span>}
      {
        titleAsExpandLink
          ? <button className='nimi inline-link-button' onClick={() => onExpand(!expanded)}>{modelTitle(model, 'koulutusmoduuli')}</button>
          : <span className="nimi">
            {t(modelData(koulutusmoduuli, 'tunniste.nimi')) + (kieliaine ? ', ' : '')}
            {kieliaine && <span className="value kieli"><Editor model={koulutusmoduuli} inline={true} path="kieli" sortBy={sortLanguages}/></span>}
          </span>
      }
    </td>)
  }
}

export const suorituksenTilaSymbol = (suoritus) => isValinnanMahdollisuus(suoritus)
  ? ''
  : suoritusValmis(suoritus) ? '' : ''
