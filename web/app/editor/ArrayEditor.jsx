import React from 'react'
import BaconComponent from '../BaconComponent'
import R from 'ramda'
import {childContext, contextualizeModel, modelItems} from './EditorModel.js'
import {Editor} from './Editor.jsx'
import {resetOptionalModel} from './OptionalEditor.jsx'

export const ArrayEditor = BaconComponent({
  render() {
    let reverse = this.props.reverse
    let model = this.getUsedModel()
    let items = modelItems(this.props.model)
    if (reverse && !model.context.edit) items = items.slice(0).reverse()
    let inline = ArrayEditor.canShowInline(this)
    let zeroValue = ArrayEditor.zeroValue(this)
    let className = inline
      ? 'array inline'
      : 'array'

    let itemModel = () => {
      return contextualizeModel(model.arrayPrototype, childContext(model.context, items.length))
    }

    let newItem = () => {
      let item = itemModel()
      return item.type === 'enum' ? R.dissoc('value', item) : item // remove default value from enums
    }

    let addItem = () => {
      let item = itemModel()
      model.context.changeBus.push([item.context, item])
    }

    return (
      <ul ref="ul" className={className}>
        {
          items.map((item, i) => {
            let removeItem = () => {
              let newItems = items
              newItems.splice(i, 1)
              item.context.changeBus.push([item.context, undefined])
              if (newItems.length === 0) {
                resetOptionalModel(this.props.model)
              }
            }
            return (<li key={item.arrayKey}>
              <Editor model = {R.merge(item, {zeroValue: zeroValue})} />
              {item.context.edit && <a className="remove-item" onClick={removeItem}></a>}
            </li>)
          })
        }
        {
          model.context.edit && model.arrayPrototype !== undefined
            ? zeroValue
              ? <li className="add-item"><Editor model = {newItem()} /></li>
              : <li className="add-item"><a onClick={addItem}>lisää uusi</a></li>
            :null
        }
      </ul>
    )
  },
  getUsedModel() {
    let { model } = this.props
    let optionalModel = () => contextualizeModel(model.optionalPrototype, model.context)
    return model.optional ? R.merge(model, optionalModel()) : model
  },
  componentWillMount() {
    this.props.model.context.changeBus && this.props.model.context.changeBus.filter(c => c[1] && !c[0].optionalSetup && c[0].path.startsWith(this.props.model.context.path + '.'))
      .takeUntil(this.unmountE)
      .onValue((c) => {
        if (this.props.model.optional && modelItems(this.props.model).length == 0) {
          let mdl = this.getUsedModel()
          mdl.context.changeBus.push([mdl.context, mdl])
          mdl.context.changeBus.push([R.merge(c[0], {optionalSetup: true}), c[1]])
        }
      })
  }
})

ArrayEditor.canShowInline = (component) => {
  let items = modelItems(component.props.model)
  // consider inlineability of first item here. make a stateless "fake component" because the actual React component isn't available to us here.
  let fakeComponent = {props: { model: items[0] }}
  return items[0] && items[0].context.edit ? false : Editor.canShowInline(fakeComponent)
}
ArrayEditor.zeroValue = (component) => {
  let mdl = component.getUsedModel()
  let childModel = mdl.arrayPrototype && contextualizeModel(mdl.arrayPrototype, childContext(mdl.context, modelItems(mdl).length))
  let childComponent = {props: {model : R.dissoc('value', childModel)}}
  return childModel && childModel.type !== 'prototype' ? Editor.zeroValue(childComponent) : null
}
ArrayEditor.handlesOptional = true