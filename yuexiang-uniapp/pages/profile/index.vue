<template>
  <view class="profile-container">
    <!-- 动态流体渐变背景 -->
    <view class="dynamic-bg">
      <view class="blob blob-1"></view>
      <view class="blob blob-2"></view>
    </view>
    
    <!-- 顶部状态栏与导航 -->
    <view class="header-nav">
      <view class="status-bar"></view>
      <view class="header-top">
        <text class="title">我的</text>
        <view class="btn-box shadow-glass" @click="openSettings">
          <AppIcon class="setting-icon" name="cog-outline" color="#fff" />
        </view>
      </view>
    </view>
    
    <!-- 悬浮毛玻璃用户信息卡片 -->
    <view class="user-card glass-panel fade-up" @click="goLogin">
      <view class="user-info">
        <view class="avatar-wrap">
          <image class="avatar" :src="userInfo?.avatar || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&h=150&fit=crop'" mode="aspectFill"></image>
          <view class="edit-btn" v-if="isLoggedIn" @click.stop="goEditProfile">
            <AppIcon class="i" name="pencil-outline" color="#fff" />
          </view>
        </view>
        <view class="detail">
          <text class="name">{{ userInfo?.nickName || '未登录' }}</text>
          <view class="id-tag" @click.stop="copyUserId">
            <text class="txt">{{ isLoggedIn ? `ID: ${userInfo?.userId}` : '点击登录' }}</text>
            <AppIcon v-if="isLoggedIn" class="copy" name="content-copy" color="#A29BFE" />
          </view>
        </view>
      </view>
      
      <!-- 数据看板 -->
      <view class="stats-grid">
        <view class="stat-item">
          <text class="num">{{ userInfo?.followCount || 0 }}</text>
          <text class="label">关注</text>
        </view>
        <view class="divider"></view>
        <view class="stat-item">
          <text class="num">{{ userInfo?.fansCount || 0 }}</text>
          <text class="label">粉丝</text>
        </view>
        <view class="divider"></view>
        <view class="stat-item">
          <text class="num">{{ userInfo?.likeCount || 0 }}</text>
          <text class="label">获赞与收藏</text>
        </view>
      </view>
    </view>
    
    <!-- 主体区域：渐变升起的视觉组件群 -->
    <view class="body-wrap fade-up delay-1">
      <!-- 创意横向卡片区：升级达人 -->
      <view class="vip-banner" @click="goCreatorCenter">
        <view class="v-left">
          <AppIcon class="crown" name="crown-outline" color="#FFCA28" />
          <text class="v-txt">升级探店达人，获100万流量扶持</text>
        </view>
        <view class="v-btn">立即领</view>
      </view>
      
      <!-- 圆角宫格：常用核心功能 -->
      <view class="feature-grid">
        <view class="f-box" v-for="(item, index) in featureGrid" :key="index" @click="goPage(item.title)">
          <view class="ic-wrap" :style="{ background: item.bg }">
            <AppIcon class="icon" :name="item.icon.name" :color="item.icon.color" />
          </view>
          <text class="f-name">{{ item.title }}</text>
        </view>
      </view>

      <!-- 流水列表：更多服务 -->
      <view class="feature-list shadow-lg">
        <view class="list-item" v-for="(item, index) in featureList" :key="index" @click="goPage(item.title)">
          <view class="left">
            <view class="ic-wrap">
              <AppIcon class="icon" :name="item.icon.name" :color="item.icon.color" />
            </view>
            <text class="title">{{ item.title }}</text>
          </view>
          <AppIcon class="arrow" name="chevron-right" color="#ccc" />
        </view>
      </view>
    </view>
    
    <view class="bottom-holder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'

const userInfo = ref(null)
const isLoggedIn = computed(() => !!uni.getStorageSync('token'))

