var varyColor = require('../client/varyColor')
module.exports = function (colorStr) {
    if (colorStr[0] === '#') colorStr = colorStr.slice(1)
    var colors = ['#' + colorStr, varyColor.toNum3(colorStr).join(',')]
    for (var i = 0; i <= 9; i++) {
        colors.push(varyColor.lighten(colorStr, Number((i / 10).toFixed(2))));
    }
    colors.push(varyColor.lighten(colorStr, 0.95));
    colors.push(varyColor.darken(colorStr, 0.1));
    colors.push(varyColor.darken(colorStr, 0.2));
    colors.push(varyColor.rrggbbToHsl(colorStr));

    // colors.push(varyColor.rgbaToRgb(colorStr, 0.1)) // element-ui此处rgba实际是与白色混合，已重复
    // colors.push(varyColor.rgbaToRgb(colorStr, 0.2))
    return colors
}
