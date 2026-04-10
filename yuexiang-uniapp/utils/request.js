import config from '../config/app'

let authRedirecting = false
let refreshingPromise = null

function normalizeMessage(payload, fallback = '请求失败') {
  return payload?.msg || payload?.message || fallback
}

function showToast(message) {
  uni.showToast({
    title: message.length > 18 ? `${message.slice(0, 18)}...` : message,
    icon: 'none'
  })
}

function clearAuthStorage() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('accessTokenExpiresAt')
  uni.removeStorageSync('refreshTokenExpiresAt')
  uni.removeStorageSync('userInfo')
}

function handleUnauthorized(message) {
  clearAuthStorage()
  if (!authRedirecting) {
    authRedirecting = true
    showToast(message || '登录状态已失效')
    setTimeout(() => {
      const pages = getCurrentPages()
      const currentRoute = pages[pages.length - 1]?.route || ''
      if (currentRoute !== 'pages/login/index') {
        uni.navigateTo({ url: '/pages/login/index' })
      }
      authRedirecting = false
    }, 200)
  }
}

function shouldRefreshToken() {
  const token = uni.getStorageSync('token')
  const refreshToken = uni.getStorageSync('refreshToken')
  const accessTokenExpiresAt = Number(uni.getStorageSync('accessTokenExpiresAt') || 0)
  const refreshTokenExpiresAt = Number(uni.getStorageSync('refreshTokenExpiresAt') || 0)
  const now = Date.now()

  if (!token || !refreshToken) return false
  if (refreshTokenExpiresAt && now >= refreshTokenExpiresAt) return false
  if (!accessTokenExpiresAt) return false
  return accessTokenExpiresAt - now <= 60000
}

function persistRefreshPayload(payload) {
  const now = Date.now()
  const nextAccessToken = payload?.accessToken || ''
  const nextRefreshToken = payload?.refreshToken || ''
  const expiresIn = Number(payload?.expiresIn || 0)
  const currentRefreshExpiresAt = Number(uni.getStorageSync('refreshTokenExpiresAt') || 0)

  uni.setStorageSync('token', nextAccessToken)
  if (nextRefreshToken) {
    uni.setStorageSync('refreshToken', nextRefreshToken)
  }
  uni.setStorageSync('accessTokenExpiresAt', expiresIn ? now + expiresIn * 1000 : 0)
  if (currentRefreshExpiresAt) {
    uni.setStorageSync('refreshTokenExpiresAt', currentRefreshExpiresAt)
  }
}

function refreshAccessToken() {
  if (refreshingPromise) {
    return refreshingPromise
  }

  const refreshToken = uni.getStorageSync('refreshToken')
  if (!refreshToken) {
    return Promise.reject(new Error('refreshToken missing'))
  }

  refreshingPromise = new Promise((resolve, reject) => {
    uni.request({
      url: `${config.baseUrl}/api/auth/token/refresh`,
      method: 'POST',
      timeout: config.requestTimeout,
      header: {
        'Content-Type': 'application/json'
      },
      data: { refreshToken },
      success: (res) => {
        const payload = res.data || {}
        const httpOk = res.statusCode >= 200 && res.statusCode < 300
        const bizOk = payload.code === undefined || payload.code === config.successCode
        if (httpOk && bizOk && payload.data?.accessToken) {
          persistRefreshPayload(payload.data)
          resolve(payload.data)
          return
        }
        reject({
          ...payload,
          statusCode: res.statusCode,
          message: normalizeMessage(payload, '刷新登录态失败')
        })
      },
      fail: (error) => reject(error),
      complete: () => {
        refreshingPromise = null
      }
    })
  })

  return refreshingPromise
}

function performRequest(options) {
  const {
    url,
    method = 'GET',
    data,
    params,
    header,
    timeout = config.requestTimeout,
    fullResponse = false
  } = options

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${config.baseUrl}${url}`,
      method,
      data: data ?? params ?? {},
      timeout,
      header: {
        'Content-Type': 'application/json',
        Authorization: uni.getStorageSync('token')
          ? `Bearer ${uni.getStorageSync('token')}`
          : '',
        ...header
      },
      success: (res) => {
        const payload = res.data || {}
        const httpOk = res.statusCode >= 200 && res.statusCode < 300
        const bizOk = payload.code === undefined || payload.code === config.successCode

        if (httpOk && bizOk) {
          resolve(fullResponse ? payload : payload.data)
          return
        }

        reject({
          ...payload,
          statusCode: res.statusCode,
          message: normalizeMessage(payload, `请求失败(${res.statusCode})`)
        })
      },
      fail: (error) => reject(error)
    })
  })
}

async function doRequest(options, retry = false) {
  const {
    showError = true,
    skipAuthRedirect = false
  } = options

  try {
    if (!skipAuthRedirect && !retry && shouldRefreshToken()) {
      await refreshAccessToken()
    }

    return await performRequest(options)
  } catch (error) {
    const unauthorized = error?.statusCode === 401 || error?.code === 401

    if (unauthorized && !skipAuthRedirect) {
      try {
        if (!retry && uni.getStorageSync('refreshToken')) {
          await refreshAccessToken()
          return await doRequest(options, true)
        }
      } catch (refreshError) {
        handleUnauthorized(normalizeMessage(refreshError, error?.message || '登录状态已失效'))
        throw {
          ...refreshError,
          statusCode: refreshError?.statusCode || 401,
          message: normalizeMessage(refreshError, error?.message || '登录状态已失效')
        }
      }

      handleUnauthorized(error?.message)
      throw error
    }

    if (error?.errMsg || !error?.statusCode) {
      if (showError) {
        showToast('网络请求失败，请稍后重试')
      }
      throw error
    }

    if (showError) {
      showToast(error?.message || '请求失败')
    }
    throw error
  }
}

export const request = (options) => doRequest(options)

export default request
