import request from '../utils/request';
import uploadFile from '../utils/upload';

export default {
  uploadImage(filePath) {
    return uploadFile({
      url: '/api/upload',
      filePath,
      name: 'file'
    });
  },

  publishBlog(data) {
    return request({
      url: '/api/blog',
      method: 'POST',
      data
    });
  },

  createDraft(data) {
    return request({
      url: '/api/blog/draft',
      method: 'POST',
      data
    });
  },

  updateDraft(draftId, data) {
    return request({
      url: `/api/blog/draft/${draftId}`,
      method: 'PUT',
      data
    });
  },

  getBlogList(params) {
    return request({
      url: '/api/blog/list',
      method: 'GET',
      params
    });
  },

  getBlogDetail(blogId) {
    return request({
      url: `/api/blog/${blogId}`,
      method: 'GET'
    });
  },

  getAiSummary(blogId) {
    return request({
      url: `/api/blog/${blogId}/ai-summary`,
      method: 'GET'
    });
  },

  getComments(blogId, params) {
    return request({
      url: `/api/blog/${blogId}/comments`,
      method: 'GET',
      params
    });
  },

  createComment(blogId, data) {
    return request({
      url: `/api/blog/${blogId}/comments`,
      method: 'POST',
      data
    });
  },

  toggleCommentLike(commentId) {
    return request({
      url: `/api/blog/comments/${commentId}/like`,
      method: 'PUT'
    });
  },

  getReplies(commentId, params) {
    return request({
      url: `/api/blog/comments/${commentId}/replies`,
      method: 'GET',
      params
    });
  },

  toggleFavorite(blogId) {
    return request({
      url: `/api/blog/${blogId}/favorite`,
      method: 'PUT'
    });
  },

  likeBlog(blogId) {
    return request({
      url: `/api/blog/like/${blogId}`,
      method: 'POST'
    });
  },

  unlikeBlog(blogId) {
    return request({
      url: `/api/blog/like/${blogId}`,
      method: 'DELETE'
    });
  },

  getLikeStatus(blogId) {
    return request({
      url: `/api/blog/like/status/${blogId}`,
      method: 'GET'
    });
  },

  followUser(userId) {
    return request({
      url: `/api/follow/${userId}`,
      method: 'POST'
    });
  },

  unfollowUser(userId) {
    return request({
      url: `/api/follow/${userId}`,
      method: 'DELETE'
    });
  },

  getFollowList(userId) {
    return request({
      url: `/api/follow/list/${userId}`,
      method: 'GET'
    });
  },

  getFansList(userId) {
    return request({
      url: `/api/follow/fans/${userId}`,
      method: 'GET'
    });
  }
};
