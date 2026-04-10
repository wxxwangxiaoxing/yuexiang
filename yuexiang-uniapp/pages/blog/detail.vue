<template>
  <view class="blog-detail">
    <view class="nav-bar" :style="{ paddingTop: 'var(--status-bar-height, 44px)' }">
      <view class="left" @click="goBack">
        <AppIcon class="icon" name="chevron-left" color="#333" />
      </view>
      <view class="right">
        <AppIcon class="icon" name="dots-horizontal" color="#333" />
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper class="swiper" indicator-active-color="#FF5A5F" indicator-dots>
      <swiper-item v-for="(img, idx) in detail.images" :key="idx">
        <image :src="img" class="slide-img" mode="aspectFill"></image>
      </swiper-item>
    </swiper>

    <view class="content-pnl">
      <!-- 作者信息 -->
      <view class="author-row">
        <view class="left">
          <image class="avatar" :src="detail.authorAvatar" mode="aspectFill"></image>
          <text class="name">{{ detail.author }}</text>
        </view>
        <view class="follow-btn" :class="{ followed: isFollowed }" @click="toggleFollow">
          {{ isFollowed ? '已关注' : '+ 关注' }}
        </view>
      </view>

      <text class="title">{{ detail.title }}</text>
      
      <!-- AI摘要 -->
      <view class="ai-summary">
        <view class="ai-hd">
          <AppIcon class="icon" name="robot-outline" color="#6C5CE7" />
          <text class="tit">AI 课代表</text>
        </view>
        <text class="txt">{{ detail.aiSummary }}</text>
      </view>

      <text class="content-text">{{ detail.content }}</text>
      
      <view class="tags-row">
        <text class="tag" v-for="tag in detail.tags" :key="tag"># {{ tag }}</text>
      </view>
      
      <view class="meta-info">
        <text class="time">{{ detail.time }} 发布</text>
        <view class="location" v-if="detail.shopId" @click="goShopDetail">
          <AppIcon class="loc-icon" name="store-outline" color="#FF5A5F" />
          <text class="loc-txt">{{ detail.location }}</text>
        </view>
        <view class="location" v-else-if="detail.location">
          <AppIcon class="loc-icon" name="map-marker" color="#999" />
          <text class="loc-txt">{{ detail.location }}</text>
        </view>
      </view>
    </view>
    
    <view class="divider"></view>
    
    <view class="comment-section">
      <text class="sec-title">共 {{ commentCount }} 条评论</text>
      <view class="comment-item" v-for="comment in comments" :key="comment.id">
        <image class="c-avatar" :src="comment.avatar || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop'" mode="aspectFill"></image>
        <view class="c-content">
          <text class="c-name">{{ comment.nickName || '用户' + comment.userId }}</text>
          <text class="c-txt">{{ comment.content }}</text>
          <view class="c-meta">
            <text class="c-time">{{ comment.createTime }}</text>
            <view class="c-like" @click="toggleCommentLike(comment.id)">
              <AppIcon class="c-l-icon" :name="comment.isLiked ? 'thumb-up' : 'thumb-up-outline'" :color="comment.isLiked ? '#FF5A5F' : '#999'" />
              <text class="c-l-num">{{ comment.likeCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="input-box" @click="showCommentInput">说点什么...</view>
      <view class="action-item" @click="toggleLike">
        <AppIcon class="icon" :name="isLiked ? 'heart' : 'heart-outline'" :color="isLiked ? '#FF5A5F' : '#333'" />
        <text class="num">{{ formatCount(likeCount) }}</text>
      </view>
      <view class="action-item" @click="toggleFavorite">
        <AppIcon class="icon" :name="isFavorited ? 'star' : 'star-outline'" :color="isFavorited ? '#FFCA28' : '#333'" />
        <text class="num">{{ formatCount(detail.favoriteCount) }}</text>
      </view>
      <view class="action-item">
        <AppIcon class="icon" name="comment-outline" color="#333" />
        <text class="num">{{ formatCount(commentCount) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'

const blogId = ref(null)
const detail = ref({
  title: '',
  author: '',
  authorAvatar: '',
  images: [],
  aiSummary: '',
  content: '',
  tags: [],
  time: '',
  location: '',
  shopId: null
})
const comments = ref([])
const commentCount = ref(0)
const isLiked = ref(false)
const isFavorited = ref(false)
const isFollowed = ref(false)
const likeCount = ref(0)

const normalizeId = (value) => {
  if (value === null || value === undefined) return ''
  const text = String(value).trim()
  if (!text || text === 'undefined' || text === 'null' || text === 'NaN') return ''
  return text
}

const goBack = () => uni.navigateBack()

const goShopDetail = () => {
  const shopId = normalizeId(detail.value.shopId)
  if (shopId) {
    uni.navigateTo({ url: `/pages/shop/detail?id=${shopId}` })
  }
}

const toggleLike = async () => {
  try {
    if (isLiked.value) {
      await api.blog.unlikeBlog(blogId.value)
      isLiked.value = false
      likeCount.value--
    } else {
      await api.blog.likeBlog(blogId.value)
      isLiked.value = true
      likeCount.value++
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const toggleFavorite = async () => {
  try {
    await api.blog.toggleFavorite(blogId.value)
    isFavorited.value = !isFavorited.value
    uni.showToast({ title: isFavorited.value ? '已收藏' : '已取消收藏', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const toggleFollow = async () => {
  try {
    const authorId = detail.value.authorId
    if (!authorId) return
    if (isFollowed.value) {
      await api.blog.unfollowUser(authorId)
      isFollowed.value = false
    } else {
      await api.blog.followUser(authorId)
      isFollowed.value = true
    }
    uni.showToast({ title: isFollowed.value ? '已关注' : '已取消关注', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const toggleCommentLike = async (commentId) => {
  try {
    await api.blog.toggleCommentLike(commentId)
    const comment = comments.value.find(c => c.id === commentId)
    if (comment) {
      comment.isLiked = !comment.isLiked
      comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1)
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const formatCount = (count) => {
  if (!count) return '0'
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return String(count)
}

const showCommentInput = () => {
  uni.showModal({
    title: '发表评论',
    editable: true,
    placeholderText: '说点什么...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          await api.blog.createComment(blogId.value, { content: res.content })
          uni.showToast({ title: '评论成功', icon: 'success' })
          const commentsRes = await api.blog.getComments(blogId.value, { page: 1, size: 20, sortBy: 'hot' })
          if (commentsRes) {
            comments.value = commentsRes.comments || []
            commentCount.value = commentsRes.totalCount || 0
          }
        } catch (e) {
          uni.showToast({ title: '评论失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(async () => {
  try {
    const pages = getCurrentPages()
    const current = pages[pages.length - 1]
    blogId.value = normalizeId(current?.options?.id)
    if (!blogId.value) return

    const [blogRes, commentsRes, summaryRes] = await Promise.all([
      api.blog.getBlogDetail(blogId.value),
      api.blog.getComments(blogId.value, { page: 1, size: 20, sortBy: 'hot' }),
      api.blog.getAiSummary(blogId.value).catch(() => null)
    ])

    if (blogRes) {
      detail.value = {
        title: blogRes.title || '',
        author: blogRes.user?.nickName || '用户' + blogRes.user?.id,
        authorId: blogRes.user?.id,
        authorAvatar: blogRes.user?.avatar || '',
        images: blogRes.images || [],
        aiSummary: summaryRes?.summary || '',
        content: blogRes.content || '',
        tags: blogRes.tags?.map(t => t.name) || [],
        time: blogRes.createTime ? blogRes.createTime.split(' ')[0] : '',
        location: blogRes.shop?.name || '',
        shopId: blogRes.shop?.id || null
      }
      isLiked.value = blogRes.isLiked || false
      isFavorited.value = blogRes.isFavorited || false
      isFollowed.value = blogRes.user?.isFollowed || false
      likeCount.value = blogRes.likeCount || 0
    }

    if (commentsRes) {
      comments.value = commentsRes.comments || []
      commentCount.value = commentsRes.totalCount || 0
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
})
</script>

<style lang="scss" scoped>
.blog-detail { background: #fff; padding-bottom: 120rpx; position: relative;}
.nav-bar {
  display: flex; justify-content: space-between; align-items: center; padding: 20rpx 30rpx;
  position: absolute; top: 0; left: 0; right: 0; z-index: 100;
  .left, .right { width: 64rpx; height: 64rpx; background: rgba(255,255,255,0.8); border-radius: 50%; display: flex; justify-content: center; align-items: center; .icon { width: 44rpx; height: 44rpx; }}
}
.swiper { height: 750rpx; .slide-img { width: 100%; height: 100%; }}
.content-pnl { padding: 30rpx; }
.author-row {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx;
  .left { display: flex; align-items: center; .avatar { width: 80rpx; height: 80rpx; border-radius: 50%; margin-right: 20rpx; } .name { font-size: 32rpx; font-weight: bold; color: #333; }}
  .follow-btn { border: 2rpx solid #FF5A5F; color: #FF5A5F; font-size: 26rpx; padding: 10rpx 36rpx; border-radius: 30rpx; font-weight: bold;}
}
.title { font-size: 40rpx; font-weight: bold; color: #333; line-height: 1.4; display: block; margin-bottom: 24rpx; }
.ai-summary {
  background: linear-gradient(to right, #F0EDFF, #F8F5FF); padding: 24rpx 30rpx; border-radius: 20rpx; margin-bottom: 24rpx; border: 2rpx solid rgba(108, 92, 231, 0.1);
  .ai-hd { display: flex; align-items: center; margin-bottom: 12rpx; .icon{ width: 36rpx; height: 36rpx; margin-right: 12rpx; } .tit { font-size: 28rpx; font-weight: bold; color: #6C5CE7; }}
  .txt { font-size: 26rpx; color: #555; line-height: 1.6; }
}
.content-text { font-size: 32rpx; color: #333; line-height: 1.8; display: block; margin-bottom: 30rpx; white-space: pre-wrap; font-weight: 400;}
.tags-row { display: flex; flex-wrap: wrap; margin-bottom: 30rpx; .tag { color: #2C64B5; font-size: 30rpx; margin-right: 24rpx; margin-bottom: 12rpx; font-weight: 500;}}
.meta-info { display: flex; justify-content: space-between; align-items: center; font-size: 24rpx; color: #999; .location { display: flex; align-items: center; .loc-icon { width: 32rpx; height: 32rpx; margin-right: 8rpx; }} .location:active { opacity: 0.6; }}
.divider { height: 16rpx; background: #F8F9FA; }
.comment-section {
  padding: 30rpx;
  .sec-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 30rpx; display: block; }
  .comment-item {
    display: flex; margin-bottom: 40rpx;
    .c-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; margin-right: 24rpx; }
    .c-content {
      flex: 1; display: flex; flex-direction: column;
      .c-name { font-size: 26rpx; font-weight: 500; color: #666; margin-bottom: 8rpx; }
      .c-txt { font-size: 28rpx; color: #333; line-height: 1.5; margin-bottom: 16rpx; }
      .c-meta { display: flex; justify-content: space-between; align-items: center; .c-time { font-size: 22rpx; color: #999;} .c-like { display: flex; align-items: center; .c-l-icon { width: 32rpx; height: 32rpx; margin-right: 6rpx; } .c-l-num { font-size: 24rpx; color: #999; }}}
    }
  }
}
.bottom-bar {
  position: fixed; bottom: 0; left: 0; width: 100%; height: 110rpx; background: #fff; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05); padding-bottom: env(safe-area-inset-bottom);
  display: flex; align-items: center; padding: 0 30rpx; z-index: 100; box-sizing: border-box;
  .input-box { flex: 1; background: #F5F6F8; height: 72rpx; border-radius: 36rpx; line-height: 72rpx; padding: 0 30rpx; font-size: 28rpx; color: #999; margin-right: 30rpx; }
  .action-item { display: flex; align-items: center; margin-left: 20rpx; padding: 0 10rpx; .icon { width: 44rpx; height: 44rpx; margin-right: 8rpx; } .num { font-size: 26rpx; font-weight: bold; color: #333; }}
}
</style>
