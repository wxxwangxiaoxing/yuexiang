<template>
  <view class="shop-detail">
    <!-- 自定义玻璃态导航栏 -->
    <view class="nav-bar" :class="{ 'nav-scrolled': isScrolled }">
      <view class="nav-glass-bg" v-if="isScrolled"></view>
      <view class="btn-box" @click="goBack">
        <AppIcon class="action-icon" name="chevron-left" color="#fff" />
      </view>
      <text class="title" :class="{ 'show-title': isScrolled }">{{ shopTitle }}</text>
      <view class="btn-box right-btn" @click="toggleFavorite">
        <AppIcon class="action-icon" :name="isFavorite ? 'heart' : 'heart-outline'" color="#fff" />
      </view>
    </view>
    
    <!-- 全屏沉浸式轮播 -->
    <view class="hero-header">
      <swiper class="swiper" indicator-active-color="#FF5A5F" indicator-dots autoplay circular>
        <swiper-item v-for="(img, i) in covers" :key="i">
          <image :src="img" class="slide-image" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
      <view class="hero-gradient"></view>
    </view>
    
    <view class="main-content">
      <!-- 核心信息悬浮卡片 -->
      <view class="info-card fade-up">
        <view class="title-row">
          <text class="shop-name">{{ shopNameWithArea }}</text>
          <view class="tag-brand">精选</view>
        </view>
        
        <view class="meta-row">
          <view class="score-wrap">
            <AppIcon class="star" name="star" color="#FF8C42" />
            <text class="score">{{ shopScore }}</text>
            <text class="score-lbl">分</text>
          </view>
          <view class="divider"></view>
          <text class="price">¥{{ shopAvgPrice }}/人</text>
          <view class="divider"></view>
          <text class="type">{{ shopTypeText }}</text>
        </view>
        
        <view class="address-box" @click="openLocation">
          <view class="left">
            <view class="loc">
              <AppIcon class="icon" name="map-marker" color="#FF5A5F" />
              <text class="txt">{{ shopAddress }}</text>
            </view>
            <view class="loc">
              <AppIcon class="icon" name="clock-outline" color="#999" />
              <text class="txt time-txt">营业时间 {{ shopOpenHours }}</text>
            </view>
          </view>
          <view class="phone-btn" @click.stop="makeCall">
            <AppIcon class="phone-icon" name="phone" color="#fff" />
          </view>
        </view>
      </view>
      
      <!-- 评价统计 -->
      <view class="ai-comment-card fade-up delay-1" v-if="reviewSummary">
        <view class="glow-bg"></view>
        <view class="header">
          <view class="ai-badge">
            <AppIcon class="ai-icon" name="star-circle" color="#fff" />
            <text class="tit">评价统计</text>
          </view>
          <text class="score-lbl">综合评分 <text class="green">{{ reviewSummary.avgScore || '0' }}</text></text>
        </view>
        <view class="score-bars">
          <view class="s-bar" v-for="item in scoreBars" :key="item.level">
            <text class="s-label">{{ item.level }}分</text>
            <view class="s-track"><view class="s-fill" :style="{ width: item.percent + '%' }"></view></view>
            <text class="s-count">{{ item.count }}</text>
          </view>
        </view>
        <view class="footer-tip">—— 共 {{ reviewSummary.totalReviews || 0 }} 条评价 ——</view>
      </view>
      
      <!-- 创意打孔优惠券 -->
      <view class="voucher-card fade-up delay-2">
        <view class="header">
          <text class="title">特惠套餐与代金券</text>
          <text class="more">查看全部 ></text>
        </view>
        <view class="v-list">
          <!-- 券卡 1 -->
          <view class="ticket active-ticket" @click="goSeckill">
            <view class="t-left">
              <view class="p-wrap">
                <text class="rmb">¥</text>
                <text class="val">{{ voucher0 ? formatMoney(voucher0.payValue) : '0' }}</text>
              </view>
              <view class="t-info">
                <text class="t-name">{{ voucher0 ? voucher0.voucherTitle : '' }}</text>
                <text class="t-sub">{{ voucher0 ? `可抵扣 ¥${formatMoney(voucher0.actualValue)}` : '' }}</text>
              </view>
            </view>
            <view class="t-divider"></view>
            <view class="t-right">
              <view class="buy-btn shadow-red">立即抢</view>
            </view>
          </view>
          <!-- 券卡 2 -->
          <view class="ticket">
            <view class="t-left">
              <view class="p-wrap">
                <text class="rmb">¥</text>
                <text class="val">{{ voucher1 ? formatMoney(voucher1.payValue) : '0' }}</text>
              </view>
              <view class="t-info">
                <text class="t-name">{{ voucher1 ? voucher1.voucherTitle : '' }}</text>
                <text class="t-sub">{{ voucher1 ? `可抵扣 ¥${formatMoney(voucher1.actualValue)}` : '' }}</text>
              </view>
            </view>
            <view class="t-divider"></view>
            <view class="t-right">
              <view class="buy-btn">去购买</view>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 悬浮吸底操作栏 -->
    <view class="glass-bottom-bar">
      <view class="action-grid">
        <view class="action">
          <AppIcon class="i" name="store-outline" color="#666" />
          <text class="t">门店</text>
        </view>
        <view class="action">
          <AppIcon class="i" name="share-variant-outline" color="#666" />
          <text class="t">分享</text>
        </view>
      </view>
      <view class="primary-btn" @click="goPublish">
        <AppIcon class="btn-ic" name="pencil-plus-outline" color="#fff" />
        <text>写评价</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api'

