<template>
  <view class="message-container">
    <view class="dynamic-bg">
      <view class="blob blob-1"></view>
    </view>

    <view class="header-nav">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view>
          <text class="title">消息互动</text>
          <text class="sub-title">汇总系统通知、点赞、关注与评论消息</text>
        </view>
        <view class="clear-btn" @click="clearUnread">
          <AppIcon class="ic" name="broom" color="#FF5A5F" />
          <text class="t">全部已读</text>
        </view>
      </view>
    </view>

    <view class="summary-card fade-up">
      <view class="summary-item">
        <text class="summary-num">{{ displayChatList.length }}</text>
        <text class="summary-label">当前会话</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-item">
        <text class="summary-num">{{ totalUnread }}</text>
        <text class="summary-label">未读消息</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-item">
        <text class="summary-num">{{ latestLabel }}</text>
        <text class="summary-label">最近更新</text>
      </view>
    </view>

    <scroll-view class="msg-categories fade-up" scroll-x :show-scrollbar="false">
      <view
        class="cat-item"
        :class="{ active: activeType === cat.type }"
        v-for="cat in categories"
        :key="String(cat.type)"
        @click="filterByType(cat.type)"
      >
        <view class="icon-wrap" :style="{ background: cat.color }">
          <AppIcon class="icon" :name="cat.icon" color="#fff" />
          <view class="badge" v-if="cat.unread > 0">{{ cat.unread > 99 ? '99+' : cat.unread }}</view>
        </view>
        <text class="name">{{ cat.name }}</text>
      </view>
    </scroll-view>

    <view class="list-wrapper fade-up delay-1">
      <scroll-view class="chat-list" scroll-y>
        <LoadingSkeleton v-if="isLoading" variant="message" :count="4" />

        <view
          v-else
          class="chat-item"
          v-for="chat in displayChatList"
          :key="chat.id"
          hover-class="item-active"
          @click="previewChat(chat)"
        >
          <view class="avatar-wrap">
            <view class="avatar" :style="{ background: chat.color }">
              <AppIcon class="avatar-ic" :name="chat.avatar" color="#fff" />
            </view>
            <view class="online-dot" v-if="chat.unread > 0"></view>
          </view>

          <view class="content">
            <view class="top">
              <text class="name">{{ chat.name }}</text>
              <text class="time">{{ chat.time }}</text>
            </view>
            <view class="bot">
              <text class="msg" :class="{ 'unread-txt': chat.unread > 0 }">{{ chat.lastMessage }}</text>
              <view class="badge" v-if="chat.unread > 0">{{ chat.unread }}</view>
            </view>
            <text class="count-text">共 {{ chat.messageCount }} 条消息</text>
          </view>
        </view>

        <view class="empty-msg" v-if="!isLoading && displayChatList.length === 0">
          <AppIcon class="empty-icon" name="email-outline" color="#ccc" />
          <text class="empty-title">暂无消息</text>
          <text class="empty-sub">新的互动会在这里集中展示</text>
        </view>
        <view class="bottom-holder"></view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'

const allChatList = ref([])
const categories = ref([])
const activeType = ref(null)
const isLoading = ref(false)

const typeNames = { 1: '系统通知', 2: '点赞收藏', 3: '新增关注', 4: '评论和@' }
const typeIcons = {
  1: 'bell-ring-outline',
  2: 'heart-outline',
  3: 'account-multiple-plus-outline',
  4: 'comment-processing-outline'
}
const typeColors = {
  1: 'linear-gradient(135deg, #4FACFE, #00F2FE)',
  2: 'linear-gradient(135deg, #FF6B6B, #FF4757)',
  3: 'linear-gradient(135deg, #FFB75E, #ED8F03)',
  4: 'linear-gradient(135deg, #A18CD1, #FBC2EB)'
}
const baseCategories = [1, 2, 3, 4]

const displayChatList = computed(() => {
  if (activeType.value === null) return allChatList.value
  return allChatList.value.filter(item => item.type === activeType.value)
})

const totalUnread = computed(() => allChatList.value.reduce((sum, item) => sum + item.unread, 0))

const latestLabel = computed(() => {
  const latest = allChatList.value[0]
  return latest?.time || '暂无'
})

