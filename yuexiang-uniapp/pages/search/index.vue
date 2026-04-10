<template>
  <view class="search-page">
    <view class="search-header">
      <view class="search-bar">
        <AppIcon class="search-icon" name="magnify" color="#999" />
        <input
          class="input"
          v-model="keyword"
          placeholder="安静适合约会的餐厅"
          focus
          confirm-type="search"
          @confirm="submitSearch()"
        />
        <AppIcon v-if="keyword" class="clear-icon" name="close-circle" color="#ccc" @click="clearKeyword" />
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <view class="filter-row" v-if="currentTypeLabel">
      <view class="type-chip">
        <AppIcon class="chip-icon" name="shape-outline" color="#FF5A5F" />
        <text class="chip-text">{{ currentTypeLabel }}</text>
      </view>
      <text class="chip-action" @click="clearTypeFilter">清除筛选</text>
    </view>

    <view class="tool-row" v-if="hasSearchIntent || hasSearched">
      <scroll-view class="sort-scroll" scroll-x :show-scrollbar="false">
        <view class="sort-list">
          <view
            class="sort-chip"
            :class="{ active: sortBy === item.value }"
            v-for="item in sortOptions"
            :key="item.value"
            @click="changeSort(item.value)"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>
      <view class="filter-btn" @click="showFilterPanel = !showFilterPanel">
        <AppIcon class="filter-icon" name="tune" color="#666" />
        <text>筛选</text>
      </view>
    </view>

    <view class="panel-card" v-if="showFilterPanel">
      <view class="panel-block">
        <text class="panel-title">商圈筛选</text>
        <view class="chip-list">
          <text class="chip-item filter" :class="{ active: selectedArea === '' }" @click="selectedArea = ''">不限</text>
          <text
            class="chip-item filter"
            :class="{ active: selectedArea === item }"
            v-for="item in areaOptions"
            :key="item"
            @click="selectedArea = item"
          >
            {{ item }}
          </text>
        </view>
      </view>
      <view class="panel-block">
        <text class="panel-title">人均价格</text>
        <view class="chip-list">
          <text
            class="chip-item filter"
            :class="{ active: selectedPriceKey === item.key }"
            v-for="item in priceOptions"
            :key="item.key"
            @click="applyPricePreset(item)"
          >
            {{ item.label }}
          </text>
        </view>
      </view>
      <view class="panel-actions">
        <view class="action ghost" @click="resetFilters">重置</view>
        <view class="action primary" @click="applyFilters">应用筛选</view>
      </view>
    </view>

    <view class="ai-intent-card" v-if="intentTags.length > 0">
      <view class="hd">
        <AppIcon class="ai-icon" name="robot-outline" color="#fff" />
        <text class="tit">AI 理解搜索意图</text>
      </view>
      <view class="tags">
        <text class="tag" v-for="tag in intentTags" :key="tag">{{ tag }}</text>
      </view>
    </view>

    <view class="suggest-panel" v-if="showSuggestions">
      <view class="suggest-title">猜你想搜</view>
      <view class="suggest-item" v-for="item in suggestList" :key="item" @click="applySuggestion(item)">
        <AppIcon class="suggest-icon" name="arrow-top-left" color="#999" />
        <text class="suggest-text">{{ item }}</text>
      </view>
    </view>

    <view class="ai-plan-card" v-if="keyword.trim()">
      <view class="plan-head">
        <text class="plan-title">让 AI 帮你进一步收窄选择</text>
        <text class="plan-subtitle">把场景、预算和人数一起告诉它</text>
      </view>
      <view class="plan-chips">
        <text class="plan-chip" v-for="prompt in aiPrompts" :key="prompt" @click="goAiChat(prompt)">{{ prompt }}</text>
      </view>
    </view>

    <view class="result-info" v-if="hasSearchIntent || hasSearched">
      <text>{{ isLoading ? '搜索中...' : hasSearched ? `为您找到 ${totalCount} 个结果` : '输入关键词后点击搜索' }}</text>
      <text class="result-sub" v-if="selectedArea || selectedPriceText">{{ selectedArea || '全城' }}{{ selectedArea && selectedPriceText ? ' · ' : '' }}{{ selectedPriceText }}</text>
    </view>

    <view class="result-list" v-if="hasSearchIntent || hasSearched">
      <LoadingSkeleton v-if="isLoading" variant="list" :count="4" />

      <template v-else>
        <view class="s-card" v-for="item in searchResults" :key="item.id" @click="goShopDetail(item.id)">
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
          <text class="empty-sub">换个关键词，或者试试 AI 帮你描述需求</text>
        </view>

        <view class="ai-try-bar">
          <text class="txt">试试问 AI 助手获得更精准推荐</text>
          <view class="btn" @click="goAiChat()">打开 AI 工具</view>
        </view>
      </template>
    </view>

    <view class="discover-section" v-if="!hasSearchIntent && !hasSearched">
      <view class="history-block" v-if="searchHistory.length > 0">
        <view class="section-head">
          <text class="section-title">最近搜索</text>
          <text class="section-action" @click="clearHistory">清空</text>
        </view>
        <view class="chip-list">
          <text class="chip-item history" v-for="item in searchHistory" :key="item" @click="applySuggestion(item)">{{ item }}</text>
        </view>
      </view>

      <view class="hot-search">
        <view class="section-head">
          <text class="section-title">大家都在搜</text>
          <text class="section-subtitle">热门关键词实时更新</text>
        </view>
        <view class="chip-list">
          <text class="chip-item hot" v-for="item in hotShops" :key="item" @click="applySuggestion(item)">{{ item }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'
import { useChatStore } from '../../store/chat'

const HISTORY_KEY = 'search_history'
const defaultCover = 'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=200&h=200&fit=crop'

const keyword = ref('')
const currentTypeId = ref('')
const currentTypeLabel = ref('')
const searchResults = ref([])
const totalCount = ref(0)
const hasSearched = ref(false)
const isLoading = ref(false)
const hotShops = ref([])
const searchHistory = ref([])
const suggestList = ref([])
const showFilterPanel = ref(false)
const areaOptions = ref([])
const selectedArea = ref('')
const selectedPriceKey = ref('all')
const minPrice = ref('')
const maxPrice = ref('')
const sortBy = ref('score')
const chatStore = useChatStore()

const sortOptions = [
  { label: '好评优先', value: 'score' },
  { label: 'AI推荐', value: 'ai' },
  { label: '低价优先', value: 'price_asc' },
  { label: '高价优先', value: 'price_desc' }
]

const priceOptions = [
  { label: '不限', key: 'all', min: '', max: '' },
  { label: '50元以下', key: 'lt50', min: '', max: 50 },
  { label: '50-100元', key: '50to100', min: 50, max: 100 },
  { label: '100-200元', key: '100to200', min: 100, max: 200 },
  { label: '200元以上', key: 'gt200', min: 200, max: '' }
]

const hasSearchIntent = computed(() => {
  return !!keyword.value.trim() || !!currentTypeId.value || !!selectedArea.value || minPrice.value !== '' || maxPrice.value !== '' || sortBy.value !== 'score'
})

const selectedPriceText = computed(() => {
  return priceOptions.find(item => item.key === selectedPriceKey.value)?.label || ''
})

const intentTags = computed(() => {
  const text = keyword.value.trim()
  if (!text && !currentTypeLabel.value && !selectedArea.value) return []
  const tags = []
  if (currentTypeLabel.value) tags.push(currentTypeLabel.value)
  if (selectedArea.value) tags.push(selectedArea.value)
  if (text) {
    text
      .replace(/[，。！？、]/g, ' ')
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .forEach(item => tags.push(item))
  }
  if (tags.length === 1 && currentTypeLabel.value) tags.push('优先推荐', '口碑优选')
  return Array.from(new Set(tags)).slice(0, 4)
})

const showSuggestions = computed(() => keyword.value.trim() && suggestList.value.length > 0 && searchResults.value.length === 0 && !hasSearched.value)
const aiPrompts = computed(() => {
  const text = keyword.value.trim()
  if (!text) return []
  return [
    `帮我找${text}，优先推荐评价高的店`,
    `${text}，两个人去，人均100以内`,
    `${text}，更适合约会或安静聊天的店`
  ]
})

watch(keyword, (val) => {
  const trimmed = val.trim()
  if (!trimmed) {
    suggestList.value = []
    searchResults.value = []
    totalCount.value = 0
    hasSearched.value = false
    return
  }
  hasSearched.value = false
  searchResults.value = []
  totalCount.value = 0
  loadSuggestions(trimmed)
})

watch(currentTypeId, () => {
  loadAreaOptions()
})

function normalizeText(item) {
  if (typeof item === 'string') return item
  return item?.name || item?.label || item?.keyword || item?.title || item?.content || ''
}

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

function updateHistory(kw) {
  if (!kw) return
  const next = [kw, ...searchHistory.value.filter(item => item !== kw)].slice(0, 8)
  searchHistory.value = next
  uni.setStorageSync(HISTORY_KEY, next)
}

function loadHistory() {
  searchHistory.value = uni.getStorageSync(HISTORY_KEY) || []
}

async function resolveTypeLabel() {
  if (!currentTypeId.value || currentTypeLabel.value) return
  try {
    const types = await api.shop.getShopTypes()
    const current = (types || []).find(item => String(item.id) === String(currentTypeId.value))
    currentTypeLabel.value = current?.name || ''
  } catch (e) {
    currentTypeLabel.value = ''
  }
}

async function loadAreaOptions() {
  try {
    const res = await api.shop.getAreaList(currentTypeId.value ? { typeId: currentTypeId.value } : {})
    areaOptions.value = (res || []).map(item => normalizeText(item)).filter(Boolean)
  } catch (e) {
    areaOptions.value = []
  }
}

async function loadSuggestions(kw) {
  try {
    const res = await api.shop.getSearchSuggest(kw)
    suggestList.value = (res || [])
      .map(item => normalizeText(item))
      .filter(Boolean)
      .slice(0, 6)
  } catch (e) {
    suggestList.value = []
  }
}

function buildSearchParams(kw = keyword.value.trim()) {
  return {
    keyword: kw || undefined,
    typeId: currentTypeId.value || undefined,
    area: selectedArea.value || undefined,
    sortBy: sortBy.value,
    minPrice: minPrice.value === '' ? undefined : Number(minPrice.value),
    maxPrice: maxPrice.value === '' ? undefined : Number(maxPrice.value),
    pageNo: 1,
    pageSize: 20
  }
}

async function submitSearch(kw = keyword.value.trim(), options = {}) {
  const { saveHistory = true } = options
  if (!kw && !hasSearchIntent.value) return
  isLoading.value = true
  hasSearched.value = false
  try {
    const res = await api.shop.getShopList(buildSearchParams(kw))
    const list = res?.list || res?.records || []
    searchResults.value = list.map(normalizeShop)
    totalCount.value = res?.total || searchResults.value.length
    hasSearched.value = true
    suggestList.value = []
    if (saveHistory && kw) updateHistory(kw)
  } catch (e) {
    console.error('搜索失败', e)
    searchResults.value = []
    totalCount.value = 0
    hasSearched.value = true
  } finally {
    isLoading.value = false
  }
}

async function loadHotShops() {
  try {
    const res = await api.shop.getHotShops(8)
    hotShops.value = (res || [])
      .map(item => normalizeText(item))
      .filter(Boolean)
      .slice(0, 8)
  } catch (e) {
    hotShops.value = []
  }
}

function applySuggestion(text) {
  keyword.value = text
  suggestList.value = []
  submitSearch(text)
}

function clearKeyword() {
  keyword.value = ''
  suggestList.value = []
}

function clearTypeFilter() {
  currentTypeId.value = ''
  currentTypeLabel.value = ''
  loadAreaOptions()
  if (hasSearchIntent.value) {
    submitSearch(keyword.value.trim(), { saveHistory: false })
  } else {
    searchResults.value = []
    totalCount.value = 0
    hasSearched.value = false
  }
}

function applyPricePreset(item) {
  selectedPriceKey.value = item.key
  minPrice.value = item.min
  maxPrice.value = item.max
}

function resetFilters() {
  selectedArea.value = ''
  applyPricePreset(priceOptions[0])
  sortBy.value = 'score'
}

function applyFilters() {
  showFilterPanel.value = false
  submitSearch(keyword.value.trim(), { saveHistory: false })
}

function changeSort(value) {
  sortBy.value = value
  submitSearch(keyword.value.trim(), { saveHistory: false })
}

function clearHistory() {
  searchHistory.value = []
  uni.removeStorageSync(HISTORY_KEY)
}

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.switchTab({ url: '/pages/home/index' })
}

