<template>
  <view class="shop-select-page">
    <view class="nav-bar">
      <text class="cancel" @click="goBack">取消</text>
      <text class="title">选择商户</text>
      <view class="placeholder"></view>
    </view>

    <view class="search-bar-wrap">
      <view class="search-bar">
        <AppIcon class="search-icon" name="magnify" color="#999" />
        <input class="input" v-model="keyword" placeholder="搜索商户名称" confirm-type="search" @confirm="doSearch" />
        <AppIcon v-if="keyword" class="clear-icon" name="close-circle" color="#ccc" @click="clearKeyword" />
      </view>
    </view>

    <view class="type-scroll">
      <scroll-view class="type-scroll-inner" scroll-x :show-scrollbar="false">
        <view class="type-list">
          <view class="type-chip" :class="{ active: currentTypeId === '' }" @click="selectType('', '')">
            <text>全部</text>
          </view>
          <view class="type-chip" :class="{ active: currentTypeId === String(item.id) }" v-for="item in shopTypes" :key="item.id" @click="selectType(item.id, item.name)">
            <text>{{ item.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="result-list">
      <LoadingSkeleton v-if="isLoading" variant="list" :count="5" />

      <template v-else>
        <view class="s-card" v-for="item in searchResults" :key="item.id" @click="selectShop(item)">
          <image class="cover" :src="item.cover" mode="aspectFill"></image>
          <view class="info">
            <view class="top">
              <text class="name">{{ item.name }}</text>
              <view class="score-wrap">
                <AppIcon class="star" name="star" color="#FF8C42" />
                <text class="score">{{ item.score }}</text>
              </view>
            </view>
            <text class="label">{{ item.typeText }}</text>
            <view class="bot">
              <text class="dist">{{ item.addressText }}</text>
              <text class="price">¥{{ item.avgPrice }}/人</text>
            </view>
          </view>
        </view>

        <view class="empty-tip" v-if="searchResults.length === 0 && hasSearched">
          <AppIcon class="empty-icon" name="store-search-outline" color="#bbb" />
          <text class="empty-title">未找到相关商户</text>
          <text class="empty-sub">换个关键词或分类试试</text>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AppIcon from '../../components/AppIcon.vue'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import api from '../../api/index'

const defaultCover = 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=200&h=200&fit=crop'

const keyword = ref('')
const currentTypeId = ref('')
const searchResults = ref([])
const hasSearched = ref(false)
const isLoading = ref(false)
const shopTypes = ref([])

function formatPrice(price) {
  if (!price) return '0'
  return price > 100 ? (price / 100).toFixed(0) : String(price)
}

function normalizeShop(item) {
  return {
    id: item.id,
    name: item.name || '未命名商户',
    score: item.score || '0',
    cover: item.images?.[0] || item.image || defaultCover,
    typeText: [item.typeName, item.area].filter(Boolean).join(' | ') || '精选商户',
    addressText: item.address || '暂无地址信息',
    avgPrice: formatPrice(item.avgPrice)
  }
}

async function loadShopTypes() {
  try {
    const res = await api.shop.getShopTypes()
    shopTypes.value = res || []
  } catch (e) {
    shopTypes.value = []
  }
}

async function doSearch() {
  isLoading.value = true
  hasSearched.value = false
  try {
    const params = {
      pageNo: 1,
      pageSize: 20
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (currentTypeId.value) params.typeId = Number(currentTypeId.value)

    const res = await api.shop.getShopList(params)
    const list = res?.list || res?.records || []
    searchResults.value = list.map(normalizeShop)
    hasSearched.value = true
  } catch (e) {
    console.error('搜索商户失败', e)
    searchResults.value = []
    hasSearched.value = true
  } finally {
    isLoading.value = false
  }
}

function selectType(id) {
  currentTypeId.value = String(id || '')
  doSearch()
}

function clearKeyword() {
  keyword.value = ''
  doSearch()
}

function selectShop(item) {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const eventChannel = currentPage.getOpenerEventChannel?.()
  if (eventChannel) {
    eventChannel.emit('selectShop', item)
  }
  uni.navigateBack()
}

function goBack() {
  uni.navigateBack()
}

onLoad(() => {
  loadShopTypes()
  doSearch()
})
</script>

<style lang="scss" scoped>
.shop-select-page {
  min-height: 100vh;
  background: #f5f6f8;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  padding-top: calc(var(--status-bar-height, 44px) + 20rpx);
  background: #fff;
  border-bottom: 2rpx solid #f0f0f0;

  .cancel {
    font-size: 30rpx;
    color: #666;
  }

  .title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
  }

  .placeholder {
    width: 60rpx;
  }
}

.search-bar-wrap {
  padding: 20rpx 30rpx;
  background: #fff;

  .search-bar {
    height: 76rpx;
    background: #f5f6f8;
    border-radius: 38rpx;
    display: flex;
    align-items: center;
    padding: 0 28rpx;

    .search-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 12rpx;
    }

    .input {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }

    .clear-icon {
      width: 36rpx;
      height: 36rpx;
      margin-left: 12rpx;
    }
  }
}

.type-scroll {
  background: #fff;
  padding: 0 30rpx 16rpx;

  .type-scroll-inner {
    white-space: nowrap;
  }

  .type-list {
    display: inline-flex;
    gap: 16rpx;

    .type-chip {
      display: inline-flex;
      align-items: center;
      padding: 12rpx 28rpx;
      border-radius: 30rpx;
      background: #f5f6f8;
      font-size: 24rpx;
      color: #666;
      white-space: nowrap;

      &.active {
        background: #fff0f0;
        color: #ff5a5f;
        font-weight: 700;
      }
    }
  }
}

.result-list {
  flex: 1;
  padding: 20rpx 30rpx;

  .s-card {
    display: flex;
    margin-bottom: 24rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);

    .cover {
      width: 160rpx;
      height: 160rpx;
      background: #eee;
      border-radius: 16rpx;
      margin-right: 20rpx;
      flex-shrink: 0;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 4rpx 0;

      .top {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .name {
          flex: 1;
          font-size: 30rpx;
          font-weight: 700;
          color: #333;
          line-height: 1.4;
          margin-right: 12rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .score-wrap {
          display: flex;
          align-items: center;
          flex-shrink: 0;

          .star {
            width: 26rpx;
            height: 26rpx;
            margin-right: 4rpx;
          }

          .score {
            color: #ff8c42;
            font-size: 26rpx;
            font-weight: 700;
          }
        }
      }

      .label {
        font-size: 22rpx;
        color: #888;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .bot {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12rpx;

        .dist {
          flex: 1;
          font-size: 22rpx;
          color: #aaa;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .price {
          font-size: 24rpx;
          color: #ff5a5f;
          font-weight: 700;
          flex-shrink: 0;
        }
      }
    }
  }

  .empty-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 100rpx 0;

    .empty-icon {
      width: 120rpx;
      height: 120rpx;
      margin-bottom: 20rpx;
    }

    .empty-title {
      font-size: 30rpx;
      color: #666;
      font-weight: 700;
      margin-bottom: 10rpx;
    }

    .empty-sub {
      font-size: 24rpx;
      color: #aaa;
    }
  }
}
</style>