function formatTime(ts) {
  if (!ts) return ''
  const diff = Date.now() - ts
  const mins = Math.floor(diff / 60000)
  if (mins <= 0) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const d = new Date(ts)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function rebuildCategories() {
  const groupedMap = allChatList.value.reduce((acc, item) => {
    acc[item.type] = item
    return acc
  }, {})
  categories.value = [
    {
      type: null,
      name: '全部',
      icon: 'message-badge-outline',
      color: 'linear-gradient(135deg, #6C5CE7, #A29BFE)',
      unread: totalUnread.value
    },
    ...baseCategories.map((type) => ({
      type,
      name: typeNames[type],
      icon: typeIcons[type],
      color: typeColors[type],
      unread: groupedMap[type]?.unread || 0
    }))
  ]
}

async function loadMessages() {
  isLoading.value = true
  try {
    const res = await api.message.getMessages()
    const grouped = {}
    ;(res || []).forEach(msg => {
      const key = msg.type
      if (!grouped[key]) {
        grouped[key] = {
          id: key,
          type: msg.type,
          name: typeNames[msg.type] || '其他',
          avatar: typeIcons[msg.type] || '',
          color: typeColors[msg.type] || 'linear-gradient(135deg, #BDBDBD, #E0E0E0)',
          lastMessage: msg.content || msg.title || '暂无内容',
          time: formatTime(msg.createTime),
          unread: 0,
          createTime: 0,
          messageCount: 0
        }
      }
      grouped[key].messageCount += 1
      if (!msg.isRead) grouped[key].unread += 1
      if (msg.createTime > (grouped[key].createTime || 0)) {
        grouped[key].createTime = msg.createTime
        grouped[key].lastMessage = msg.content || msg.title || '暂无内容'
        grouped[key].time = formatTime(msg.createTime)
      }
    })
    allChatList.value = Object.values(grouped).sort((a, b) => (b.createTime || 0) - (a.createTime || 0))
    rebuildCategories()
  } catch (e) {
    console.error('加载消息失败', e)
    allChatList.value = []
    rebuildCategories()
  } finally {
    isLoading.value = false
  }
}

function filterByType(type) {
  activeType.value = type
}

function previewChat(chat) {
  uni.navigateTo({ url: `/pages/message/detail?type=${chat.type}` })
}

async function clearUnread() {
  if (totalUnread.value === 0) {
    uni.showToast({ title: '当前没有未读消息', icon: 'none' })
    return
  }
  try {
    await api.message.markAllRead()
    allChatList.value = allChatList.value.map(item => ({
      ...item,
      unread: 0
    }))
    rebuildCategories()
    uni.showToast({ title: '未读已清零', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style lang="scss" scoped>
.message-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f4f6f9;
  position: relative;
  overflow: hidden;
}

.dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 540rpx;
  z-index: 0;
  overflow: hidden;
  background: linear-gradient(180deg, #f0edff 0%, #f4f6f9 100%);

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(80rpx);
    opacity: 0.6;
    animation: floatAnim 10s infinite alternate ease-in-out;
  }

  .blob-1 {
    width: 600rpx;
    height: 600rpx;
    background: #ffd1e3;
    top: -200rpx;
    right: -200rpx;
  }
}

@keyframes floatAnim {
  0% {
    transform: scale(1);
  }

  100% {
    transform: scale(1.2);
  }
}

.header-nav {
  position: relative;
  z-index: 10;

  .status-bar {
    height: var(--status-bar-height, 44px);
  }

  .nav-bar {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 24rpx 40rpx 18rpx;

    .title {
      display: block;
      font-size: 40rpx;
      font-weight: 900;
      color: #1a1a1a;
      letter-spacing: 2rpx;
      margin-bottom: 8rpx;
    }

    .sub-title {
      font-size: 24rpx;
      color: #7d7d92;
    }

    .clear-btn {
      display: flex;
      align-items: center;
      background: rgba(255, 255, 255, 0.72);
      backdrop-filter: blur(10px);
      padding: 12rpx 24rpx;
      border-radius: 30rpx;
      border: 2rpx solid #fff;
      box-shadow: 0 4rpx 16rpx rgba(255, 90, 95, 0.1);

      &:active {
        transform: scale(0.96);
      }

      .ic {
        width: 32rpx;
        height: 32rpx;
        margin-right: 8rpx;
      }

      .t {
        font-size: 24rpx;
        color: #ff5a5f;
        font-weight: 800;
      }
    }
  }
}

.fade-up {
  animation: fadeUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.delay-1 {
  animation-delay: 0.15s;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.summary-card {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 30rpx 24rpx;
  padding: 28rpx 24rpx;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border-radius: 32rpx;
  box-shadow: 0 16rpx 40rpx rgba(108, 92, 231, 0.05);

  .summary-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .summary-num {
    font-size: 34rpx;
    color: #1a1a1a;
    font-weight: 800;
    margin-bottom: 8rpx;
  }

  .summary-label {
    font-size: 22rpx;
    color: #8c8c9a;
  }

  .summary-divider {
    width: 2rpx;
    height: 54rpx;
    background: #ececf5;
  }
}

.msg-categories {
  position: relative;
  z-index: 10;
  white-space: nowrap;
  padding: 0 30rpx 20rpx;

  .cat-item {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    margin-right: 24rpx;
    padding: 24rpx 18rpx 18rpx;
    min-width: 136rpx;
    background: rgba(255, 255, 255, 0.72);
    border-radius: 34rpx;
    border: 2rpx solid transparent;

    &.active {
      background: #fff;
      border-color: rgba(108, 92, 231, 0.16);
      box-shadow: 0 16rpx 36rpx rgba(108, 92, 231, 0.08);

      .name {
        color: #6c5ce7;
      }
    }

    .icon-wrap {
      width: 96rpx;
      height: 96rpx;
      border-radius: 32rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 16rpx;
      position: relative;
      box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.08);

      .icon {
        width: 52rpx;
        height: 52rpx;
      }

      .badge {
        position: absolute;
        top: -12rpx;
        right: -12rpx;
        background-color: #ff4757;
        color: #fff;
        font-size: 20rpx;
        font-weight: 900;
        padding: 6rpx 12rpx;
        border-radius: 20rpx;
        border: 4rpx solid #fff;
      }
    }

    .name {
      font-size: 24rpx;
      color: #333;
      font-weight: 700;
    }
  }
}

.list-wrapper {
  flex: 1;
  height: 0;
  background: #fff;
  border-top-left-radius: 56rpx;
  border-top-right-radius: 56rpx;
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 10;
  padding-top: 20rpx;

  .chat-list {
    height: 100%;
  }
}

.loading-state,
.empty-msg {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  color: #999;
  font-size: 28rpx;
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

.chat-item {
  display: flex;
  padding: 32rpx 40rpx;
  transition: background-color 0.2s, transform 0.2s;

  &.item-active {
    background-color: #f9fbfc;
    transform: scale(0.98);
    border-radius: 24rpx;
    margin: 0 10rpx;
  }

  .avatar-wrap {
    position: relative;
    margin-right: 32rpx;
    flex-shrink: 0;

    .avatar {
      width: 110rpx;
      height: 110rpx;
      border-radius: 50%;
      background-color: #eee;
      border: 2rpx solid #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .avatar-ic {
      width: 56rpx;
      height: 56rpx;
    }

    .online-dot {
      position: absolute;
      bottom: 4rpx;
      right: 4rpx;
      width: 24rpx;
      height: 24rpx;
      background: #2ed573;
      border-radius: 50%;
      border: 4rpx solid #fff;
    }
  }

  .content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    border-bottom: 2rpx solid #f5f6f8;
    padding-bottom: 32rpx;

    .top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .name {
        font-size: 34rpx;
        color: #1a1a1a;
        font-weight: 900;
        letter-spacing: 1rpx;
      }

      .time {
        font-size: 24rpx;
        color: #aaa;
        font-weight: 500;
      }
    }

    .bot {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .msg {
        font-size: 28rpx;
        color: #888;
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-right: 20rpx;
        font-weight: 500;

        &.unread-txt {
          color: #333;
          font-weight: 800;
        }
      }

      .badge {
        background: linear-gradient(135deg, #ff6b6b, #ff4757);
        color: #fff;
        font-size: 22rpx;
        min-width: 40rpx;
        height: 40rpx;
        line-height: 40rpx;
        text-align: center;
        border-radius: 20rpx;
        padding: 0 12rpx;
        font-weight: 900;
        flex-shrink: 0;
        box-shadow: 0 4rpx 10rpx rgba(255, 71, 87, 0.3);
      }
    }

    .count-text {
      font-size: 22rpx;
      color: #b0b0bc;
    }
  }
}

.bottom-holder {
  height: 100rpx;
}
</style>
