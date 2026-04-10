<template>
  <view class="seckill-container">
    <!-- 时间轴 -->
    <view class="time-tabs" v-if="sessions.length">
      <view
        class="tab"
        v-for="(s, idx) in sessions"
        :key="s.sessionId"
        :class="{ active: currentIdx === idx }"
        @click="currentIdx = idx"
      >
        <text class="time">{{ formatHm(s.beginTime) }}</text>
        <text class="status">{{ s.statusDesc }}</text>
      </view>
    </view>
    <view class="time-tabs" v-else>
      <view class="tab active">
        <text class="time">--:--</text>
        <text class="status">加载中</text>
      </view>
    </view>
    
    <view class="timing-bar" v-if="currentSession">
      <text class="left">抢购中，先到先得</text>
      <view class="right">
        <text class="lbl">距结束</text>
        <text class="t-box">{{ countdown.hh }}</text>:<text class="t-box">{{ countdown.mm }}</text>:<text class="t-box">{{ countdown.ss }}</text>
      </view>
    </view>
    <view class="timing-bar" v-else>
      <text class="left">抢购中，先到先得</text>
    </view>

    <!-- 列表 -->
    <view class="goods-list">
      <view class="goods-item" v-for="item in goods" :key="item.voucherId">
        <image class="cover" :src="item.shopImage" mode="aspectFill"></image>
        <view class="info">
          <view class="top">
            <text class="title">{{ item.title }}</text>
            <text class="sub">{{ item.subTitle }}</text>
          </view>
          
          <view class="price-row">
            <view class="p-left">
              <view class="sec-price"><text class="rmb">¥</text>{{ fenToYuan(item.seckillPrice) }}</view>
              <text class="ori-price">¥{{ fenToYuan(item.originalPrice) }}</text>
            </view>
            <view class="p-right">
              <view class="btn" :class="{ disabled: isButtonDisabled(item) }" @click="doSeckill(item.voucherId)">
                {{ buttonText(item) }}
              </view>
              <view class="progress-wrap">
                <view class="progress" :style="'width:' + (item.stockPercent || 0) + '%'"></view>
                <text class="p-text">库存剩余 {{ item.stockPercent || 0 }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="!goods.length" class="empty">
        <text>暂无秒杀券</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import api from '../../api'

const sessions = ref([])
const currentIdx = ref(0)
const goods = ref([])

const currentSession = computed(() => sessions.value[currentIdx.value] || null)

const countdown = ref({ hh: '00', mm: '00', ss: '00' })
let countdownTimer = null

const formatHm = (ms) => {
  if (!ms) return '--:--'
  const d = new Date(ms)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

const fenToYuan = (fen) => {
  if (fen === null || fen === undefined) return '0'
  const yuan = Number(fen) / 100
  const isInt = Math.abs(yuan - Math.round(yuan)) < 1e-6
  return isInt ? String(Math.round(yuan)) : yuan.toFixed(2)
}

const isButtonDisabled = (item) => {
  if (!item) return true
  const remain = item.remainStock || 0
  return remain <= 0 || item.seckillStatus !== 1
}

const buttonText = (item) => {
  if (!item) return '马上抢'
  if ((item.remainStock || 0) <= 0) return '已抢光'
  if (item.seckillStatus !== 1) return item.seckillStatusDesc || '不可抢'
  return '马上抢'
}

const loadVouchersForCurrent = async () => {
  const s = currentSession.value
  if (!s) {
    goods.value = []
    return
  }

  const res = await api.voucher.getSeckillVouchers(s.sessionId, { page: 1, pageSize: 10 })

  goods.value = res.list || res.vouchers || []
}

const loadSessions = async () => {
  const res = await api.voucher.getSeckillSessions()

  sessions.value = res.sessions || []
  if (!sessions.value.length) return

  const idx = sessions.value.findIndex((s) => s.status === 1)
  currentIdx.value = idx >= 0 ? idx : 0
  await loadVouchersForCurrent()
}

const updateCountdown = () => {
  const s = currentSession.value
  if (!s || !s.endTime) {
    countdown.value = { hh: '00', mm: '00', ss: '00' }
    return
  }

  let diff = Number(s.endTime) - Date.now()
  if (diff < 0) diff = 0

  const hh = String(Math.floor(diff / 3600000)).padStart(2, '0')
  const mm = String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0')
  const ss = String(Math.floor((diff % 60000) / 1000)).padStart(2, '0')

  countdown.value = { hh, mm, ss }
}

watch(currentIdx, () => {
  loadVouchersForCurrent()
})

const doSeckill = async (voucherId) => {
  if (!voucherId) return
  try {
    const res = await api.voucher.doSeckill({ voucherId })
    uni.showToast({ title: `下单成功: ${res.orderNo || ''}`, icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '秒杀下单失败', icon: 'none' })
  }
}

onMounted(async () => {
  await loadSessions()
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style lang="scss" scoped>
.seckill-container { min-height: 100vh; background: #F8F9FA; display: flex; flex-direction: column;}
.time-tabs {
  background: #333; display: flex;
  .tab {
    flex: 1; display: flex; flex-direction: column; align-items: center; padding: 24rpx 0; color: #999;
    &.active { background: #FF4757; color: #fff; font-weight: bold; }
    .time { font-size: 36rpx; margin-bottom: 6rpx; }
    .status { font-size: 24rpx; }
  }
}
.timing-bar {
  background: #fff; padding: 24rpx 30rpx; display: flex; justify-content: space-between; align-items: center;
  font-size: 26rpx; color: #333; border-bottom: 2rpx solid #F5F5F5;
  .left { font-weight: 500; }
  .right { display: flex; align-items: center; .lbl { margin-right: 12rpx; color: #666;} .t-box { background: #FF4757; color: #fff; padding: 6rpx 10rpx; border-radius: 8rpx; font-weight: bold; margin: 0 6rpx; font-size: 24rpx;}}
}
.goods-list {
  padding: 24rpx 30rpx; flex: 1;
  .goods-item {
    background: #fff; border-radius: 20rpx; padding: 24rpx; display: flex; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
    .cover { width: 220rpx; height: 220rpx; border-radius: 12rpx; margin-right: 24rpx; background: #eee; flex-shrink: 0; }
    .info {
      flex: 1; display: flex; flex-direction: column; justify-content: space-between;
      .top {
        display: flex; flex-direction: column;
        .title { font-size: 32rpx; font-weight: bold; color: #333; line-height: 1.4; margin-bottom: 8rpx;}
        .sub { font-size: 26rpx; color: #666; }
      }
      .price-row {
        display: flex; justify-content: space-between; align-items: flex-end; margin-top: auto;
        .p-left {
          display: flex; flex-direction: column;
          .sec-price { color: #FF4757; font-size: 40rpx; font-weight: bold; line-height: 1; margin-bottom: 8rpx; .rmb { font-size: 26rpx; margin-right: 4rpx; }}
          .ori-price { color: #999; font-size: 24rpx; text-decoration: line-through; }
        }
        .p-right {
          display: flex; flex-direction: column; align-items: flex-end;
          .btn { background: linear-gradient(90deg, #FF6B6B, #FF4757); color: #fff; font-size: 26rpx; font-weight: bold; padding: 14rpx 32rpx; border-radius: 36rpx; margin-bottom: 16rpx; box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.3); &.disabled { background: #ccc; box-shadow: none; }}
          .progress-wrap {
            width: 140rpx; height: 26rpx; background: #FFF0F0; border-radius: 14rpx; border: 2rpx solid #FFCDD2; position: relative; overflow: hidden;
            display: flex; justify-content: center; align-items: center; box-sizing: border-box;
            .progress { position: absolute; left: 0; top: 0; height: 100%; background: rgba(255, 71, 87, 0.25); }
            .p-text { font-size: 18rpx; color: #FF4757; position: relative; z-index: 1; font-weight: bold; letter-spacing: 1rpx;}
          }
        }
      }
    }
  }
}

  .empty {
    padding: 60rpx 0;
    text-align: center;
    color: #999;
    font-size: 28rpx;
  }
</style>
