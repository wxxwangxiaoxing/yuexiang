<template>
  <view class="skeleton-wrap" :class="`variant-${variant}`">
    <template v-if="variant === 'home'">
      <view class="block hero"></view>
      <view class="grid-row">
        <view class="grid-item" v-for="item in 8" :key="`grid-${item}`">
          <view class="block icon"></view>
          <view class="block text short"></view>
        </view>
      </view>
      <view class="card-row">
        <view class="block title large"></view>
        <view class="shop-card" v-for="item in 2" :key="`home-card-${item}`">
          <view class="block cover"></view>
          <view class="shop-info">
            <view class="block title"></view>
            <view class="block text"></view>
            <view class="block text short"></view>
          </view>
        </view>
      </view>
    </template>

    <template v-else-if="variant === 'message'">
      <view class="chip-row">
        <view class="chip-card" v-for="item in 4" :key="`chip-${item}`">
          <view class="block chip-icon"></view>
          <view class="block text short"></view>
        </view>
      </view>
      <view class="message-card" v-for="item in items" :key="`msg-${item}`">
        <view class="block avatar"></view>
        <view class="message-info">
          <view class="message-top">
            <view class="block title"></view>
            <view class="block text mini"></view>
          </view>
          <view class="block text"></view>
          <view class="block text short"></view>
        </view>
      </view>
    </template>

    <template v-else-if="variant === 'profile'">
      <view class="profile-card">
        <view class="profile-head">
          <view class="block avatar large-avatar"></view>
          <view class="profile-meta">
            <view class="block title large"></view>
            <view class="block text short"></view>
          </view>
        </view>
        <view class="form-card" v-for="item in 5" :key="`profile-${item}`">
          <view class="block text short"></view>
          <view class="block input"></view>
        </view>
      </view>
    </template>

    <template v-else>
      <view class="list-card" v-for="item in items" :key="`list-${item}`">
        <view class="block cover"></view>
        <view class="list-info">
          <view class="block title"></view>
          <view class="block text"></view>
          <view class="block text short"></view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'list'
  },
  count: {
    type: Number,
    default: 4
  }
})

const items = computed(() => Array.from({ length: props.count }, (_, index) => index + 1))
</script>

<style lang="scss" scoped>
.skeleton-wrap {
  width: 100%;
}

.block {
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #f1f3f6 25%, #fafbfc 50%, #f1f3f6 75%);
  background-size: 400% 100%;
  animation: shimmer 1.6s infinite linear;
  border-radius: 18rpx;
}

.title {
  height: 30rpx;
  margin-bottom: 16rpx;
}

.large {
  width: 50%;
}

.text {
  height: 24rpx;
  margin-bottom: 14rpx;
}

.short {
  width: 45%;
}

.mini {
  width: 90rpx;
}

.hero {
  height: 220rpx;
  border-radius: 40rpx;
  margin-bottom: 30rpx;
}

.grid-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx 18rpx;
  margin-bottom: 34rpx;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 32rpx;
  margin-bottom: 16rpx;
}

.card-row {
  background: #fff;
  border-radius: 36rpx;
  padding: 28rpx;
}

.shop-card,
.list-card,
.message-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 28rpx;
  padding: 24rpx;
  margin-bottom: 22rpx;
}

.cover {
  width: 180rpx;
  height: 180rpx;
  border-radius: 24rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.shop-info,
.list-info,
.message-info {
  flex: 1;
}

.chip-row {
  display: flex;
  gap: 18rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
}

.chip-card {
  min-width: 150rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 30rpx;
  padding: 24rpx 18rpx;
}

.chip-icon {
  width: 86rpx;
  height: 86rpx;
  border-radius: 28rpx;
  margin-bottom: 16rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.large-avatar {
  width: 128rpx;
  height: 128rpx;
}

.message-top,
.profile-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.profile-card {
  padding: 30rpx;
  background: #fff;
  border-radius: 40rpx;
}

.profile-meta {
  flex: 1;
  margin-left: 24rpx;
}

.form-card {
  margin-top: 24rpx;
}

.input {
  height: 84rpx;
  border-radius: 24rpx;
}

@keyframes shimmer {
  0% {
    background-position: 100% 0;
  }

  100% {
    background-position: -100% 0;
  }
}
</style>
