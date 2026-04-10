<template>
  <view class="home-container">
    <!-- 动态渐变导航顶栏 -->
    <view class="header-bg">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <view class="location" @click="goMap">
          <AppIcon class="loc-icon" name="map-marker" color="#fff" />
          <text class="loc-text">{{ currentArea || '选择位置' }}</text>
          <AppIcon class="arrow-icon" name="chevron-down" color="#fff" />
        </view>
        <view class="msg-icon-wrap" @click="goMessage">
          <AppIcon class="msg-icon" name="bell-outline" color="#fff" />
          <view class="badge" v-if="unreadCount > 0"></view>
        </view>
      </view>
      <view class="search-bar" @click="goSearch">
        <view class="search-inner">
          <AppIcon class="search-icon" name="magnify" color="#FF5A5F" />
          <text class="search-placeholder">搜索商户、美食、优惠券</text>
        </view>
        <view class="search-btn">搜索</view>
      </view>
    </view>
    
    <scroll-view class="main-scroll" scroll-y :show-scrollbar="false">
      <view class="scroll-pad"></view>
      <LoadingSkeleton v-if="loading" variant="home" :count="4" />

      <template v-else>
      <!-- 创意金刚区模块 -->
      <view class="category-grid fade-up" v-if="categories.length > 0">
        <view class="category-item" v-for="(cat, index) in categories" :key="index" @click="goCategory(cat)">
          <view class="icon-wrap" :style="{ background: cat.bg }">
            <AppIcon class="cat-icon" :name="cat.icon.name" :color="cat.icon.color" />
          </view>
          <text class="cat-name">{{ cat.name }}</text>
        </view>
      </view>
      
      <!-- 沉浸式限时秒杀卡片 -->
      <view class="seckill-panel shadow-glass fade-up delay-1" v-if="seckills.length > 0">
        <view class="panel-header">
          <view class="title-row">
            <view class="fire-box">
              <AppIcon class="s-icon" name="fire" color="#fff" />
            </view>
            <text class="title">限时秒杀</text>
            <view class="countdown">
              <text class="time-box">{{ countdown.hh }}</text><text class="colon">:</text>
              <text class="time-box">{{ countdown.mm }}</text><text class="colon">:</text>
              <text class="time-box">{{ countdown.ss }}</text>
            </view>
          </view>
          <view class="more-btn" @click="goSeckill">
            <text class="txt">更多</text>
            <AppIcon class="arrow" name="chevron-right" color="#FF5A5F" />
          </view>
        </view>
        
        <scroll-view class="seckill-scroll" scroll-x :show-scrollbar="false">
          <view class="seckill-list">
            <view class="s-item" v-for="item in seckills" :key="item.id" @click="goSeckill">
              <view class="img-box">
                <image class="cover" :src="item.image" mode="aspectFill"></image>
                <view class="discount-tag" v-if="item.oldPrice > item.price">-{{ Math.floor((1 - item.price/item.oldPrice)*100) }}%</view>
              </view>
              <text class="name">{{ item.name }}</text>
              <view class="price-row">
                <text class="sec-price"><text class="rmb">¥</text>{{ item.price }}</text>
                <text class="old-price" v-if="item.oldPrice > item.price">¥{{ item.oldPrice }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 极光色 AI 智能推荐 -->
      <view class="ai-recommend-box fade-up delay-2" v-if="aiShops.length > 0">
        <view class="ai-glow-bg"></view>
        <view class="ai-content glass-card">
          <view class="header">
            <view class="ai-badge">
              <AppIcon class="robot" name="robot-excited-outline" color="#fff" />
              <text>懂你的 AI 助手</text>
            </view>
            <view class="refresh" @click="refreshRecommend">
              <AppIcon class="ref-icon" name="refresh" color="#6C5CE7" />
              <text>换一批</text>
            </view>
          </view>
          <text class="ai-msg">"根据您的偏好，为您找到以下高分好店："</text>
          
          <view class="ai-shop-list">
            <view class="ai-shop-card" v-for="shop in aiShops" :key="shop.id" @click="goShopDetail(shop.id)">
              <view class="left">
                <text class="s-name">{{ shop.name }}</text>
                <view class="s-meta">
                  <AppIcon class="star" name="star" color="#FFCA28" />
                  <text class="score">{{ shop.score }}分</text>
                  <text class="div">|</text>
                  <text class="dist">{{ shop.distance }}</text>
                </view>
                <view class="ai-reason">
                  <AppIcon class="sparkle" name="auto-fix" color="#FF5A5F" />
                  <text class="r-txt">{{ shop.reason }}</text>
                </view>
              </view>
              <image class="s-cover" :src="shop.image" mode="aspectFill"></image>
            </view>
          </view>

          <view class="ai-action-bar">
            <view class="ask-btn" @click="openAiPlanner()">让 AI 定制方案</view>
            <view class="scene-list">
              <text class="scene-chip" @click="openAiPlanner('附近适合朋友聚餐的店，预算人均100以内')">聚餐</text>
              <text class="scene-chip" @click="openAiPlanner('帮我找适合约会、环境安静的餐厅')">约会</text>
              <text class="scene-chip" @click="openAiPlanner('今天有什么值得冲的秒杀和高性价比店铺')">捡漏</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 瀑布流附近的店 -->
      <view class="nearby-section fade-up delay-3" v-if="nearbyShops.length > 0">
        <text class="section-title">发现附近好店</text>
        <view class="shop-flow">
          <view class="shop-card" v-for="shop in nearbyShops" :key="shop.id" @click="goShopDetail(shop.id)">
            <view class="img-wrapper">
              <image class="cover" :src="shop.image" mode="aspectFill"></image>
              <view class="status-badge" :class="shop.status">
                {{ shop.status === 'hot' ? '🔥 热门' : '✨ 新店' }}
              </view>
            </view>
            <view class="info">
              <text class="name">{{ shop.name }}</text>
              <view class="meta">
                <text class="score">⭐ {{ shop.score }}</text>
                <text class="price">¥{{ shop.price }}/人</text>
              </view>
              <view class="tags" v-if="shop.tags && shop.tags.length > 0">
                <text class="tag" v-for="tag in shop.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="!loading && nearbyShops.length === 0 && seckills.length === 0 && aiShops.length === 0">
        <AppIcon class="empty-img" name="store-search-outline" color="#ccc" />
        <text class="empty-txt">暂无商户数据</text>
        <text class="empty-sub">联系管理员添加商户信息</text>
      </view>
      
      <view class="bottom-holder"></view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'
import { useChatStore } from '../../store/chat'

const categories = ref([])
const seckills = ref([])
const aiShops = ref([])
const nearbyShops = ref([])
const currentArea = ref('定位中')
const unreadCount = ref(0)
const loading = ref(true)
const recommendLoading = ref(false)
const currentLocation = ref(null)
const chatStore = useChatStore()

const countdown = ref({ hh: '00', mm: '00', ss: '00' })
let countdownTimer = null

const categoryIcons = {
  '美食': { name: 'food-drumstick-outline', color: '#FF6B6B' },
  'KTV': { name: 'microphone-variant', color: '#6C5CE7' },
  '休闲娱乐': { name: 'gamepad-variant-outline', color: '#2ED573' },
  '丽人': { name: 'face-woman-shimmer-outline', color: '#FF5285' },
  '酒店': { name: 'bed-outline', color: '#FFC312' },
  '购物': { name: 'cart-outline', color: '#00BCD4' },
  '运动健身': { name: 'dumbbell', color: '#FF6348' },
  '亲子': { name: 'baby-carriage', color: '#A29BFE' },
}
const categoryBgs = [
  'linear-gradient(135deg, #FFF0F0, #FFE4E4)',
  'linear-gradient(135deg, #F0EDFF, #E0D4FF)',
  'linear-gradient(135deg, #E8F8F5, #D1F2EB)',
  'linear-gradient(135deg, #FFE4EF, #FFD1E3)',
  'linear-gradient(135deg, #FFF8E1, #FFF0B2)',
  'linear-gradient(135deg, #E0F7FA, #B2EBF2)',
  'linear-gradient(135deg, #FFF0F0, #FFCDD2)',
  'linear-gradient(135deg, #F3E5F5, #E1BEE7)',
]

const goMap = () => { uni.showToast({ title: currentLocation.value ? currentArea.value : '定位未开启', icon: 'none' }) }
const goMessage = () => { uni.navigateTo({ url: '/pages/message/index' }) }
const goSearch = () => { uni.navigateTo({ url: '/pages/search/index' }) }
const openAiPlanner = (prompt = '帮我推荐 3 家最值得去的店') => {
  chatStore.setDraftPrompt(prompt)
  uni.switchTab({ url: '/pages/ai/chat' })
}
const goCategory = (item) => {
  if (item.typeId) {
    uni.navigateTo({ url: `/pages/search/index?typeId=${item.typeId}&typeName=${encodeURIComponent(item.name)}` })
  } else {
    uni.navigateTo({ url: `/pages/search/index?keyword=${item.name}` })
  }
}
const refreshRecommend = () => { loadAiShops(true) }
const goShopDetail = (id) => { uni.navigateTo({ url: `/pages/shop/detail?id=${id}` }) }
const goSeckill = () => { uni.navigateTo({ url: '/pages/voucher/seckill' }) }

function getPrimaryImage(item) {
  if (Array.isArray(item?.images) && item.images.length > 0) return item.images[0]
  return item?.image || ''
}

function normalizeDistance(item) {
  if (item?.distanceText) return item.distanceText
  if (item?.distance) return `${item.distance}m`
  return '附近优选'
}

function formatCoordinate(value) {
  return Number(value).toFixed(2)
}

function updateCurrentArea() {
  if (currentLocation.value?.city) {
    currentArea.value = currentLocation.value.city
    return
  }
  if (currentLocation.value?.longitude != null && currentLocation.value?.latitude != null) {
    currentArea.value = `已定位 ${formatCoordinate(currentLocation.value.longitude)}, ${formatCoordinate(currentLocation.value.latitude)}`
    return
  }
  currentArea.value = '定位未开启'
}

async function resolveLocation() {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        currentLocation.value = res
        chatStore.setLocation(res)
        updateCurrentArea()
        resolve(res)
      },
      fail: () => {
        currentLocation.value = null
        updateCurrentArea()
        resolve(null)
      }
    })
  })
}

