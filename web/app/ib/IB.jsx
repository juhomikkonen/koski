import React from 'react'
import R from 'ramda'
import {LukionOppiaineEditor} from '../lukio/Lukio'
import {LukionOppiaineetTableHead} from '../lukio/fragments/LukionOppiaineetTable'
import {modelData, modelLookup} from '../editor/EditorModel'

export class IBTutkinnonOppiaineetEditor extends React.Component {
  render() {
    const {oppiaineet} = this.props
    const aineryhmittäin = R.groupBy(
      oppiaine => modelData(oppiaine, 'koulutusmoduuli.ryhmä').koodiarvo,
      oppiaineet
    )
    return (
      <table className='suoritukset oppiaineet'>
        <LukionOppiaineetTableHead />
        <tbody>
        {
          Object.values(aineryhmittäin).map(aineet => [
            <tr className='aineryhmä'>
              <th colSpan='4'>{modelLookup(aineet[0], 'koulutusmoduuli.ryhmä').value.title}</th>
            </tr>,
            aineet.map((oppiaine, oppiaineIndex) =>
              <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} />
            )
          ])
        }
        </tbody>
      </table>
    )
  }
}
