import request from '../utils/request';

export default {
  getVoucherDetail(voucherId) {
    return request({
      url: `/api/voucher/${voucherId}`,
      method: 'GET'
    });
  },

  getShopVouchers(shopId, params) {
    return request({
      url: `/api/voucher/shop/${shopId}`,
      method: 'GET',
      params
    });
  },

  getMyVouchers(params) {
    return request({
      url: '/api/voucher/my',
      method: 'GET',
      params
    });
  },

  getAvailableVouchers(params) {
    return request({
      url: '/api/voucher/available',
      method: 'GET',
      params
    });
  },

  getExpiringSoonVouchers(days) {
    return request({
      url: '/api/voucher/expiring-soon',
      method: 'GET',
      params: { days }
    });
  },

  getSeckillTime() {
    return request({
      url: '/api/seckill/time',
      method: 'GET'
    });
  },

  getSeckillSessions(date) {
    return request({
      url: '/api/seckill/sessions',
      method: 'GET',
      params: { date }
    });
  },

  getSeckillVouchers(sessionId, params) {
    return request({
      url: `/api/seckill/session/${sessionId}/vouchers`,
      method: 'GET',
      params
    });
  },

  getSeckillVoucherDetail(voucherId) {
    return request({
      url: `/api/seckill/voucher/${voucherId}`,
      method: 'GET'
    });
  },

  doSeckill(data) {
    return request({
      url: '/api/seckill/order',
      method: 'POST',
      data
    });
  },

  getSeckillOrderResult(orderId) {
    return request({
      url: `/api/seckill/order/${orderId}`,
      method: 'GET'
    });
  },

  getOrderDetail(orderId) {
    return request({
      url: `/api/voucher-order/${orderId}`,
      method: 'GET'
    });
  },

  payOrder(orderId, payType) {
    return request({
      url: `/api/voucher-order/${orderId}/pay`,
      method: 'POST',
      params: { payType }
    });
  },

  cancelOrder(orderId) {
    return request({
      url: `/api/voucher-order/${orderId}/cancel`,
      method: 'POST'
    });
  },

  refundOrder(orderId, reason) {
    return request({
      url: `/api/voucher-order/${orderId}/refund`,
      method: 'POST',
      params: { reason }
    });
  },

  getMyOrders(params) {
    return request({
      url: '/api/voucher-order/my',
      method: 'GET',
      params
    });
  },

  getPaymentRecords(params) {
    return request({
      url: '/api/voucher-order/payments',
      method: 'GET',
      params
    });
  },

  getRefundRecords(params) {
    return request({
      url: '/api/voucher-order/refunds',
      method: 'GET',
      params
    });
  }
};