async function loadShopTypes() {
  try {
    const res = await api.shop.getShopTypes()
    const types = res || []
    categories.value = types.map((t, i) => ({
      name: t.name,
      typeId: t.id,
      icon: categoryIcons[t.name] || { name: 'store-outline', color: '#666' },
      bg: categoryBgs[i % categoryBgs.length]
    }))
    updateCurrentArea()
  } catch (e) {
    console.error('加载商户类型失败', e)
  }
}

async function loadSeckills() {
  try {
    const sessionsRes = await api.voucher.getSeckillSessions()
    if (sessionsRes && sessionsRes.sessions && sessionsRes.sessions.length > 0) {
      const session = sessionsRes.sessions.find(s => s.status === 1) || sessionsRes.sessions[0]
      if (session) {
        const vouchersRes = await api.voucher.getSeckillVouchers(session.id, { page: 1, pageSize: 5 })
        seckills.value = (vouchersRes?.vouchers || []).map(v => ({
          id: v.id,
          name: v.voucherTitle || '',
          price: v.payValue ? (v.payValue / 100).toFixed(0) : '0',
          oldPrice: v.actualValue ? (v.actualValue / 100).toFixed(0) : '0',
          image: v.images?.[0] || ''
        }))
        if (session.endTime) {
          startCountdown(session.endTime)
        }
      }
    }
  } catch (e) {
    console.error('加载秒杀数据失败', e)
  }
}

