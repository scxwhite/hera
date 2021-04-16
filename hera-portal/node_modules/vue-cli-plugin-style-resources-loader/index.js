module.exports = (api, projectOptions) => {
  const pluginOptions = projectOptions.pluginOptions['style-resources-loader']
  api.chainWebpack(webpackConfig => {
    [
      'normal',
      'normal-modules',
      'vue',
      'vue-modules'
    ].forEach((oneOf) => {
      webpackConfig.module.rule(pluginOptions.preProcessor).oneOf(oneOf)
        .use('style-resources-loader')
        .loader('style-resources-loader')
        .options({
          patterns: pluginOptions.patterns
        })
    })
  })
}