const featureGrid = ref([
  { title: '我的笔记', bg: 'linear-gradient(135deg, #FF9A9E, #FECFEF)', icon: { name: 'notebook-heart-outline', color: '#fff' } },
  { title: '我的收藏', bg: 'linear-gradient(135deg, #A18CD1, #FBC2EB)', icon: { name: 'star-face', color: '#fff' } },
  { title: '浏览足迹', bg: 'linear-gradient(135deg, #84FAB0, #8FD3F4)', icon: { name: 'shoe-print', color: '#fff' } },
  { title: '我的订单', bg: 'linear-gradient(135deg, #FFC371, #FF5F6D)', icon: { name: 'shopping-outline', color: '#fff' } }
])

const featureList = ref([
  { title: '我的优惠券', icon: { name: 'ticket-percent-outline', color: '#FF5A5F' } },
  { title: '我的钱包', icon: { name: 'wallet-bifold-outline', color: '#2ED573' } },
  { title: '签到日历', icon: { name: 'calendar-check-outline', color: '#00BCD4' } },
  { title: '互动消息', icon: { name: 'message-badge-outline', color: '#6C5CE7' } },
  { title: '创作者中心', icon: { name: 'lightbulb-on-outline', color: '#FFCA28' } }
])

const checkLogin = () => {
  if (!isLoggedIn.value) {
    uni.showModal({
      title: '提示',
      content: '请先登录后再使用',
      confirmText: '去登录',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/index' })
        }
      }
    })
    return false
  }
  return true
}

const goCreatorCenter = () => {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/blog/publish' })
}

const copyUserId = () => {
  if (!isLoggedIn.value || !userInfo.value?.userId) return
  uni.setClipboardData({
    data: String(userInfo.value.userId),
    success: () => uni.showToast({ title: '用户 ID 已复制', icon: 'none' })
  })
}

const showComingSoon = (title) => {
  uni.showToast({ title: `${title}功能完善中`, icon: 'none' })
}

const showWalletPreview = async () => {
  try {
    const wallet = await api.user.getWallet()
    const balance = wallet?.balance ? (wallet.balance / 100).toFixed(2) : '0.00'
    const frozen = wallet?.freezeAmount ? (wallet.freezeAmount / 100).toFixed(2) : '0.00'
    uni.showModal({
      title: '我的钱包',
      content: `可用余额：¥${balance}\n冻结金额：¥${frozen}`,
      showCancel: false
    })
  } catch (e) {
    uni.showToast({ title: '钱包信息加载失败', icon: 'none' })
  }
}

const showSignPreview = async () => {
  const now = new Date()
  try {
    const [records, rewards] = await Promise.all([
      api.user.getSignRecord(now.getFullYear(), now.getMonth() + 1),
      api.user.getSignRewards()
    ])
    const signedDays = Array.isArray(records) ? records.length : (records?.signedDays || 0)
    const rewardCount = Array.isArray(rewards) ? rewards.length : (rewards?.length || 0)
    uni.showModal({
      title: '签到日历',
      content: `本月已签到 ${signedDays} 天\n可领取奖励 ${rewardCount} 项`,
      showCancel: false
    })
  } catch (e) {
    uni.showToast({ title: '签到信息加载失败', icon: 'none' })
  }
}

const goPage = async (title) => {
  if (!checkLogin()) return
  const routeMap = {
    '我的笔记': '/pages/blog/index?mine=1',
    '我的收藏': '/pages/blog/index?favorite=1',
    '浏览足迹': '/pages/blog/index?history=1',
    '我的订单': '/pages/voucher/seckill?tab=orders',
    '我的优惠券': '/pages/voucher/seckill?tab=vouchers',
    '互动消息': '/pages/message/index',
    '创作者中心': '/pages/blog/publish'
  }
  if (title === '我的钱包') {
    await showWalletPreview()
    return
  }
  if (title === '签到日历') {
    await showSignPreview()
    return
  }
  const url = routeMap[title]
  if (url) {
    uni.navigateTo({ url })
  } else {
    showComingSoon(title)
  }
}

const goEditProfile = () => {
  if (!checkLogin()) return
  uni.navigateTo({ url: '/pages/profile/edit' })
}

const goLogin = () => {
  if (!isLoggedIn.value) {
    uni.navigateTo({ url: '/pages/login/index' })
  }
}

