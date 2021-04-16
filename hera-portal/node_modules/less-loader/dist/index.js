"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

var _path = _interopRequireDefault(require("path"));

var _less = _interopRequireDefault(require("less"));

var _loaderUtils = require("loader-utils");

var _schemaUtils = require("schema-utils");

var _options = _interopRequireDefault(require("./options.json"));

var _utils = require("./utils");

var _LessError = _interopRequireDefault(require("./LessError"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

async function lessLoader(source) {
  const options = (0, _loaderUtils.getOptions)(this);
  (0, _schemaUtils.validate)(_options.default, options, {
    name: "Less Loader",
    baseDataPath: "options"
  });
  const callback = this.async();
  const lessOptions = (0, _utils.getLessOptions)(this, options);
  const useSourceMap = typeof options.sourceMap === "boolean" ? options.sourceMap : this.sourceMap;

  if (useSourceMap) {
    lessOptions.sourceMap = {
      outputSourceFiles: true
    };
  }

  let data = source;

  if (typeof options.additionalData !== "undefined") {
    data = typeof options.additionalData === "function" ? `${await options.additionalData(data, this)}` : `${options.additionalData}\n${data}`;
  }

  let result;

  try {
    result = await (options.implementation || _less.default).render(data, lessOptions);
  } catch (error) {
    if (error.filename) {
      // `less` returns forward slashes on windows when `webpack` resolver return an absolute windows path in `WebpackFileManager`
      // Ref: https://github.com/webpack-contrib/less-loader/issues/357
      this.addDependency(_path.default.normalize(error.filename));
    }

    callback(new _LessError.default(error));
    return;
  }

  const {
    css,
    imports
  } = result;
  imports.forEach(item => {
    if ((0, _utils.isUnsupportedUrl)(item)) {
      return;
    } // `less` return forward slashes on windows when `webpack` resolver return an absolute windows path in `WebpackFileManager`
    // Ref: https://github.com/webpack-contrib/less-loader/issues/357


    this.addDependency(_path.default.normalize(item));
  });
  let map = typeof result.map === "string" ? JSON.parse(result.map) : result.map;

  if (map && useSourceMap) {
    map = (0, _utils.normalizeSourceMap)(map, this.rootContext);
  }

  callback(null, css, map);
}

var _default = lessLoader;
exports.default = _default;