# Resize Detector

This project is basically a modified version of [sdecima/javascript-detect-element-resize](#sdecimajavascript-detect-element-resize) including these changes:

* Try to utilize native `ResizeObserver` first.
* Adopt Mutation-based approach to track detaching/attaching in both DOM trees and render trees (see [que-etc/resize-observer-polyfill](//github.com/que-etc/resize-observer-polyfill)).
* Use ES Modules.
* Put most CSS content inside a separate `.css` file.
* Drop support for IE8 and below.
* Make the package available from npm.

## Installation

```bash
$ npm i --save resize-detector
```

## Usage

```js
import { addListener, removeListener } from 'resize-detector'

// adding listener
addListener(elem, callback)

// removing listener
removeListener(elem, callback)
```

*`this` inside `callback` function is the element whose size has been changed.*

### Heads up

As `resize-detector` is published in both ES Module & CommonJS format and when you use webpack to bundle your app, the ESM version will be imported. It is not transpiled by Babel or similar tools so you have to transpile it in your build process.

For webpack with babe-loader you need to add it to the `include` field of the options:

```js
// ...
{
  test: /\.js$/,
  loader: 'babel-loader',
  include: [
    // other stuff to be transpiled
    // ...
    path.resolve('node_modules/resize-detector')
  ]
}
// ...
```

If you are using other toolchain, just configure your bundler similarly so that `resize-detector` will be transpiled during build process.

---

## Limitations and caveats

- **Is polyfill?**

  No.

- **Native first**

  Yes.

- **Strategy**

  Scroll-based + Mutation-based.

- **Pros**

  * Small size.
  * Minimal limitations.

- **Side effects**

  * Targets with `position: static` will become `position: relative`.
  * Several hidden elements will be injected into the target elements.

## Comparison with other projects

### [sdecima/javascript-detect-element-resize](//github.com/sdecima/javascript-detect-element-resize)

- **Is polyfill?**

  No.

- **Native first**

  No.

- **Strategy**

  Scroll-based.

- **Pros**

  * Small size.
  * Higher performance comparing to hidden `<object>`s.
  * Compatible with down to IE7.

- **Side effects**

  * Targets with `position: static` will become `position: relative`.
  * Several hidden elements will be injected into the target elements.

- **Limitations**

  * Cannot track detach/attach or visibility change on IE10 and below.

### [que-etc/resize-observer-polyfill](//github.com/que-etc/resize-observer-polyfill)

- **Is polyfill**

  Yes.

- **Native first**

  Yes.

- **Fallback Strategy**

  Use `MutationObserver` to observe every mutation in a document. For IE9/10, use Mutation Events instead.

- **Pros**

  * Small size.
  * Minimal side effects on target elements.
  * Can track detaching/attaching in both DOM trees and render trees as soon as it's triggered by DOM mutation.

- **Limitations**

  * Need extra transition event handling to catch size change from user interaction pseudo classes like `:hover`.
  * Delayed transitions will receive only one notification with the latest dimensions of an element.

### [developit/simple-element-resize-detector](//github.com/developit/simple-element-resize-detector)

- **Is polyfill**

  No.

- **Native first**

  No.

- **Strategy**

  Listen to `resize` events via hidden `<iframe>`s.

- **Pros**

  Dead simple.

- **Side effects**

  * Targets with `position: static` will become `position: relative`.
  * Several hidden elements will be injected into the target elements.
  * Relatively low performance.

- **Limitations**

  * Inapplicable for void elements.
  * Cannot track detach/attach or visibility change.

### [pelotoncycle/resize-observer](//github.com/pelotoncycle/resize-observer)

- **Is polyfill?**

  Yes.

- **Native first**

  Yes.

- **Fallback Strategy**

  Long polling through `requestAnimationFrame` or `setTimeout`.

- **Pros**

  Dead simple.

- **Side effects**

  * Might be not so performant by checking rendered sizes in each animation frame.

### [wnr/element-resize-detector](//github.com/wnr/element-resize-detector)

- **Is polyfill?**

  No.

- **Native first**

  No.

- **Strategy**

  Either hidden `<object>`s or scroll-based.

- **Pros**

  Two approaches available (Really, why?) with scroll-based approach being much faster than hidden `<object>`s.

- **Side effects**

  * Targets with `position: static` will become `position: relative`.
  * Several hidden elements will be injected into the target elements.

- **Limitations**

  * Package size is relatively large.
  * Inapplicable for void elements.
  * Cannot track detach/attach or visibility change.