const isScrolled = ref(false)

const shopDetail = ref(null)
const covers = ref([])
const voucherOrders = ref([])
const reviewSummary = ref(null)
const isFavorite = ref(false)

const scoreBars = computed(() => {
  const s = reviewSummary.value
  if (!s) return []
  const maxCount = Math.max(s.score5Count || 0, s.score4Count || 0, s.score3Count || 0, s.score2Count || 0, s.score1Count || 0, 1)
  return [
    { level: 5, count: s.score5Count || 0, percent: ((s.score5Count || 0) / maxCount) * 100 },
    { level: 4, count: s.score4Count || 0, percent: ((s.score4Count || 0) / maxCount) * 100 },
    { level: 3, count: s.score3Count || 0, percent: ((s.score3Count || 0) / maxCount) * 100 },
    { level: 2, count: s.score2Count || 0, percent: ((s.score2Count || 0) / maxCount) * 100 },
    { level: 1, count: s.score1Count || 0, percent: ((s.score1Count || 0) / maxCount) * 100 },
  ]
})

const shopTitle = computed(() => shopDetail.value?.name || '')
const shopNameWithArea = computed(() => {
  const name = shopDetail.value?.name || ''
  const area = shopDetail.value?.area
  return area ? `${name} (${area})` : name
})
const shopScore = computed(() => {
  const s = shopDetail.value?.score
  return s === null || s === undefined ? '' : String(s)
})
const shopAvgPrice = computed(() => shopDetail.value?.avgPrice ?? '0')
const shopTypeText = computed(() => shopDetail.value?.typeName || shopDetail.value?.area || '')
const shopAddress = computed(() => shopDetail.value?.address || '')
const shopOpenHours = computed(() => {
  const raw = shopDetail.value?.openHours
  if (!raw) return '暂无营业时间'
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (obj.open && obj.close) {
      return `${obj.open} - ${obj.close}`
    }
    if (obj.open) return obj.open
    return '暂无营业时间'
  } catch {
    return raw || '暂无营业时间'
  }
})

const voucher0 = computed(() => voucherOrders.value[0] || null)
const voucher1 = computed(() => voucherOrders.value[1] || null)

const normalizeId = (value) => {
  if (value === null || value === undefined) return ''
  const text = String(value).trim()
  if (!text || text === 'undefined' || text === 'null' || text === 'NaN') return ''
  return text
}

const formatMoney = (v) => {
  if (v === null || v === undefined) return '0'
  const num = Number(v)
  if (!isFinite(num)) return String(v)
  const isInt = Math.abs(num - Math.round(num)) < 1e-6
  return isInt ? String(Math.round(num)) : num.toFixed(2)
}

const goBack = () => {
  uni.navigateBack()
}

