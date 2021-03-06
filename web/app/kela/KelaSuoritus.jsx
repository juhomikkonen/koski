import React from 'baret'
import * as R from 'ramda'
import Atom from 'bacon.atom'
import {DateView, KeyValueTable} from './KeyValueTable'
import {KelaOsasuorituksetTable} from './KelaOsasuorituksetTable'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'

export const TabulatedSuorituksetView = ({suoritukset, path}) => {
  const indexAtom = Atom(0)
  return (
    <div className='kela opiskeluoikeus suoritukset'>
      {indexAtom.map(selectedIndex => (
        <>
          <SuoritusTabs suoritukset={suoritukset}
                        selectedIndex={selectedIndex}
                        setCurrentIndex={(index) => indexAtom.set(index)}
          />
          <SuoritusView suoritus={suoritukset[selectedIndex]} path={path}/>
        </>
      ))}
    </div>
  )
}

const SuoritusTabs = ({suoritukset, selectedIndex, setCurrentIndex}) => {
  return (
    <div className='tabs'>
      <ul>
        {suoritukset.map((suoritus, index) => (
            <li onClick={() => setCurrentIndex(index)}
                className={'tab' + (index === selectedIndex ? ' selected' : '')}
                key={index}
            >
              <span>{suorituksenNimi(suoritus.koulutusmoduuli)}</span>
            </li>
          )
        )}
      </ul>
    </div>
  )
}

const SuoritusView = ({suoritus, path}) => {
  const properties = R.omit(['osasuoritukset', 'vahvistus', 'koulutusmoduuli'], suoritus)
  const osasuoritukset = suoritus.osasuoritukset
  return (
    <>
      <KeyValueTable object={properties} path={path}/>
      <SuorituksenVahvistus vahvistus={suoritus.vahvistus}/>
      {osasuoritukset && <KelaOsasuorituksetTable osasuoritukset={osasuoritukset} path={path}/>}
    </>
  )
}

const SuorituksenVahvistus = ({vahvistus}) => (
  <div className={'suoritus vahvistus' + (vahvistus ? ' valmis' : ' kesken')}>
    <span className='status'>{t(vahvistus ? 'Suoritus valmis' : 'Suoritus kesken').toUpperCase()}</span>
    {vahvistus && (
      <span>
        {' '}
        <Text name={'Vahvistus'}/>
        {': '}
        <DateView value={vahvistus.päivä}/>
      </span>
    )}
  </div>
)

const suorituksenNimi = koulutusmoduuli => {
  return koulutusmoduuli && koulutusmoduuli.tunniste && t(koulutusmoduuli.tunniste.nimi) || null
}
