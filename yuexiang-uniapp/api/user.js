import request from '../utils/request';

export default {
  getMe() {
    return request({
      url: '/api/user/profile',
      method: 'GET'
    });
  },

  getPublicInfo(userId) {
    return request({
      url: `/api/user/${userId}`,
      method: 'GET'
    });
  },

  updateInfo(data) {
    return request({
      url: '/api/user/info',
      method: 'PUT',
      data
    });
  },

  updatePhone(data) {
    return request({
      url: '/api/user/phone',
      method: 'PUT',
      data
    });
  },

  setPassword(data) {
    return request({
      url: '/api/user/password',
      method: 'POST',
      data
    });
  },

  updatePassword(data) {
    return request({
      url: '/api/user/password',
      method: 'PUT',
      data
    });
  },

  resetPassword(data) {
    return request({
      url: '/api/user/password/reset',
      method: 'POST',
      data
    });
  },

  setPayPassword(data) {
    return request({
      url: '/api/user/pay-password',
      method: 'POST',
      data
    });
  },

  updatePayPassword(data) {
    return request({
      url: '/api/user/pay-password',
      method: 'PUT',
      data
    });
  },

  resetPayPassword(data) {
    return request({
      url: '/api/user/pay-password/reset',
      method: 'POST',
      data
    });
  },

  submitRealName(data) {
    return request({
      url: '/api/user/real-name',
      method: 'POST',
      data
    });
  },

  getRealName() {
    return request({
      url: '/api/user/real-name',
      method: 'GET'
    });
  },

  applyCancel(data) {
    return request({
      url: '/api/user/cancel',
      method: 'POST',
      data
    });
  },

  revokeCancel(data) {
    return request({
      url: '/api/user/cancel',
      method: 'DELETE',
      data
    });
  },

  getSignRecord(year, month) {
    return request({
      url: '/api/sign/record',
      method: 'GET',
      params: { year, month }
    });
  },

  doSign() {
    return request({
      url: '/api/sign/do',
      method: 'POST'
    });
  },

  getSignRewards() {
    return request({
      url: '/api/sign/rewards',
      method: 'GET'
    });
  },

  getSignRank() {
    return request({
      url: '/api/sign/rank',
      method: 'GET'
    });
  },

  repairSign(data) {
    return request({
      url: '/api/sign/repair',
      method: 'POST',
      data
    });
  },

  getWallet() {
    return request({
      url: '/api/user/wallet',
      method: 'GET'
    });
  }
};