const openLocation = () => {
  if (!shopDetail.value?.latitude || !shopDetail.value?.longitude) {
    uni.showToast({ title: '暂无定位信息', icon: 'none' })
    return
  }
  uni.openLocation({
    latitude: Number(shopDetail.value.latitude),
    longitude: Number(shopDetail.value.longitude),
    name: shopDetail.value.name,
    address: shopDetail.value.address
  })
}

const makeCall = () => {
  if (!shopDetail.value?.phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({
    phoneNumber: String(shopDetail.value.phone)
  })
}

const goSeckill = () => {
  uni.navigateTo({ url: '/pages/voucher/seckill'})
}

const goPublish = () => {
  uni.navigateTo({ url: '/pages/blog/publish'})
}

const toggleFavorite = async () => {
  if (!shopDetail.value?.id) return
  try {
    await api.shop.toggleFavorite(shopDetail.value.id)
    isFavorite.value = !isFavorite.value
    uni.showToast({ title: isFavorite.value ? '已收藏' : '已取消收藏', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
  }
}

onPageScroll((event) => {
  isScrolled.value = event.scrollTop > 120
})

onMounted(async () => {
  try {
    const pages = getCurrentPages?.()
    const current = pages?.[pages.length - 1]
    const options = current?.options || {}
    const id = normalizeId(options.id || options.shopId)
    if (!id) return

    const [detailRes, voucherRes, summaryRes, favoriteRes] = await Promise.all([
      api.shop.getShopDetail(id),
      api.voucher.getShopVouchers(id, { pageNo: 1, pageSize: 2 }),
      api.shop.getReviewSummary(id),
      api.shop.isFavorite(id).catch(() => false)
    ])

    shopDetail.value = detailRes
    covers.value = detailRes?.images || []
    voucherOrders.value = voucherRes?.list || []
    reviewSummary.value = summaryRes
    isFavorite.value = Boolean(favoriteRes)
  } catch (e) {
    uni.showToast({ title: '加载商户信息失败', icon: 'none' })
  }
})
</script>

<style lang="scss" scoped>
/* 全局设定 */
.shop-detail { 
  background: #F4F6F9; 
  min-height: 100vh; 
  padding-bottom: 200rpx; 
}

/* 玻璃态顶部导航栏 */
.nav-bar {
  position: fixed; top: 0; left: 0; right: 0;
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 30rpx; padding-top: calc(var(--status-bar-height, 44px) + 20rpx);
  z-index: 100; transition: all 0.3s;
  
  .nav-glass-bg {
    position: absolute; top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(255,255,255,0.85); backdrop-filter: blur(20px); z-index: -1;
  }
  
  .title { 
    font-size: 34rpx; font-weight: bold; color: #333; z-index: 1; opacity: 0; transform: translateY(20rpx); transition: all 0.3s;
    &.show-title { opacity: 1; transform: translateY(0); }
  }
  
  .btn-box {
    width: 72rpx; height: 72rpx; border-radius: 50%;
    background: rgba(0,0,0,0.3); backdrop-filter: blur(10px);
    display: flex; justify-content: center; align-items: center; z-index: 1;
    .action-icon { width: 44rpx; height: 44rpx; }
  }
}

/* 轮播图区域 */
.hero-header {
  position: relative; height: 600rpx;
  .swiper { width: 100%; height: 100%; .slide-image { width: 100%; height: 100%; }}
  .hero-gradient {
    position: absolute; bottom: 0; left: 0; width: 100%; height: 200rpx;
    background: linear-gradient(to top, #F4F6F9 0%, transparent 100%);
  }
}

/* 主内容容器动画 */
.main-content {
  padding: 0 30rpx; margin-top: -80rpx; position: relative; z-index: 10;
  
  .fade-up {
    animation: fadeUpAnim 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
  }
  .delay-1 { animation-delay: 0.1s; }
  .delay-2 { animation-delay: 0.2s; }
}

@keyframes fadeUpAnim {
  from { opacity: 0; transform: translateY(40rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* 核心信息卡片 */
.info-card {
  background: #fff; border-radius: 32rpx; padding: 40rpx 36rpx;
  box-shadow: 0 16rpx 48rpx rgba(0,0,0,0.06); margin-bottom: 30rpx;
  
  .title-row {
    display: flex; align-items: center; margin-bottom: 16rpx;
    .shop-name { font-size: 44rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 1rpx; }
    .tag-brand { background: linear-gradient(135deg, #FF6B6B, #FF4757); color: #fff; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 12rpx; border-bottom-left-radius: 0; font-weight: bold; margin-left: 16rpx;}
  }
  
  .meta-row {
    display: flex; align-items: center; font-size: 26rpx; margin-bottom: 36rpx;
    .score-wrap { display: flex; align-items: baseline; .star { width: 30rpx; height: 30rpx; margin-right: 6rpx; transform: translateY(4rpx); } .score { color: #FF6B6B; font-weight: 900; font-size: 34rpx; margin-right: 4rpx;} .score-lbl { color: #FF6B6B; font-size: 24rpx; font-weight: bold;}}
    .divider { width: 4rpx; height: 20rpx; background: #E0E0E0; margin: 0 20rpx; border-radius: 2rpx;}
    .price, .type { color: #666; font-weight: 500;}
  }
  
  .address-box {
    display: flex; justify-content: space-between; align-items: center; 
    background: #F9FAFB; padding: 24rpx; border-radius: 20rpx;
    .left {
      display: flex; flex-direction: column;
      .loc { 
        display: flex; align-items: center; margin-bottom: 16rpx;
        &:last-child { margin-bottom: 0; }
        .icon { width: 32rpx; height: 32rpx; margin-right: 12rpx;} 
        .txt { font-size: 26rpx; color: #333; font-weight: bold;}
        .time-txt { color: #888; font-weight: normal; }
      }
    }
    .phone-btn {
      width: 80rpx; height: 80rpx; background: linear-gradient(135deg, #FF6B6B, #FF4757); border-radius: 50%;
      display: flex; justify-content: center; align-items: center; box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.3);
      .phone-icon { width: 40rpx; height: 40rpx; }
      &:active { transform: scale(0.9); }
    }
  }
}

/* AI 极光卡片 */
.ai-comment-card {
  position: relative; border-radius: 32rpx; padding: 40rpx 36rpx; margin-bottom: 30rpx;
  background: #fff; overflow: hidden;
  box-shadow: 0 16rpx 48rpx rgba(108, 92, 231, 0.06);
  border: 2rpx solid rgba(108, 92, 231, 0.1);
  
  .glow-bg {
    position: absolute; top: -50%; right: -20%; width: 300rpx; height: 300rpx;
    background: radial-gradient(circle, rgba(108,92,231,0.15) 0%, rgba(255,255,255,0) 70%);
    border-radius: 50%; z-index: 0;
  }
  
  .header { 
    position: relative; z-index: 1; display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx;
    .ai-badge { 
      background: linear-gradient(90deg, #6C5CE7, #A29BFE); padding: 8rpx 24rpx 8rpx 12rpx; border-radius: 30rpx;
      display: flex; align-items: center;
      .ai-icon { width: 36rpx; height: 36rpx; margin-right: 8rpx; }
      .tit { font-size: 26rpx; font-weight: bold; color: #fff;}
    }
    .score-lbl { font-size: 26rpx; color: #666; font-weight: 500; .green { color: #2ED573; font-weight: 900; font-size: 34rpx; margin-left: 8rpx;} }
  }
  
  .tag-group {
    position: relative; z-index: 1; margin-bottom: 30rpx;
    .tag-row { 
      display: flex; flex-wrap: wrap; align-items: center; 
      .emoji { margin-right: 16rpx; font-size: 36rpx;}
      .pill { 
        font-size: 24rpx; padding: 10rpx 24rpx; border-radius: 30rpx; margin-right: 16rpx; font-weight: bold; 
        &.good { background: rgba(46, 213, 115, 0.1); color: #27ae60; border: 2rpx solid rgba(46, 213, 115, 0.2);}
        &.bad { background: rgba(255, 107, 107, 0.1); color: #c0392b; border: 2rpx solid rgba(255, 107, 107, 0.2);}
      }
    }
  }
  
  .score-bars {
    position: relative; z-index: 1; margin-bottom: 20rpx;
    .s-bar {
      display: flex; align-items: center; margin-bottom: 12rpx;
      .s-label { font-size: 24rpx; color: #666; width: 40rpx; text-align: right; margin-right: 12rpx; font-weight: bold;}
      .s-track { flex: 1; height: 16rpx; background: #F0F0F0; border-radius: 8rpx; overflow: hidden; margin: 0 12rpx; }
      .s-fill { height: 100%; background: linear-gradient(90deg, #FF6B6B, #FF4757); border-radius: 8rpx; transition: width 0.3s;}
      .s-count { font-size: 24rpx; color: #999; width: 60rpx; font-weight: bold;}
    }
  }
  
  .footer-tip { position: relative; z-index: 1; font-size: 22rpx; color: #A29BFE; text-align: center; font-weight: 500; letter-spacing: 2rpx;}
}

/* 创意打孔优惠券 */
.voucher-card {
  margin-bottom: 30rpx;
  .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; .title { font-size: 36rpx; font-weight: 800; color: #1a1a1a; } .more { font-size: 26rpx; color: #999; font-weight: 500;}}
  
  .v-list {
    .ticket {
      display: flex; background: #fff; border-radius: 20rpx; margin-bottom: 24rpx;
      box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.03); overflow: hidden; position: relative;
      
      &.active-ticket {
        background: linear-gradient(to right, #FFF5F5, #fff);
      }
      
      .t-left {
        flex: 1; padding: 36rpx 30rpx; display: flex; align-items: center;
        .p-wrap { color: #FF4757; font-weight: 900; margin-right: 24rpx; display: flex; align-items: baseline; .rmb { font-size: 32rpx; margin-right: 4rpx;} .val { font-size: 64rpx; line-height: 1;}}
        .t-info { display: flex; flex-direction: column; .t-name { font-size: 32rpx; font-weight: 800; color: #333; margin-bottom: 8rpx;} .t-sub { font-size: 24rpx; color: #888; font-weight: 500;}}
      }
      
      /* 虚线打孔效果 */
      .t-divider {
        width: 0; border-left: 4rpx dashed #FFE4E4; position: relative;
        &::before, &::after { content: ''; position: absolute; left: -14rpx; width: 24rpx; height: 24rpx; background: #F4F6F9; border-radius: 50%; box-shadow: inset 0 6rpx 10rpx rgba(0,0,0,0.02);}
        &::before { top: -14rpx; }
        &::after { bottom: -14rpx; box-shadow: inset 0 -6rpx 10rpx rgba(0,0,0,0.02);}
      }
      
      .t-right {
        width: 200rpx; display: flex; justify-content: center; align-items: center;
        .buy-btn { 
          background: #FF4757; color: #fff; font-size: 30rpx; font-weight: bold; width: 140rpx; height: 64rpx; 
          border-radius: 32rpx; display: flex; justify-content: center; align-items: center;
          &.shadow-red { box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.4); }
        }
      }
    }
  }
}

/* 吸底毛玻璃操作栏 */
.glass-bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; height: 160rpx;
  background: rgba(255,255,255,0.9); backdrop-filter: blur(20px); border-top: 2rpx solid rgba(255,255,255,0.5);
  display: flex; justify-content: space-between; align-items: center; padding: 0 40rpx;
  padding-bottom: env(safe-area-inset-bottom); z-index: 200; box-shadow: 0 -8rpx 40rpx rgba(0,0,0,0.05);
  
  .action-grid {
    display: flex; gap: 40rpx;
    .action { display: flex; flex-direction: column; align-items: center; justify-content: center; .i { width: 48rpx; height: 48rpx; margin-bottom: 6rpx;} .t { font-size: 22rpx; color: #555; font-weight: bold;}}
  }
  
  .primary-btn {
    width: 400rpx; height: 90rpx; background: linear-gradient(135deg, #FF6B6B, #FF4757); border-radius: 45rpx;
    display: flex; justify-content: center; align-items: center; color: #fff; font-size: 32rpx; font-weight: bold;
    box-shadow: 0 12rpx 30rpx rgba(255, 71, 87, 0.35); transition: transform 0.2s;
    &:active { transform: scale(0.96); }
    .btn-ic { width: 44rpx; height: 44rpx; margin-right: 12rpx;}
  }
}
</style>
