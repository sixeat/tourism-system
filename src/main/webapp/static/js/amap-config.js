/**
 * 高德地图（AMap）全局配置脚本
 * 
 * 本文件用于配置高德地图 Web JS API 的 key 和安全密钥（securityJsCode）。
 * 在正式部署或提交到版本控制时，应将真实的 key 和 securityJsCode 留空，
 * 避免泄露敏感凭证。实际 key 可在 amap-config.local.js 中覆盖，或通过构建工具注入。
 * 
 * 使用方式：在页面中先加载本脚本，再加载高德地图 JS API，
 * 高德会自动读取 window.TOURISM_AMAP_CONFIG 中的配置。
 */
window.TOURISM_AMAP_CONFIG = {
    /* 高德地图 Key，正式环境请填写；演示环境留空 */
    key: "",
    /* 高德安全密钥（securityJsCode），用于增强前端调用安全性 */
    securityJsCode: ""
};
