import React from 'react'
import {MultistateToggleButton} from '../../components/ToggleButton'
import {withFeatureFlag} from '../../components/withFeatureFlag'
import {FormState} from './Header'
import {virheRaportointiTitle} from './HeaderVirheraportointiSection'
import {hasOpintoja} from '../../oppija/oppija'

const VirheraportointiButton = withFeatureFlag(FEATURE.OMAT_TIEDOT.VIRHERAPORTOINTI, MultistateToggleButton)

export const HeaderButtons = ({uiModeA, oppija}) => (
  <div className='header__buttons'>
    {hasOpintoja(oppija) && <VirheraportointiButton
      stateA={uiModeA}
      value={FormState.VIRHERAPORTOINTI}
      clearedStateValue={FormState.NONE}
      text={virheRaportointiTitle(oppija)}
      style='secondary'
    />}
  </div>
)
