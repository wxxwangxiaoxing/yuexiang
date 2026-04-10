<template>
  <view class="detail-page">
    <view class="hero-card">
      <view class="hero-top">
        <view class="icon-wrap" :style="{ background: currentMeta.color }">
          <AppIcon class="hero-icon" :name="currentMeta.icon" color="#fff" />
        </view>
        <view class="hero-text">
          <text class="hero-title">{{ currentMeta.name }}</text>
          <text class="hero-sub">查看该分类下的全部消息动态与关联内容</text>
        </view>
      </view>
      <view class="hero-stats">
        <view class="stat-item">
          <text class="stat-num">{{ messageList.length }}</text>
          <text class="stat-label">消息总数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ unreadCount }}</text>
          <text class="stat-label">未读数量</text>
        </view>
      </view>
    </view>

    <LoadingSkeleton v-if="loading" variant="message" :count="4" />

    <view v-else class="message-list">
      <view class="message-item" v-for="item in messageList" :key="item.id">
        <view class="message-head">
          <view class="left">
            <text class="msg-title">{{ item.title || currentMeta.name }}</text>
            <view class="unread-dot" v-if="!item.isRead"></view>
          </view>
          <text class="msg-time">{{ formatTime(item.createTime) }}</text>
        </view>
        <text class="msg-content">{{ item.content || '暂无详情内容' }}</text>
        <view class="message-foot">
          <text class="biz-text">{{ getBizText(item) }}</text>
          <view class="action-btn" :class="{ disabled: !canJump(item) }" @click="openRelated(item)">
            {{ canJump(item) ? '查看相关内容' : '仅查看消息' }}
          </view>
        </view>
      </view>

      <view class="empty-state" v-if="messageList.length === 0">
        <AppIcon class="empty-icon" name="inbox-outline" color="#c7c7d1" />
        <text class="empty-title">暂无该分类消息</text>
        <text class="empty-sub">后续互动会第一时间展示在这里</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'

const type = ref(0)
const loading = ref(true)
const messageList = ref([])

const typeMetaMap = {
  1: {
    name: '系统通知',
    icon: 'bell-ring-outline',
    color: 'linear-gradient(135deg, #4FACFE, #00F2FE)'
  },
  2: {
    name: '点赞收藏',
    icon: 'heart-outline',
    color: 'linear-gradient(135deg, #FF6B6B, #FF4757)'
  },
  3: {
    name: '新增关注',
    icon: 'account-multiple-plus-outline',
    color: 'linear-gradient(135deg, #FFB75E, #ED8F03)'
  },
  4: {
    name: '评论和@',
    icon: 'comment-processing-outline',
    color: 'linear-gradient(135deg, #A18CD1, #FBC2EB)'
  }
}

const currentMeta = computed(() => typeMetaMap[type.value] || {
  name: '消息详情',
  icon: 'email-outline',
  color: 'linear-gradient(135deg, #BDBDBD, #E0E0E0)'
})

const unreadCount = computed(() => messageList.value.filter(item => !item.isRead).length)

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

function canJump(item) {
  return !!item.bizId && (Number(type.value) === 2 || Number(type.value) === 4)
}

function getBizText(item) {
  if (!item.bizId) return '暂无关联业务信息'
  if (Number(type.value) === 2) return `关联笔记 ID：${item.bizId}`
  if (Number(type.value) === 4) return `关联互动 ID：${item.bizId}`
  if (Number(type.value) === 3) return `关联用户 ID：${item.bizId}`
  return `业务编号：${item.bizId}`
}

function openRelated(item) {
  if (!canJump(item)) {
    uni.showToast({ title: item.content || '消息详情已展示', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/blog/detail?id=${item.bizId}` })
}

async function loadMessageList() {
  loading.value = true
  try {
    const res = await api.message.getMessages(type.value)
    messageList.value = (res || []).map(item => ({
      ...item,
      isRead: Number(item.isRead) === 1
    }))
  } catch (e) {
    messageList.value = []
    uni.showToast({ title: '消息详情加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((options) => {
  type.value = Number(options?.type || 0)
  loadMessageList()
})
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #f7f4ff 0%, #f8f9fb 100%);
}

.hero-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 36rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 18rpx 46rpx rgba(108, 92, 231, 0.08);
}

.hero-top {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.icon-wrap {
  width: 92rpx;
  height: 92rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.hero-icon {
  width: 46rpx;
  height: 46rpx;
}

.hero-text {
  flex: 1;
}

.hero-title {
  display: block;
  font-size: 34rpx;
  color: #1a1a1a;
  font-weight: 800;
  margin-bottom: 10rpx;
}

.hero-sub {
  font-size: 24rpx;
  color: #9093a1;
  line-height: 1.5;
}

.hero-stats {
  display: flex;
  gap: 18rpx;
}

.stat-item {
  flex: 1;
  background: #f7f8fc;
  border-radius: 26rpx;
  padding: 22rpx 0;
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 34rpx;
  color: #1f2430;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #9297a6;
}

.message-item {
  background: #fff;
  border-radius: 30rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 14rpx 40rpx rgba(0, 0, 0, 0.04);
}

.message-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
}

.left {
  display: flex;
  align-items: center;
}

.msg-title {
  font-size: 30rpx;
  color: #1a1a1a;
  font-weight: 800;
}

.unread-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #ff5a5f;
  margin-left: 12rpx;
}

.msg-time {
  font-size: 22rpx;
  color: #a0a3b2;
}

.msg-content {
  display: block;
  font-size: 28rpx;
  color: #4b5060;
  line-height: 1.7;
  margin-bottom: 22rpx;
}

.message-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.biz-text {
  flex: 1;
  font-size: 22rpx;
  color: #9a9eb0;
}

.action-btn {
  flex-shrink: 0;
  padding: 14rpx 22rpx;
  border-radius: 24rpx;
  background: #f0edff;
  color: #6c5ce7;
  font-size: 24rpx;
  font-weight: 700;

  &.disabled {
    background: #f4f5f8;
    color: #a5a8b5;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  width: 140rpx;
  height: 140rpx;
  margin-bottom: 22rpx;
}

.empty-title {
  font-size: 30rpx;
  color: #666;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.empty-sub {
  font-size: 24rpx;
  color: #aaa;
}
</style>
