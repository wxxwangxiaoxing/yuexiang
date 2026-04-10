const runtimeBaseUrl = (() => {
  // #ifdef H5
  if (typeof window !== 'undefined' && window.__APP_CONFIG__?.baseUrl) {
    return window.__APP_CONFIG__.baseUrl
  }
  // #endif
  return ''
})()

const config = {
  appName: '悦享生活',
  baseUrl: runtimeBaseUrl || 'http://localhost:8080',
  requestTimeout: 15000,
  successCode: 200
}

export function getAppConfig() {
  return config
}

export function getBaseUrl() {
  return config.baseUrl
}

export default config
