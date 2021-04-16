'use strict';
var Handler = require('./Handler')

class ThemeColorReplacer {
    constructor(options) {
        this.handler = new Handler(options)
    }

    getBinder(compiler, event) {
        return compiler.hooks
            ? compiler.hooks[event].tapAsync.bind(compiler.hooks[event], 'ThemeColorReplacer')
            : compiler.plugin.bind(compiler, event)
    }

    apply(compiler) {
        // this.getBinder(compiler, 'compilation')((compilation) => {
        //   this.getBinder(compilation, 'html-webpack-plugin-before-html-processing')((htmlPluginData, callback) => {
        //     debugger
        //   })
        // });
        this.getBinder(compiler, 'emit')((compilation, callback) => {
            this.handler.handle(compilation)
            callback()
        });
    }
}

ThemeColorReplacer.varyColor = require('../client/varyColor');

module.exports = ThemeColorReplacer;
