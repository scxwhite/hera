'use strict';
var AssetsExtractor = require('./AssetsExtractor')
var replaceFileName = require('./replaceFileName')
var {ConcatSource} = require('webpack-sources');
var LineReg = /\n/g

module.exports = class Handler {
    constructor(options) {
        // Default options
        this.options = Object.assign({
            fileName: 'css/theme-colors-[contenthash:8].css',
            matchColors: [],
            isJsUgly: !(process.env.NODE_ENV === 'development' || process.argv.find(arg => arg.match(/\bdev/))),
        }, options);
        this.assetsExtractor = new AssetsExtractor(this.options)
    }

    handle(compilation) {
        var output = this.assetsExtractor.extractAssets(compilation.assets);
        console.log('Extracted theme color css content length: ' + output.length);

        //Add to assets for output
        var outputName = getFileName(this.options.fileName, output)
        compilation.assets[outputName] = {
            source: () => output,
            size: () => output.length
        };

        // 记录动态的文件名，到每个入口
        this.addToEntryJs(outputName, compilation, output)

        function getFileName(fileName, src) {
            return compilation.getPath(replaceFileName(fileName, src), {})
        }
    }

// 自动注入js代码，设置css文件名
    addToEntryJs(outputName, compilation, cssCode) {
        var onlyEntrypoints = {
            entrypoints: true,
            errorDetails: false,
            modules: false,
            assets: false,
            children: false,
            chunks: false,
            chunkGroups: false
        }
        var entrypoints = compilation.getStats().toJson(onlyEntrypoints).entrypoints;
        Object.keys(entrypoints).forEach(entryName => {
            var entryAssets = entrypoints[entryName].assets
            for (var i = 0, l = entryAssets.length; i < l; i++) {
                var assetName = entryAssets[i].name || entryAssets[i];
                if (assetName.slice(-3) === '.js' && assetName.indexOf('manifest.') === -1) { //
                    var assetSource = compilation.assets[assetName]
                    if (assetSource && !assetSource._isThemeJsInjected) {
                        var cSrc = this.getEntryJs(outputName, assetSource, cssCode)
                        cSrc._isThemeJsInjected = true
                        compilation.assets[assetName] = cSrc
                        break;
                    }
                }
            }
        })
    }

    getEntryJs(outputName, assetSource, cssCode) {
        var config = {url: outputName, colors: this.options.matchColors}
        var configJs = `\nthis.__theme_COLOR_cfg=${JSON.stringify(config)};\n`
        if (this.options.injectCss) {
            configJs = configJs + 'this.__theme_COLOR_css=' + JSON.stringify(cssCode.replace(LineReg, '')) + ';\n'
        }
        return new ConcatSource(assetSource, configJs)
    }
}