async function loadNearbyShops() {
  try {
    const params = currentLocation.value
      ? {
          longitude: currentLocation.value.longitude,
          latitude: currentLocation.value.latitude,
          pageSize: 8
        }
      : { pageNo: 1, pageSize: 8, sortBy: 'score' }
    const res = currentLocation.value
      ? await api.shop.getNearbyShops(params)
      : await api.shop.getShopList(params)
    const list = res?.list || res?.records || []
    nearbyShops.value = list.map(item => ({
      id: item.id,
      name: item.name,
      score: item.score || '0',
      price: item.avgPrice ? (item.avgPrice / 100).toFixed(0) : '0',
      tags: item.tags || [],
      status: item.salesCount > 100 ? 'hot' : 'new',
      image: getPrimaryImage(item)
    }))
  } catch (e) {
    console.error('加载附近商户失败', e)
  }
}

async function loadAiShops(forceRefresh = false) {
  recommendLoading.value = true
  try {
    const excludeIds = forceRefresh ? aiShops.value.map(item => item.id).join(',') : undefined
    const res = currentLocation.value
      ? await api.shop.getNearbyShops({
          longitude: currentLocation.value.longitude,
          latitude: currentLocation.value.latitude,
          pageSize: 2,
          excludeIds
        })
      : await api.shop.getShopList({ pageNo: 1, pageSize: 2, sortBy: 'ai' })
    const list = res?.list || res?.records || []
    aiShops.value = list.map(item => ({
      id: item.id,
      name: item.name,
      score: item.score || '0',
      distance: normalizeDistance(item),
      reason: item.aiSummary || '根据您的偏好推荐',
      image: getPrimaryImage(item)
    }))
  } catch (e) {
    console.error('加载AI推荐失败', e)
  } finally {
    recommendLoading.value = false
  }
}

