# monaco-editor-vue

> [Monaco Editor](https://github.com/Microsoft/monaco-editor) for Vue.

[![NPM version][npm-image]][npm-url]
[![Downloads][downloads-image]][npm-url]

[![monaco-editor-vue](https://nodei.co/npm/monaco-editor-vue.png)](https://npmjs.org/package/monaco-editor-vue)

[npm-url]: https://www.npmjs.com/package/monaco-editor-vue
[downloads-image]: http://img.shields.io/npm/dm/monaco-editor-vue.svg
[npm-image]: http://img.shields.io/npm/v/monaco-editor-vue.svg

## Installation

```bash
npm install monaco-editor-vue
```

## Using with Webpack

```js
<template>
  <div id="app">
    <MonacoEditor
      width="800"
      height="500"
      theme="vs-dark"
      language="javascript"
      :options="options"
      @change="onChange"
    ></MonacoEditor>
  </div>
</template>

<script>
import MonacoEditor from 'monaco-editor-vue';
export default {
  name: "App",
  components: {
    MonacoEditor
  },
  data() {
    return {
      options: {
        //Monaco Editor Options
      }
    }
  },
  methods: {
    onChange(value) {
      console.log(value);
    }
  }
};
</script>
```

Add the [Monaco Webpack plugin](https://github.com/Microsoft/monaco-editor-webpack-plugin) `monaco-editor-webpack-plugin` to your `webpack.config.js`:

```js
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
module.exports = {
  plugins: [
    new MonacoWebpackPlugin({
      // available options are documented at https://github.com/Microsoft/monaco-editor-webpack-plugin#options
      languages: ['javascript']
    })
  ]
};
```

## Properties

If you specify `value` property, the component behaves in controlled mode.
Otherwise, it behaves in uncontrolled mode.

- `width` width of editor. Defaults to `100%`.
- `height` height of editor. Defaults to `100%`.
- `value` value of the auto created model in the editor.
- `original` value of the auto created original model in the editor.
- `language` the initial language of the auto created model in the editor. Defaults to `javascript`.
- `theme` the theme of the editor. Defaults to `vs`.
- `options` refer to [Monaco interface IEditorConstructionOptions](https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.ieditorconstructionoptions.html).
- `change(newValue, event)` an event emitted when the content of the current model has changed.
- `editorBeforeMount(monaco)` an event emitted before the editor mounted (similar to `beforeMount` of Vue).
- `editorMounted(editor, monaco)` an event emitted when the editor has been mounted (similar to `mounted` of Vue).

## Events & Methods

Refer to [Monaco interface IEditor](https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.ieditor.html).

### Use multiple themes

[Monaco only supports one theme](https://github.com/Microsoft/monaco-editor/issues/338).

### How to use the diff editor

```js
<template>
  <div id="app">
    <MonacoEditor
      :diffEditor="true"
      original="..."
      //...
    ></MonacoEditor>
  </div>
</template>
```
