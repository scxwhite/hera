module.exports = (api, options, rootOptions) => {
  api.extendPackage({
    devDependencies: {
      'style-resources-loader': '^1.4.1'
    },
    vue: {
      pluginOptions: {
        'style-resources-loader': {
          'preProcessor': options.preProcessor,
          'patterns': []
        }
      }
    }
  })
  api.exitLog(`One more step, add patterns for your resources's files in vue.config.js`, 'done')
}