function goAiChat(prompt) {
  const nextPrompt = prompt || keyword.value.trim() || '帮我推荐附近值得去的店'
  chatStore.setDraftPrompt(nextPrompt)
  uni.switchTab({ url: '/pages/ai/chat' })
}

function goShopDetail(id) {
  uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
}

onLoad(async (options) => {
  loadHistory()
  await Promise.all([loadHotShops(), loadAreaOptions()])
  currentTypeId.value = options?.typeId || ''
  currentTypeLabel.value = options?.typeName ? decodeURIComponent(options.typeName) : ''
  if (currentTypeId.value) await resolveTypeLabel()
  if (options?.keyword) {
    keyword.value = decodeURIComponent(options.keyword)
    return
  }
  if (currentTypeId.value) submitSearch('', { saveHistory: false })
})

onUnload(() => {
  suggestList.value = []
})
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff 0%, #fafbff 100%);
}

.search-header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;

  .search-bar {
    flex: 1;
    height: 84rpx;
    background: #f5f6f8;
    border-radius: 42rpx;
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.7);

    .search-icon {
      width: 44rpx;
      height: 44rpx;
      margin-right: 12rpx;
    }

    .input {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }

    .clear-icon {
      width: 40rpx;
      height: 40rpx;
      margin-left: 12rpx;
    }
  }

  .cancel-btn {
    font-size: 32rpx;
    color: #666;
    margin-left: 30rpx;
  }
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 30rpx 10rpx;

  .type-chip {
    display: inline-flex;
    align-items: center;
    background: #fff2f2;
    color: #ff5a5f;
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
    border: 2rpx solid rgba(255, 90, 95, 0.12);

    .chip-icon {
      width: 28rpx;
      height: 28rpx;
      margin-right: 10rpx;
    }

    .chip-text {
      font-size: 24rpx;
      font-weight: 700;
    }
  }

  .chip-action {
    font-size: 24rpx;
    color: #999;
  }
}

