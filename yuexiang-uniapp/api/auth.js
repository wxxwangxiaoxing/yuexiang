import request from '../utils/request'

export default {
  getCaptcha() {
    return request({
      url: '/api/auth/captcha',
      method: 'GET'
    })
  },

  sendSmsCode(data) {
    return request({
      url: '/api/auth/sms-code',
      method: 'POST',
      data
    })
  },

  loginBySms(data) {
    return request({
      url: '/api/auth/login/sms',
      method: 'POST',
      data,
      skipAuthRedirect: true
    })
  },

  refreshToken(data) {
    return request({
      url: '/api/auth/token/refresh',
      method: 'POST',
      data,
      skipAuthRedirect: true
    })
  },

  logout(data) {
    return request({
      url: '/api/auth/logout',
      method: 'POST',
      data
    })
  }
}
