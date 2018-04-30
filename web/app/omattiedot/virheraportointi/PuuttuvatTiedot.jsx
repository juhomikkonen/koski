import React from 'react'

export const PuuttuvatTiedot = () => (
  <div className='puuttuvat-tiedot__info'>
    <h3>{'Huomioithan, että Koski-palvelussa ei pystytä näyttämään seuraavia tietoja:'}</h3>
    <ul>
      <li>
        {'Korkeakoulututkintoja '}<b>{'ennen vuotta 1995'}</b>{'. '}
        {'Tässä voi olla korkeakoulukohtaisia poikkeuksia.'}
      </li>
      <li>
        <b>{'Ennen vuotta 1990'}</b>
        {' suoritettuja ylioppilastutkintoja.'}
      </li>
      <li>
        <b>{'Ennen vuoden 2018 tammikuuta'}</b>
        {' suoritettuja peruskoulun, lukion tai ammattikoulun suorituksia ja opiskeluoikeuksia.'}
      </li>
    </ul>
  </div>
)