.tool-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 0 30rpx 18rpx;
}

.sort-scroll {
  flex: 1;
  white-space: nowrap;
}

.sort-list {
  display: inline-flex;
  gap: 16rpx;
}

.sort-chip,
.filter-btn,
.chip-item.filter {
  background: #fff;
  border-radius: 30rpx;
  padding: 14rpx 24rpx;
  font-size: 24rpx;
  color: #666;
  box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.04);
}

.sort-chip.active,
.chip-item.filter.active {
  background: #fff0f0;
  color: #ff5a5f;
  font-weight: 700;
}

.filter-btn {
  display: flex;
  align-items: center;
  flex-shrink: 0;

  .filter-icon {
    width: 28rpx;
    height: 28rpx;
    margin-right: 10rpx;
  }
}

.panel-card {
  margin: 0 30rpx 20rpx;
  padding: 26rpx;
  background: #fff;
  border-radius: 28rpx;
  box-shadow: 0 16rpx 44rpx rgba(0, 0, 0, 0.05);
}

.panel-block {
  margin-bottom: 26rpx;
}

.panel-title {
  display: block;
  font-size: 26rpx;
  color: #333;
  font-weight: 700;
  margin-bottom: 18rpx;
}

.panel-actions {
  display: flex;
  gap: 16rpx;

  .action {
    flex: 1;
    text-align: center;
    padding: 20rpx 0;
    border-radius: 24rpx;
    font-size: 26rpx;
    font-weight: 700;
  }

  .ghost {
    background: #f5f6f8;
    color: #666;
  }

  .primary {
    background: linear-gradient(135deg, #ff6b6b, #ff5a5f);
    color: #fff;
  }
}

.ai-intent-card {
  margin: 20rpx 30rpx 24rpx;
  background: rgba(108, 92, 231, 0.05);
  border: 2rpx solid rgba(108, 92, 231, 0.15);
  border-radius: 24rpx;
  padding: 30rpx;

  .hd {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .ai-icon {
      width: 44rpx;
      height: 44rpx;
      background: #6c5ce7;
      border-radius: 50%;
      padding: 6rpx;
      margin-right: 16rpx;
    }

    .tit {
      font-size: 30rpx;
      font-weight: 700;
      color: #6c5ce7;
    }
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .tag {
      background: #fff;
      border: 2rpx solid #e0d4ff;
      color: #6c5ce7;
      font-size: 24rpx;
      padding: 10rpx 20rpx;
      border-radius: 14rpx;
      font-weight: 700;
    }
  }
}

.suggest-panel {
  margin: 0 30rpx 20rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 12rpx 0;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.04);

  .suggest-title {
    font-size: 24rpx;
    color: #999;
    padding: 16rpx 24rpx 8rpx;
  }

  .suggest-item {
    display: flex;
    align-items: center;
    padding: 20rpx 24rpx;

    .suggest-icon {
      width: 28rpx;
      height: 28rpx;
      margin-right: 16rpx;
    }

    .suggest-text {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.ai-plan-card {
  margin: 0 30rpx 24rpx;
  background: linear-gradient(135deg, rgba(108, 92, 231, 0.08), rgba(255, 90, 95, 0.08));
  border: 2rpx solid rgba(108, 92, 231, 0.12);
  border-radius: 24rpx;
  padding: 28rpx;

  .plan-head {
    margin-bottom: 18rpx;
  }

  .plan-title {
    display: block;
    font-size: 28rpx;
    font-weight: 700;
    color: #333;
    margin-bottom: 8rpx;
  }

  .plan-subtitle {
    font-size: 22rpx;
    color: #8f93a2;
  }

  .plan-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }

  .plan-chip {
    background: #fff;
    color: #6c5ce7;
    font-size: 24rpx;
    padding: 14rpx 20rpx;
    border-radius: 16rpx;
    font-weight: 700;
  }
}

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  font-size: 26rpx;
  color: #999;
}

.result-sub {
  font-size: 22rpx;
  color: #b1b3bf;
}

.result-list {
  padding: 0 30rpx 30rpx;

  .s-card {
    display: flex;
    margin-bottom: 28rpx;
    background: #fff;
    border-radius: 28rpx;
    padding: 20rpx;
    box-shadow: 0 14rpx 40rpx rgba(0, 0, 0, 0.04);

    .cover {
      width: 180rpx;
      height: 180rpx;
      background: #eee;
      border-radius: 20rpx;
      margin-right: 24rpx;
      flex-shrink: 0;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 8rpx 0;

      .top {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .name {
          flex: 1;
          font-size: 32rpx;
          font-weight: 700;
          color: #333;
          line-height: 1.4;
          margin-right: 16rpx;
        }

        .score-wrap {
          display: flex;
          align-items: center;

          .star {
            width: 28rpx;
            height: 28rpx;
            margin-right: 4rpx;
          }

          .score {
            color: #ff8c42;
            font-size: 28rpx;
            font-weight: 700;
          }
        }
      }

      .label {
        font-size: 24rpx;
        color: #666;
      }

      .bot {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 16rpx;

        .dist {
          flex: 1;
          font-size: 24rpx;
          color: #999;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .price {
          font-size: 24rpx;
          color: #ff5a5f;
          font-weight: 700;
        }
      }
    }
  }
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 50rpx;

  .empty-icon {
    width: 140rpx;
    height: 140rpx;
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

.ai-try-bar {
  background: #f8f9fa;
  margin-top: 20rpx;
  padding: 28rpx 30rpx;
  border-radius: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .txt {
    font-size: 26rpx;
    color: #666;
  }

  .btn {
    border: 2rpx solid #6c5ce7;
    color: #6c5ce7;
    font-size: 24rpx;
    padding: 12rpx 28rpx;
    border-radius: 32rpx;
    font-weight: 700;
  }
}

.discover-section {
  padding: 10rpx 30rpx 40rpx;
}

.history-block,
.hot-search {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 16rpx 44rpx rgba(0, 0, 0, 0.04);
}

.history-block {
  margin-bottom: 24rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
}

.section-action,
.section-subtitle {
  font-size: 24rpx;
  color: #999;
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
}

.chip-item {
  font-size: 26rpx;
  padding: 14rpx 26rpx;
  border-radius: 36rpx;

  &.history {
    background: #f5f6f8;
    color: #555;
  }

  &.hot {
    background: #fff0f0;
    color: #ff5a5f;
    font-weight: 700;
  }
}
</style>
