import _objectWithoutProperties from 'babel-runtime/helpers/objectWithoutProperties';
import _defineProperty from 'babel-runtime/helpers/defineProperty';
import _extends from 'babel-runtime/helpers/extends';
import classNames from 'classnames';
import { getComponentFromProp, getOptionProps, getListeners } from '../_util/props-util';
import Input from './Input';
import Icon from '../icon';
import inputProps from './inputProps';
import PropTypes from '../_util/vue-types';
import BaseMixin from '../_util/BaseMixin';

var ActionMap = {
  click: 'click',
  hover: 'mouseover'
};

export default {
  name: 'AInputPassword',
  mixins: [BaseMixin],
  inheritAttrs: false,
  model: {
    prop: 'value',
    event: 'change.value'
  },
  props: _extends({}, inputProps, {
    prefixCls: PropTypes.string.def('ant-input-password'),
    inputPrefixCls: PropTypes.string.def('ant-input'),
    action: PropTypes.string.def('click'),
    visibilityToggle: PropTypes.bool.def(true)
  }),
  data: function data() {
    return {
      visible: false
    };
  },

  methods: {
    focus: function focus() {
      this.$refs.input.focus();
    },
    blur: function blur() {
      this.$refs.input.blur();
    },
    onVisibleChange: function onVisibleChange() {
      if (this.disabled) {
        return;
      }
      this.setState({
        visible: !this.visible
      });
    },
    getIcon: function getIcon() {
      var _on;

      var h = this.$createElement;
      var _$props = this.$props,
          prefixCls = _$props.prefixCls,
          action = _$props.action;

      var iconTrigger = ActionMap[action] || '';
      var iconProps = {
        props: {
          type: this.visible ? 'eye' : 'eye-invisible'
        },
        on: (_on = {}, _defineProperty(_on, iconTrigger, this.onVisibleChange), _defineProperty(_on, 'mousedown', function mousedown(e) {
          // Prevent focused state lost
          // https://github.com/ant-design/ant-design/issues/15173
          e.preventDefault();
        }), _defineProperty(_on, 'mouseup', function mouseup(e) {
          // Prevent focused state lost
          // https://github.com/ant-design/ant-design/pull/23633/files
          e.preventDefault();
        }), _on),
        'class': prefixCls + '-icon',
        key: 'passwordIcon'
      };
      return h(Icon, iconProps);
    }
  },
  render: function render() {
    var h = arguments[0];

    var _getOptionProps = getOptionProps(this),
        prefixCls = _getOptionProps.prefixCls,
        inputPrefixCls = _getOptionProps.inputPrefixCls,
        size = _getOptionProps.size,
        suffix = _getOptionProps.suffix,
        visibilityToggle = _getOptionProps.visibilityToggle,
        restProps = _objectWithoutProperties(_getOptionProps, ['prefixCls', 'inputPrefixCls', 'size', 'suffix', 'visibilityToggle']);

    var suffixIcon = visibilityToggle && this.getIcon();
    var inputClassName = classNames(prefixCls, _defineProperty({}, prefixCls + '-' + size, !!size));
    var inputProps = {
      props: _extends({}, restProps, {
        prefixCls: inputPrefixCls,
        size: size,
        suffix: suffixIcon,
        prefix: getComponentFromProp(this, 'prefix'),
        addonAfter: getComponentFromProp(this, 'addonAfter'),
        addonBefore: getComponentFromProp(this, 'addonBefore')
      }),
      attrs: _extends({}, this.$attrs, {
        type: this.visible ? 'text' : 'password'
      }),
      'class': inputClassName,
      ref: 'input',
      on: getListeners(this)
    };
    return h(Input, inputProps);
  }
};