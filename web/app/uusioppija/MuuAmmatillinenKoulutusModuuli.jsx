import React from 'baret'
import Text from '../i18n/Text'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import { koodistoValues } from './koodisto'

export default ({ koulutusmoduuliAtom }) => {
  const koulutuksetP = koodistoValues('ammatilliseentehtavaanvalmistavakoulutus')
  return (
    <div>
      <KoodistoDropdown
        className='ammatilliseentehtavaanvalmistavakoulutus'
        title={ <Text name='Koulutuksen nimi' /> }
        options={ koulutuksetP }
        selected={ koulutusmoduuliAtom } />
    </div>
  )
}
