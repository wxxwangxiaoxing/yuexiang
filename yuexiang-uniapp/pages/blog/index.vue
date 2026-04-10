<template>
  <view class="blog-container">
    <view class="header-bg">
      <view class="status-bar"></view>
      <view class="nav-glass">
        <view class="tabs">
          <view class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
            <text class="txt">关注</text>
            <view class="line" v-if="currentTab === 0"></view>
          </view>
          <view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">
            <text class="txt">推荐</text>
            <view class="line" v-if="currentTab === 1"></view>
          </view>
          <view class="tab-item" :class="{ active: currentTab === 2 }" @click="switchTab(2)">
            <text class="txt">附近</text>
            <view class="line" v-if="currentTab === 2"></view>
          </view>
        </view>
        <view class="search-btn" @click="goSearch">
          <AppIcon class="search-icon" name="magnify" color="#1a1a1a" />
        </view>
      </view>
    </view>
    
    <scroll-view class="waterfall-scroll" scroll-y enable-back-to-top>
      <view class="scroll-pad"></view>
      <view class="waterfall">
        <view class="col left-col">
          <view class="note-card fade-up" v-for="(note, index) in leftList" :key="note.id" :style="{ animationDelay: (index * 0.1) + 's' }" @click="goDetail(note.id)">
            <view class="img-box">
              <image class="cover" :src="note.cover" mode="widthFix"></image>
            </view>
            <view class="content">
              <text class="title">{{ note.title }}</text>
              <view class="bot">
                <view class="author">
                  <image class="avatar" :src="note.avatar" mode="aspectFill"></image>
                  <text class="name">{{ note.author }}</text>
                </view>
                <view class="like">
                  <AppIcon class="l-icon" name="heart-outline" color="#888" />
                  <text class="l-num">{{ note.likes }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <view class="col right-col">
          <view class="note-card fade-up" v-for="(note, index) in rightList" :key="note.id" :style="{ animationDelay: (index * 0.1 + 0.05) + 's' }" @click="goDetail(note.id)">
            <view class="img-box">
              <image class="cover" :src="note.cover" mode="widthFix"></image>
            </view>
            <view class="content">
              <text class="title">{{ note.title }}</text>
              <view class="bot">
                <view class="author">
                  <image class="avatar" :src="note.avatar" mode="aspectFill"></image>
                  <text class="name">{{ note.author }}</text>
                </view>
                <view class="like">
                  <AppIcon class="l-icon" name="heart-outline" color="#888" />
                  <text class="l-num">{{ note.likes }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="bottom-holder"></view>
    </scroll-view>

    <!-- 悬浮发布按钮: 带呼吸光晕效果 -->
    <view class="fab-wrapper">
      <view class="fab-glow"></view>
      <view class="fab-btn" @click="goPublish">
        <AppIcon class="plus-icon" name="plus" color="#fff" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import blogApi from '../../api/blog'
import AppIcon from '../../components/AppIcon.vue'

const currentTab = ref(1)
const leftList = ref([])
const rightList = ref([])

const splitToColumns = (items) => {
  const left = []
  const right = []
  items.forEach((it, idx) => (idx % 2 === 0 ? left : right).push(it))
  leftList.value = left
  rightList.value = right
}

const loadBlogs = async () => {
  const res = await blogApi.getBlogList({ pageNo: 1, pageSize: 10 })
  const list = res?.list || []
  // 后端返回 BlogListItemVO，字段已对齐：{id,title,cover,author,avatar,likes}
  splitToColumns(list)
}

const switchTab = (idx) => {
  currentTab.value = idx
  // 当前后端先不区分 tab 语义，这里直接刷新同一份列表
  loadBlogs().catch(() => {})
}

const goSearch = () => {
  uni.navigateTo({ url: '/pages/search/index' })
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/blog/detail?id=${id}` })
}

const goPublish = () => {
  uni.navigateTo({ url: '/pages/blog/publish' })
}

onMounted(() => {
  loadBlogs().catch(() => {})
})
</script>

<style lang="scss" scoped>
.blog-container { height: 100vh; display: flex; flex-direction: column; background-color: #F4F6F9; position: relative;}

/* 顶部毛玻璃导航 */
.header-bg {
  position: absolute; top: 0; left: 0; right: 0; z-index: 100;
  .status-bar { height: var(--status-bar-height, 44px); background: rgba(255,255,255,0.85); backdrop-filter: blur(30px);}
  .nav-glass {
    height: 100rpx; background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(30px);
    display: flex; justify-content: space-between; align-items: center; padding: 0 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03); border-bottom: 2rpx solid rgba(255,255,255,0.6);
    
    .tabs {
      display: flex; height: 100%;
      .tab-item {
        position: relative; padding: 0 30rpx; display: flex; align-items: center; justify-content: center; height: 100%;
        .txt { font-size: 32rpx; color: #777; font-weight: 600; transition: all 0.3s;}
        &.active .txt { font-size: 36rpx; font-weight: 900; color: #1a1a1a; letter-spacing: 1rpx; }
        .line { position: absolute; bottom: 8rpx; width: 40rpx; height: 8rpx; background: linear-gradient(90deg, #FF6B6B, #FF4757); border-radius: 4rpx; box-shadow: 0 4rpx 8rpx rgba(255, 71, 87, 0.4);}
      }
    }
    
    .search-btn {
      width: 72rpx; height: 72rpx; background: #F5F6F8; border-radius: 50%; display: flex; justify-content: center; align-items: center; box-shadow: inset 0 2rpx 0 rgba(255,255,255,1); transition: transform 0.2s; border: 2rpx solid #EAEAEA;
      &:active { transform: scale(0.9); }
      .search-icon { width: 44rpx; height: 44rpx; }
    }
  }
}

.waterfall-scroll { flex: 1; height: 0; box-sizing: border-box; }
.scroll-pad { height: calc(var(--status-bar-height, 44px) + 120rpx); }

/* 瀑布流 */
.waterfall {
  display: flex; padding: 0 24rpx; align-items: flex-start;
  .col {
    flex: 1; margin: 0 10rpx;
    
    .note-card {
      background: #fff; border-radius: 36rpx; margin-bottom: 30rpx; overflow: hidden; box-shadow: 0 16rpx 40rpx rgba(0,0,0,0.06); border: 2rpx solid rgba(0,0,0,0.01); transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s;
      &:active { transform: translateY(4rpx) scale(0.98); box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.03); }
      
      .img-box {
        width: 100%; border-radius: 36rpx 36rpx 20rpx 20rpx; overflow: hidden;
        .cover { width: 100%; display: block; background: #eee; }
      }
      
      .content {
        padding: 24rpx;
        .title { font-size: 30rpx; font-weight: 900; color: #1a1a1a; display: block; margin-bottom: 24rpx; line-height: 1.4; letter-spacing: 1rpx;}
        .bot {
          display: flex; justify-content: space-between; align-items: center;
          .author { display: flex; align-items: center; .avatar { width: 44rpx; height: 44rpx; border-radius: 50%; margin-right: 12rpx; border: 2rpx solid #f0f0f0; } .name { font-size: 24rpx; color: #666; font-weight: 600; font-family: -apple-system, sans-serif;} }
          .like { display: flex; align-items: center; .l-icon { width: 32rpx; height: 32rpx; margin-right: 6rpx; } .l-num { font-size: 24rpx; color: #888; font-weight: bold;} }
        }
      }
    }
  }
}

.fade-up { animation: fadeUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(60rpx); } to { opacity: 1; transform: translateY(0); } }

.bottom-holder { height: env(safe-area-inset-bottom); margin-bottom: 40rpx; }

/* 炫光悬浮按钮 */
.fab-wrapper {
  position: fixed; right: 40rpx; bottom: calc(var(--window-bottom, 0) + 40rpx + env(safe-area-inset-bottom)); z-index: 200; width: 110rpx; height: 110rpx; display: flex; justify-content: center; align-items: center;
  
  .fab-glow {
    position: absolute; width: 110%; height: 110%; background: linear-gradient(135deg, #FF6B6B, #FF4757); border-radius: 50%; filter: blur(16rpx); opacity: 0.6; animation: breath 2s infinite alternate ease-in-out;
  }
  
  .fab-btn {
    position: relative; z-index: 1; width: 110rpx; height: 110rpx; border-radius: 50%; display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #FF6B6B, #FF4757); box-shadow: 0 10rpx 30rpx rgba(255, 71, 87, 0.4); transition: transform 0.2s; border: 4rpx solid rgba(255,255,255,0.3);
    &:active { transform: scale(0.9); }
    .plus-icon { width: 64rpx; height: 64rpx; filter: drop-shadow(0 4rpx 6rpx rgba(0,0,0,0.2));}
  }
}

@keyframes breath {
  0% { transform: scale(0.9); opacity: 0.5; }
  100% { transform: scale(1.1); opacity: 0.8; }
}
</style>
