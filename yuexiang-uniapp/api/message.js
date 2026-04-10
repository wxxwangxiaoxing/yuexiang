import request from '../utils/request'

export default {
  getMessages(type) {
    return request({
      url: '/api/user/message/list',
      method: 'GET',
      params: type ? { type } : {}
    })
  },

  getUnreadCount() {
    return request({
      url: '/api/user/message/unread-count',
      method: 'GET'
    })
  },

  markAllRead() {
    return request({
      url: '/api/user/message/mark-all-read',
      method: 'POST'
    })
  }
}