async function loadUserInfo() {
  if (!isLoggedIn.value) {
    userInfo.value = null
    return
  }
  try {
    const res = await api.user.getMe()
    userInfo.value = res
  } catch (e) {
    console.error('加载用户信息失败', e)
    userInfo.value = null
  }
}

const openSettings = () => {
  const itemList = isLoggedIn.value ? ['复制用户ID', '退出登录'] : ['前往登录']
  uni.showActionSheet({
    itemList,
    success: ({ tapIndex }) => {
      if (!isLoggedIn.value) {
        goLogin()
        return
      }
      if (tapIndex === 0) {
        copyUserId()
        return
      }
      if (tapIndex === 1) {
        uni.removeStorageSync('token')
        userInfo.value = null
        uni.showToast({ title: '已退出登录', icon: 'none' })
      }
    }
  })
}

onShow(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh; background-color: #F8F9FA; position: relative; overflow-x: hidden;
}

/* 动态流体渐变背景 */
.dynamic-bg {
  position: absolute; top: 0; left: 0; width: 100%; height: 640rpx; z-index: 0; overflow: hidden;
  background: linear-gradient(145deg, #FF6B6B, #8e44ad);
  .blob {
    position: absolute; border-radius: 50%; filter: blur(80rpx); opacity: 0.6; animation: floatAnim 10s infinite alternate ease-in-out;
  }
  .blob-1 { width: 500rpx; height: 500rpx; background: #FF9F43; top: -100rpx; left: -150rpx; animation-delay: 0s;}
  .blob-2 { width: 600rpx; height: 600rpx; background: #EE5A24; bottom: -200rpx; right: -200rpx; animation-delay: -5s;}
}

@keyframes floatAnim {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(60rpx, 80rpx) scale(1.1); }
}

.header-nav {
  position: relative; z-index: 10;
  .status-bar { height: var(--status-bar-height, 44px); }
  .header-top { 
    display: flex; justify-content: space-between; align-items: center; padding: 20rpx 40rpx;
    .title { font-size: 40rpx; font-weight: 900; color: #fff; letter-spacing: 2rpx;}
    .btn-box {
      width: 76rpx; height: 76rpx; border-radius: 50%; display: flex; justify-content: center; align-items: center;
      background: rgba(255,255,255,0.15); backdrop-filter: blur(20px); border: 2rpx solid rgba(255,255,255,0.3);
      box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.1);
      .setting-icon { width: 44rpx; height: 44rpx; }
      &:active { transform: scale(0.95);}
    }
  }
}

/* 入场微动画 */
.fade-up { animation: fadeUp 0.7s cubic-bezier(0.16, 1, 0.3, 1) both; }
.delay-1 { animation-delay: 0.15s; }
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(60rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* 用户名片：超越级毛玻璃质感 */
.glass-panel {
  position: relative; z-index: 10; margin: 40rpx 30rpx; border-radius: 48rpx; padding: 40rpx 36rpx;
  background: rgba(255, 255, 255, 0.75); backdrop-filter: blur(40px);
  box-shadow: 0 30rpx 80rpx rgba(0,0,0,0.15), inset 0 4rpx 0 rgba(255,255,255,0.5);
  border: 2rpx solid rgba(255,255,255,0.6);
  
  .user-info {
    display: flex; align-items: center; margin-bottom: 40rpx;
    
    .avatar-wrap {
      position: relative; margin-right: 32rpx;
      .avatar { width: 148rpx; height: 148rpx; border-radius: 50%; border: 6rpx solid #fff; box-shadow: 0 12rpx 30rpx rgba(0,0,0,0.15); }
      .edit-btn { position: absolute; right: 0; bottom: 0; width: 44rpx; height: 44rpx; background: #333; border-radius: 50%; display: flex; justify-content: center; align-items: center; border: 4rpx solid #fff; .i { width: 24rpx; height: 24rpx; }}
    }
    
    .detail {
      display: flex; flex-direction: column;
      .name { font-size: 46rpx; font-weight: 900; color: #1a1a1a; margin-bottom: 12rpx; letter-spacing: 1rpx;}
      .id-tag {
        display: inline-flex; align-items: center; background: rgba(108, 92, 231, 0.1); padding: 8rpx 20rpx; border-radius: 30rpx; align-self: flex-start;
        border: 2rpx solid rgba(108, 92, 231, 0.2);
        .txt { font-size: 24rpx; color: #6C5CE7; font-weight: bold; margin-right: 8rpx;}
        .copy { width: 26rpx; height: 26rpx;}
      }
    }
  }
  
  .stats-grid {
    display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.9); border-radius: 28rpx; padding: 30rpx 20rpx; box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.04);
    .stat-item {
      display: flex; flex-direction: column; align-items: center; flex: 1;
      .num { font-size: 40rpx; font-weight: 900; color: #333; margin-bottom: 6rpx; font-family: 'Helvetica Neue', Arial, sans-serif;}
      .label { font-size: 24rpx; color: #777; font-weight: 600;}
    }
    .divider { width: 4rpx; height: 48rpx; background: #F0F0F0; border-radius: 4rpx;}
  }
}

/* 主体操作区 */
.body-wrap {
  position: relative; z-index: 10; margin-top: 10rpx; padding: 0 30rpx;
  
  .vip-banner {
    display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #1e272e, #2f3640); padding: 24rpx 36rpx; border-radius: 28rpx; margin-bottom: 30rpx; box-shadow: 0 16rpx 40rpx rgba(30, 39, 46, 0.2);
    border: 2rpx solid #485460;
    .v-left { display: flex; align-items: center; .crown { width: 44rpx; height: 44rpx; margin-right: 16rpx;} .v-txt { color: #f1c40f; font-size: 26rpx; font-weight: 800; text-shadow: 0 2rpx 10rpx rgba(241,196,15,0.4);}}
    .v-btn { background: linear-gradient(90deg, #f1c40f, #f39c12); color: #1e272e; font-size: 24rpx; font-weight: 900; padding: 12rpx 30rpx; border-radius: 30rpx; box-shadow: 0 6rpx 20rpx rgba(243, 156, 18, 0.4);}
  }
  
  /* 宫格卡片：采用半糖色渐变增强多巴胺情绪 */
  .feature-grid {
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 20rpx; margin-bottom: 40rpx;
    .f-box {
      display: flex; flex-direction: column; align-items: center; background: #fff; padding: 36rpx 0; border-radius: 36rpx; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.04); transition: transform 0.2s;
      &:active { transform: scale(0.92); }
      .ic-wrap { width: 96rpx; height: 96rpx; border-radius: 32rpx; display: flex; justify-content: center; align-items: center; margin-bottom: 20rpx; box-shadow: 0 12rpx 24rpx rgba(0,0,0,0.1); .icon { width: 48rpx; height: 48rpx; }}
      .f-name { font-size: 24rpx; color: #333; font-weight: 800;}
    }
  }

  /* 列表卡片：极简化设计语言 */
  .feature-list {
    background-color: #fff; border-radius: 40rpx; padding: 20rpx 24rpx; box-shadow: 0 12rpx 50rpx rgba(0,0,0,0.04);
    .list-item {
      display: flex; justify-content: space-between; align-items: center; padding: 36rpx 20rpx; position: relative;
      &:not(:last-child)::after {
        content: ''; position: absolute; bottom: 0; left: 110rpx; right: 20rpx; height: 2rpx; background: #f0f0f0;
      }
      &:active { background-color: #f8f9fa; border-radius: 20rpx;}
      .left {
        display: flex; align-items: center;
        .ic-wrap { width: 72rpx; height: 72rpx; background: #F8F9FA; border-radius: 24rpx; display: flex; justify-content: center; align-items: center; margin-right: 32rpx; border: 2rpx solid #F0F0F0; .icon { width: 44rpx; height: 44rpx; }}
        .title { font-size: 32rpx; color: #1a1a1a; font-weight: 800; letter-spacing: 1rpx;}
      }
      .arrow { width: 44rpx; height: 44rpx; opacity: 0.4;}
    }
  }
}
.bottom-holder { height: 100rpx; }
</style>
