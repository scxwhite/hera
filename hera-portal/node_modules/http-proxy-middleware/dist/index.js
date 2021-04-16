"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createProxyMiddleware = void 0;
const http_proxy_middleware_1 = require("./http-proxy-middleware");
function createProxyMiddleware(context, options) {
    const { middleware } = new http_proxy_middleware_1.HttpProxyMiddleware(context, options);
    return middleware;
}
exports.createProxyMiddleware = createProxyMiddleware;