async function loadUnreadCount() {
  if (!uni.getStorageSync('token')) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await api.message.getUnreadCount()
    unreadCount.value = Number(res?.total ?? res) || 0
  } catch (e) {
    unreadCount.value = 0
  }
}

function startCountdown(endTime) {
  const update = () => {
    let diff = Number(endTime) - Date.now()
    if (diff < 0) diff = 0
    countdown.value = {
      hh: String(Math.floor(diff / 3600000)).padStart(2, '0'),
      mm: String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0'),
      ss: String(Math.floor((diff % 60000) / 1000)).padStart(2, '0')
    }
  }
  update()
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(update, 1000)
}

async function refreshHomeData() {
  loading.value = true
  await resolveLocation()
  await Promise.all([
    loadShopTypes(),
    loadSeckills(),
    loadNearbyShops(),
    loadAiShops(),
    loadUnreadCount()
  ])
  loading.value = false
}

onMounted(async () => {
  await refreshHomeData()
})

onShow(() => {
  loadUnreadCount()
})

onPullDownRefresh(async () => {
  await refreshHomeData()
  uni.stopPullDownRefresh()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

</script>

<style lang="scss" scoped>
.home-container { height: 100vh; display: flex; flex-direction: column; background-color: #F4F6F9; }

.header-bg {
  background: linear-gradient(135deg, #FF6B6B, #FF4757); padding-bottom: 44rpx;
  border-bottom-left-radius: 48rpx; border-bottom-right-radius: 48rpx;
  box-shadow: 0 10rpx 40rpx rgba(255, 71, 87, 0.2); position: relative; z-index: 100;
  
  .status-bar { height: var(--status-bar-height, 44px); }
  .nav-bar {
    display: flex; justify-content: space-between; align-items: center; padding: 20rpx 40rpx;
    .location { display: flex; align-items: center; background: rgba(0,0,0,0.15); padding: 12rpx 24rpx; border-radius: 36rpx; backdrop-filter: blur(10px); .loc-icon { width: 36rpx; height: 36rpx; margin-right: 8rpx;} .loc-text { font-size: 28rpx; color: #fff; font-weight: 800; margin-right: 8rpx; letter-spacing: 1rpx;} .arrow-icon { width: 32rpx; height: 32rpx; }}
    .msg-icon-wrap { position: relative; .msg-icon { width: 52rpx; height: 52rpx; } .badge { position: absolute; top: 0; right: 0; width: 18rpx; height: 18rpx; background: #fff; border-radius: 50%; box-shadow: 0 0 8rpx rgba(0,0,0,0.3);}}
  }
  
  .search-bar {
    margin: 20rpx 40rpx 0; background: rgba(255,255,255,0.95); height: 96rpx; border-radius: 48rpx; display: flex; justify-content: space-between; align-items: center; padding: 8rpx 8rpx 8rpx 36rpx; box-shadow: 0 12rpx 30rpx rgba(0,0,0,0.06); transition: transform 0.2s; border: 2rpx solid rgba(255,255,255,0.5);
    &:active { transform: scale(0.98); }
    .search-inner { display: flex; align-items: center; flex: 1; .search-icon { width: 44rpx; height: 44rpx; margin-right: 16rpx;} .search-placeholder { font-size: 28rpx; color: #777; font-weight: 500;}}
    .search-btn { background: linear-gradient(135deg, #1e272e, #3a4b5c); color: #fff; font-size: 28rpx; font-weight: 800; padding: 0 44rpx; height: 80rpx; line-height: 80rpx; border-radius: 40rpx; box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.15); }
  }
}

.scroll-pad { height: calc(var(--status-bar-height, 44px) + 20rpx); }
.main-scroll { flex: 1; height: 0; padding: 0 30rpx; box-sizing: border-box; position: relative; z-index: 10; }

.fade-up { animation: fadeUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both; }
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(40rpx); } to { opacity: 1; transform: translateY(0); } }

.category-grid {
  display: flex; justify-content: space-between; margin-top: 30rpx; margin-bottom: 40rpx; padding: 0 10rpx;
  .category-item {
    display: flex; flex-direction: column; align-items: center; transition: transform 0.2s;
    &:active { transform: scale(0.9); }
    .icon-wrap { width: 108rpx; height: 108rpx; border-radius: 40rpx; display: flex; justify-content: center; align-items: center; margin-bottom: 16rpx; box-shadow: 0 12rpx 24rpx rgba(0,0,0,0.06); border: 2rpx solid rgba(255,255,255,0.5); .cat-icon { width: 56rpx; height: 56rpx; filter: drop-shadow(0 4rpx 6rpx rgba(0,0,0,0.05));}}
    .cat-name { font-size: 24rpx; color: #333; font-weight: 800; }
  }
}

.seckill-panel {
  background: #fff; border-radius: 40rpx; padding: 36rpx; margin-bottom: 40rpx;
  box-shadow: 0 16rpx 48rpx rgba(0,0,0,0.04);
  background-image: radial-gradient(circle at 100% 0%, rgba(255, 90, 95, 0.08), transparent 50%);
  border: 2rpx solid rgba(255, 90, 95, 0.08);

  .panel-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx;
    .title-row {
      display: flex; align-items: center; 
      .fire-box { background: linear-gradient(135deg, #FF6B6B, #FF4757); padding: 10rpx; border-radius: 12rpx; margin-right: 16rpx; .s-icon { width: 32rpx; height: 32rpx; }}
      .title { font-size: 34rpx; font-weight: 900; color: #1a1a1a; margin-right: 24rpx; letter-spacing: 1rpx;}
      .countdown { display: flex; align-items: center; .time-box { background: rgba(255, 71, 87, 0.1); color: #FF4757; font-size: 24rpx; font-weight: 900; padding: 6rpx 12rpx; border-radius: 8rpx;} .colon { color: #FF4757; font-weight: 900; margin: 0 8rpx; }}
    }
    .more-btn { display: flex; align-items: center; padding: 6rpx 16rpx; border-radius: 20rpx; .txt { font-size: 26rpx; color: #999; font-weight: bold;} .arrow { width: 36rpx; height: 36rpx; opacity: 0.6;}}
  }
  
  .seckill-scroll {
    .seckill-list {
      display: flex; gap: 24rpx; padding-bottom: 10rpx;
      .s-item {
        width: 200rpx; flex-shrink: 0;
        .img-box {
          position: relative; width: 100%; height: 200rpx; border-radius: 24rpx; overflow: hidden; margin-bottom: 16rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); border: 2rpx solid #F5F5F5;
          .cover { width: 100%; height: 100%; }
          .discount-tag { position: absolute; top: 0; left: 0; background: linear-gradient(135deg, #FF6B6B, #FF4757); color: #fff; font-size: 22rpx; font-weight: 900; padding: 6rpx 16rpx; border-bottom-right-radius: 24rpx; box-shadow: 2rpx 2rpx 10rpx rgba(255, 71, 87, 0.3);}
        }
        .name { font-size: 26rpx; color: #333; font-weight: 800; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; margin-bottom: 8rpx; letter-spacing: 1rpx;}
        .price-row { display: flex; align-items: baseline; .sec-price { color: #FF4757; font-size: 34rpx; font-weight: 900; line-height: 1; margin-right: 12rpx; .rmb {font-size: 22rpx; margin-right: 2rpx;}} .old-price { color: #aaa; font-size: 22rpx; text-decoration: line-through; font-weight: 500;}}
      }
    }
  }
}

.ai-recommend-box {
  position: relative; margin-bottom: 40rpx; border-radius: 44rpx; overflow: hidden; background: #fff; padding: 3rpx;
  box-shadow: 0 16rpx 48rpx rgba(108, 92, 231, 0.08); border: 2rpx solid rgba(108, 92, 231, 0.1);
  
  .ai-glow-bg {
    position: absolute; top: -30%; left: -20%; width: 500rpx; height: 500rpx;
    background: radial-gradient(circle, rgba(108, 92, 231, 0.15) 0%, rgba(255,255,255,0) 70%); border-radius: 50%; z-index: 0; pointer-events: none;
  }
  .ai-content {
    background: rgba(255,255,255,0.85); backdrop-filter: blur(30px); border-radius: 40rpx; padding: 36rpx; position: relative; z-index: 10;
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx;
      .ai-badge { display: flex; align-items: center; background: linear-gradient(135deg, #6C5CE7, #A29BFE); padding: 10rpx 28rpx 10rpx 16rpx; border-radius: 30rpx; box-shadow: 0 6rpx 20rpx rgba(108, 92, 231, 0.25); .robot { width: 36rpx; height: 36rpx; margin-right: 12rpx;} color: #fff; font-size: 26rpx; font-weight: 900; letter-spacing: 1rpx;}
      .refresh { display: flex; align-items: center; background: #F0EDFF; padding: 10rpx 24rpx; border-radius: 24rpx; .ref-icon { width: 32rpx; height: 32rpx; margin-right: 6rpx;} color: #6C5CE7; font-size: 24rpx; font-weight: 800;}
    }
    .ai-msg { font-size: 30rpx; color: #1a1a1a; line-height: 1.6; font-weight: 800; font-family: "PingFang SC", sans-serif; display: block; margin-bottom: 30rpx; background: rgba(255,255,255,0.6); padding: 24rpx; border-radius: 20rpx; border-left: 8rpx solid #A29BFE; box-shadow: inset 0 2rpx 10rpx rgba(0,0,0,0.02);}
    
    .ai-shop-list {
      .ai-shop-card {
        background: #fff; border-radius: 28rpx; padding: 24rpx; display: flex; margin-bottom: 20rpx; box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.04); border: 2rpx solid rgba(108, 92, 231, 0.05); transition: transform 0.2s;
        &:active { transform: scale(0.98); }
        .left { flex: 1; margin-right: 24rpx; display: flex; flex-direction: column; justify-content: center; .s-name { font-size: 34rpx; font-weight: 800; color: #1a1a1a; margin-bottom: 12rpx;} .s-meta { display: flex; align-items: center; font-size: 24rpx; color: #666; margin-bottom: 20rpx; font-weight: bold; .star {width: 32rpx; height: 32rpx; margin-right: 8rpx;} .score{color: #FF8C42;} .div{margin: 0 16rpx; color: #e0e0e0;}} .ai-reason { display: flex; align-items: flex-start; background: #FFF0F0; padding: 16rpx 20rpx; border-radius: 16rpx; .sparkle { width: 32rpx; height: 32rpx; margin-right: 12rpx; flex-shrink: 0;} .r-txt { font-size: 24rpx; color: #FF5A5F; line-height: 1.4; font-weight: 600;}}}
        .s-cover { width: 160rpx; height: 160rpx; border-radius: 24rpx; background: #eee; flex-shrink: 0; box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);}
      }
    }

    .ai-action-bar {
      margin-top: 12rpx;

      .ask-btn {
        text-align: center;
        background: linear-gradient(135deg, #1e272e, #3a4b5c);
        color: #fff;
        font-size: 28rpx;
        font-weight: 800;
        padding: 22rpx 0;
        border-radius: 22rpx;
        margin-bottom: 18rpx;
      }

      .scene-list {
        display: flex;
        flex-wrap: wrap;
        gap: 14rpx;
      }

      .scene-chip {
        background: #F7F4FF;
        color: #6C5CE7;
        font-size: 24rpx;
        padding: 12rpx 20rpx;
        border-radius: 20rpx;
        font-weight: 700;
      }
    }
  }
}

.nearby-section {
  .section-title { font-size: 38rpx; font-weight: 900; color: #1a1a1a; margin-bottom: 30rpx; display: block; letter-spacing: 1rpx; text-shadow: 0 4rpx 10rpx rgba(0,0,0,0.03);}
  .shop-flow {
    column-count: 2; column-gap: 24rpx;
    .shop-card {
      break-inside: avoid; background: #fff; border-radius: 36rpx; margin-bottom: 24rpx; overflow: hidden; box-shadow: 0 16rpx 50rpx rgba(0,0,0,0.05); border: 2rpx solid rgba(0,0,0,0.01); transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s;
      &:active { transform: translateY(4rpx) scale(0.98); box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.03); }
      .img-wrapper { position: relative; width: 100%; height: 280rpx; .cover { width: 100%; height: 100%; background: #f0f0f0;} .status-badge { position: absolute; top: 16rpx; left: 16rpx; font-size: 22rpx; font-weight: 800; padding: 6rpx 16rpx; border-radius: 20rpx; color: #fff; backdrop-filter: blur(6px); box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.15); &.hot { background: rgba(255, 71, 87, 0.9); } &.new { background: rgba(46, 213, 115, 0.9); } } }
      .info { padding: 24rpx; .name { font-size: 32rpx; font-weight: 900; color: #1a1a1a; margin-bottom: 16rpx; display: block; line-height: 1.4; letter-spacing: 1rpx;} .meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; .score { font-size: 26rpx; color: #FF8C42; font-weight: 900; } .price { font-size: 24rpx; color: #888; font-weight: 800;} } .tags { display: flex; flex-wrap: wrap; gap: 12rpx; .tag { background: #F8F9FA; color: #444; font-size: 22rpx; padding: 8rpx 16rpx; border-radius: 12rpx; font-weight: 800; border: 2rpx solid #F0F0F0; } } }
    }
  }
}

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 0;
  .empty-img { width: 200rpx; height: 200rpx; margin-bottom: 30rpx; opacity: 0.5; }
  .empty-txt { font-size: 32rpx; color: #999; font-weight: bold; margin-bottom: 12rpx; }
  .empty-sub { font-size: 26rpx; color: #ccc; }
}

.bottom-holder { height: 100rpx; }
</style>


