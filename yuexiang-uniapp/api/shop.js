import request from '../utils/request';

export default {
  getShopList(params) {
    return request({
      url: '/api/shop/list',
      method: 'GET',
      params
    });
  },

  getShopDetail(shopId) {
    return request({
      url: `/api/shop/${shopId}`,
      method: 'GET'
    });
  },

  getNearbyShops(params) {
    return request({
      url: '/api/shop/nearby',
      method: 'GET',
      params
    });
  },

  getShopTypes() {
    return request({
      url: '/api/shop-type/list',
      method: 'GET'
    });
  },

  searchShops(params) {
    return request({
      url: '/api/shop/search',
      method: 'GET',
      params
    });
  },

  getAreaList(params) {
    return request({
      url: '/api/shop/area/list',
      method: 'GET',
      params
    });
  },

  toggleFavorite(shopId) {
    return request({
      url: '/api/shop/favorite/toggle',
      method: 'POST',
      params: { shopId }
    });
  },

  isFavorite(shopId) {
    return request({
      url: '/api/shop/favorite/is-favorite',
      method: 'GET',
      params: { shopId }
    });
  },

  batchCheckFavorite(shopIds) {
    return request({
      url: '/api/shop/favorite/batch',
      method: 'POST',
      data: shopIds
    });
  },

  getShopReviews(shopId, params) {
    return request({
      url: '/api/shop/review/list',
      method: 'GET',
      params: { shopId, ...params }
    });
  },

  getReviewSummary(shopId) {
    return request({
      url: '/api/shop/review/summary',
      method: 'GET',
      params: { shopId }
    });
  },

  createReview(data) {
    return request({
      url: '/api/shop/review/create',
      method: 'POST',
      data
    });
  },

  getSearchSuggest(keyword) {
    return request({
      url: '/api/shop/search/suggest',
      method: 'GET',
      params: { keyword }
    });
  },

  getHotShops(limit) {
    return request({
      url: '/api/shop/search/hot',
      method: 'GET',
      params: { limit }
    });
  }
};
