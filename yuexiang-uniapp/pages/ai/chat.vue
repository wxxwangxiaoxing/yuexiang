<template>
  <view class="chat-container">
    <view class="nav-glass-bg">
      <view class="status-bar"></view>
      <view class="nav-bar">
        <AppIcon class="back-icon" name="chevron-left" color="#1a1a1a" @click="goBack" />
        <view class="title-wrap">
          <AppIcon class="bot-icon" name="robot-excited-outline" color="#6C5CE7" />
          <text class="title">AI 探店助手</text>
        </view>
        <view class="nav-actions">
          <text class="action-link" v-if="sessionId" @click="handleReset">新对话</text>
        </view>
      </view>
    </view>
    
    <scroll-view class="chat-list" scroll-y :scroll-into-view="scrollInto" scroll-with-animation>
      <view class="scroll-pad"></view>
      
      <view class="message-item fade-up" v-for="(msg, index) in messages" :key="msg.id" :id="'msg-' + msg.id" :style="{ animationDelay: (index * 0.1) + 's' }">
        <view class="ai-message" v-if="msg.role === 'assistant'">
          <view class="avatar-box">
            <AppIcon class="avatar" name="robot-outline" color="#fff" />
          </view>
          <view class="bubble-wrap">
            <view class="bubble" :class="{ streaming: msg.streaming }">{{ msg.content || 'AI 正在组织回答...' }}</view>
            <view class="recommend-cards" v-if="msg.cards && msg.cards.length">
              <view class="shop-card" v-for="shop in msg.cards" :key="shop.id">
                <view class="s-top">
                  <text class="shop-name">{{ shop.name }}</text>
                  <view class="s-score"><AppIcon class="star" name="star" color="#FF8C42" />{{ shop.score }}</view>
                </view>
                <text class="shop-desc">{{ shop.desc }}</text>
                <view class="btn shadow-btn" @click="goShopDetail(shop.id)">查看详情</view>
              </view>
            </view>
            <view class="hot-questions glass-panel" v-if="index === 0">
              <text class="hq-title">我可以帮你找店、做攻略、补充筛选条件：</text>
              <view class="chips">
                <text class="chip" @click="sendText('附近有啥好吃的')">附近有啥好吃的</text>
                <text class="chip" @click="sendText('人均50聚餐推荐')">人均50聚餐推荐</text>
                <text class="chip" @click="sendText('安静约会餐厅')">安静约会餐厅</text>
                <text class="chip" @click="sendText('今日秒杀推荐')">今日秒杀推荐</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="user-message" v-else>
          <view class="bubble">{{ msg.content }}</view>
          <view class="avatar-box shadow-avatar">
            <image v-if="userAvatar" class="avatar" :src="userAvatar" mode="aspectFill"></image>
            <text v-else class="avatar-text">我</text>
          </view>
        </view>
      </view>
      
      <view class="padding-bottom"></view>
    </scroll-view>

    <view class="glass-input-bar">
      <view class="input-wrap">
        <input class="input" v-model="inputText" placeholder="想吃什么，问我吧..." placeholder-style="color:#a0a0a0;" @confirm="onSend" />
        <view class="mic-btn">
          <AppIcon class="icon" name="microphone" color="#6C5CE7" />
        </view>
      </view>
      <view class="send-btn" :class="{ 'active': inputText.length > 0 }" @click="onSend">
        <AppIcon class="s-ic" name="send-variant" :color="inputText.length > 0 ? '#fff' : '#ccc'" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'
import { useChatStore } from '../../store/chat'
import { useUserStore } from '../../store/user'

const scrollInto = ref('')
const inputText = ref('')
const isSending = ref(false)
let currentStreamTask = null
const chatStore = useChatStore()
const userStore = useUserStore()
const { messages, sessionId } = storeToRefs(chatStore)
const userAvatar = computed(() => userStore.userInfo?.avatar || '')

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.switchTab({ url: '/pages/home/index' })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messages.value.length) {
      scrollInto.value = 'msg-' + messages.value[messages.value.length - 1].id
    }
  })
}

const goShopDetail = (id) => {
  uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
}

const sendText = (text) => {
  inputText.value = text
  onSend()
}

async function ensureLocation() {
  if (chatStore.location) return chatStore.location
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        chatStore.setLocation(res)
        resolve(res)
      },
      fail: () => resolve(null)
    })
  })
}

async function restoreSession() {
  if (!sessionId.value) return
  try {
    const detail = await api.ai.getSessionDetail(sessionId.value)
    if (detail) {
      chatStore.hydrateSession(detail)
      scrollToBottom()
    }
  } catch (error) {
    console.error('恢复 AI 会话失败', error)
  }
}

async function consumeDraftPrompt() {
  const draftPrompt = chatStore.consumeDraftPrompt()
  if (!draftPrompt) return
  inputText.value = draftPrompt
  await onSend()
}

function buildQuestion(text, location) {
  if (location) {
    return text
  }
  return text.replace('结合当前位置', '结合当前需求').replace('当前位置', '当前需求')
}

async function finalizeStreamMessage(loadingId) {
  chatStore.replaceMessage(loadingId, { streaming: false })
  await restoreSession()
  const currentMessage = messages.value.find((item) => item.id === loadingId)
  if (currentMessage?.content) {
    return
  }
  chatStore.replaceMessage(loadingId, {
    content: '我已经收到你的需求了。当前还没有拿到定位信息，你可以直接告诉我商圈、预算或场景，我会继续帮你筛选。',
    streaming: false,
    cards: []
  })
}

async function handleReset() {
  currentStreamTask?.abort?.()
  currentStreamTask = null
  if (sessionId.value) {
    try {
      await api.ai.deleteSession(sessionId.value)
    } catch (error) {
      console.error('删除 AI 会话失败', error)
    }
  }
  chatStore.resetSession()
  inputText.value = ''
  scrollToBottom()
}

const onSend = async () => {
  if (!inputText.value.trim() || isSending.value) return
  const text = inputText.value.trim()
  inputText.value = ''

  chatStore.appendMessage({ role: 'user', content: text })
  scrollToBottom()

  isSending.value = true
  const loadingId = `assistant-loading-${Date.now()}`
  let streamSettled = false
  chatStore.appendMessage({ id: loadingId, role: 'assistant', content: '', cards: [], streaming: true })
  scrollToBottom()

  try {
    const location = await ensureLocation()
    currentStreamTask = api.ai.chatStream({
      sessionId: sessionId.value || '',
      question: buildQuestion(text, location),
      lng: location?.longitude,
      lat: location?.latitude
    }, {
      onSession: (payload) => {
        chatStore.sessionId = payload?.sessionId || chatStore.sessionId
        chatStore.sessionTitle = payload?.title || chatStore.sessionTitle
        chatStore.persist()
      },
      onDelta: (payload) => {
        const previousContent = messages.value.find((item) => item.id === loadingId)?.content || ''
        chatStore.replaceMessage(loadingId, {
          content: `${previousContent}${payload?.content || ''}`,
          streaming: true
        })
        scrollToBottom()
      },
      onDone: async (payload) => {
        if (streamSettled) return
        streamSettled = true
        if (payload?.sessionId) {
          chatStore.sessionId = payload.sessionId
          chatStore.persist()
        }
        await finalizeStreamMessage(loadingId)
      },
      onError: () => {
        if (streamSettled) return
        streamSettled = true
        chatStore.replaceMessage(loadingId, {
          content: '抱歉，AI 服务暂时不可用，请稍后再试，或者换一个更具体的需求试试。',
          cards: [],
          streaming: false
        })
      }
    })
    await currentStreamTask.done
    if (!streamSettled) {
      streamSettled = true
      await finalizeStreamMessage(loadingId)
    }
  } catch (e) {
    chatStore.replaceMessage(loadingId, {
      content: '抱歉，AI 服务暂时不可用，请稍后再试，或者换一个更具体的需求试试。',
      cards: [],
      streaming: false
    })
  } finally {
    currentStreamTask = null
    isSending.value = false
    scrollToBottom()
  }
}

onLoad(async () => {
  chatStore.bootstrap()
  await restoreSession()
  await consumeDraftPrompt()
  scrollToBottom()
})

onShow(async () => {
  if (!chatStore.initialized) {
    chatStore.bootstrap()
  }
  await consumeDraftPrompt()
})

onUnload(() => {
  currentStreamTask?.abort?.()
  currentStreamTask = null
})
</script>

<style lang="scss" scoped>
.chat-container { height: 100vh; display: flex; flex-direction: column; background-color: #F4F6F9; position: relative;}

.nav-glass-bg {
  position: absolute; top: 0; left: 0; right: 0; z-index: 100;
  background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(30px);
  border-bottom: 2rpx solid rgba(255,255,255,0.6); box-shadow: 0 4rpx 30rpx rgba(0,0,0,0.02);
  .status-bar { height: var(--status-bar-height, 44px); }
  .nav-bar {
    display: flex; justify-content: space-between; align-items: center; padding: 16rpx 30rpx;
    .back-icon { width: 64rpx; height: 64rpx; padding: 10rpx; margin-left: -10rpx;}
    .title-wrap {
      display: flex; align-items: center; background: rgba(108, 92, 231, 0.1); padding: 8rpx 32rpx; border-radius: 30rpx; border: 2rpx solid rgba(108, 92, 231, 0.15);
      .bot-icon { width: 36rpx; height: 36rpx; margin-right: 10rpx;}
      .title { font-size: 30rpx; font-weight: 800; color: #6C5CE7; letter-spacing: 1rpx;}
    }
    .nav-actions { min-width: 88rpx; display: flex; justify-content: flex-end; }
    .action-link { font-size: 24rpx; color: #6C5CE7; font-weight: 700; }
  }
}

.chat-list { flex: 1; height: 0; padding: 0 30rpx; box-sizing: border-box; }
.scroll-pad { height: calc(var(--status-bar-height, 44px) + 120rpx); }
.padding-bottom { height: 300rpx; }

.fade-up { animation: fadeUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) both; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(30rpx); } to { opacity: 1; transform: translateY(0); } }
@keyframes blink { 50% { opacity: 0; } }

.message-item { margin-bottom: 50rpx; display: flex; width: 100%; }

.ai-message {
  display: flex; justify-content: flex-start; align-items: flex-start; max-width: 90%;
  .avatar-box {
    width: 80rpx; height: 80rpx; border-radius: 50%; margin-right: 24rpx;
    background: linear-gradient(135deg, #6C5CE7, #A29BFE); flex-shrink: 0;
    display: flex; justify-content: center; align-items: center; box-shadow: 0 8rpx 20rpx rgba(108, 92, 231, 0.3);
    .avatar { width: 44rpx; height: 44rpx; }
  }
  .bubble-wrap {
    display: flex; flex-direction: column;
    .bubble {
      background-color: #fff; padding: 28rpx 36rpx; border-radius: 8rpx 36rpx 36rpx 36rpx;
      font-size: 30rpx; color: #1a1a1a; line-height: 1.6; font-weight: 500;
      box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.05); border: 2rpx solid rgba(255,255,255,1);
    }
    .bubble.streaming::after {
      content: ' |';
      color: #6C5CE7;
      animation: blink 1s step-end infinite;
    }
    .hot-questions.glass-panel {
      margin-top: 30rpx; background: rgba(255,255,255,0.7); backdrop-filter: blur(20px);
      padding: 30rpx; border-radius: 32rpx; border: 2rpx solid rgba(255,255,255,0.8);
      box-shadow: 0 12rpx 30rpx rgba(0,0,0,0.03);
      .hq-title { font-size: 26rpx; font-weight: 800; color: #6C5CE7; margin-bottom: 20rpx; display: block; }
      .chips {
        display: flex; flex-wrap: wrap; gap: 16rpx;
        .chip { background: #fff; color: #555; font-size: 24rpx; padding: 14rpx 28rpx; border-radius: 30rpx; font-weight: 600; box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.02); transition: all 0.2s; border: 2rpx solid #F0EDFF;}
        .chip:active { transform: scale(0.95); background: #F0EDFF; color: #6C5CE7;}
      }
    }
    .recommend-cards {
      margin-top: 24rpx;
      .shop-card {
        background-color: #fff; border-radius: 28rpx; padding: 30rpx; margin-bottom: 20rpx; border: 2rpx solid #F0EDFF; box-shadow: 0 10rpx 40rpx rgba(108, 92, 231, 0.05);
        .s-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; .shop-name { font-size: 34rpx; font-weight: 900; color: #1a1a1a; letter-spacing: 1rpx;} .s-score { display: flex; align-items: center; color: #FF8C42; font-weight: 900; font-size: 28rpx; .star {width: 28rpx; height: 28rpx; margin-right: 4rpx;}}}
        .shop-desc { font-size: 26rpx; color: #666; line-height: 1.5; display: block; margin-bottom: 24rpx; white-space: pre-wrap; font-weight: 500;}
        .btn.shadow-btn { text-align: center; background: linear-gradient(135deg, #6C5CE7, #8e44ad); color: #fff; font-size: 26rpx; padding: 16rpx 0; border-radius: 20rpx; font-weight: bold; box-shadow: 0 6rpx 20rpx rgba(108, 92, 231, 0.3); transition: transform 0.2s; &:active{ transform: scale(0.98);}}
      }
    }
  }
}

.user-message {
  display: flex; justify-content: flex-end; align-items: flex-start; width: 100%;
  .bubble {
    background: linear-gradient(135deg, #FF6B6B, #FF4757); color: #fff;
    padding: 28rpx 36rpx; border-radius: 36rpx 8rpx 36rpx 36rpx;
    font-size: 30rpx; line-height: 1.6; max-width: 75%; font-weight: 500;
    box-shadow: 0 12rpx 36rpx rgba(255, 90, 95, 0.35); border: 2rpx solid rgba(255,255,255,0.2);
  }
  .avatar-box.shadow-avatar {
    width: 80rpx; height: 80rpx; border-radius: 50%; margin-left: 24rpx; flex-shrink: 0;
    box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.1); border: 4rpx solid #fff; box-sizing: border-box; overflow: hidden;
    .avatar { width: 100%; height: 100%; }
    .avatar-text { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #666; background: #fff; font-weight: 700; }
  }
}

.glass-input-bar {
  position: fixed; bottom: var(--window-bottom, 0); left: 0; right: 0; display: flex; align-items: center; padding: 20rpx 30rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255,255,255,0.85); backdrop-filter: blur(30px);
  border-top: 2rpx solid rgba(255,255,255,0.5); box-shadow: 0 -8rpx 40rpx rgba(0,0,0,0.05); z-index: 200;
  .input-wrap {
    flex: 1; background: #fff; height: 96rpx; border-radius: 48rpx; padding: 0 12rpx 0 40rpx; display: flex; align-items: center; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.04); border: 2rpx solid #F0F0F0; margin-right: 24rpx;
    .input { flex: 1; font-size: 30rpx; color: #1a1a1a; height: 100%; font-weight: 500;}
    .mic-btn { width: 76rpx; height: 76rpx; background: #F0EDFF; border-radius: 50%; display: flex; justify-content: center; align-items: center; margin-left: 20rpx; .icon { width: 44rpx; height: 44rpx; }}
  }
  .send-btn {
    width: 96rpx; height: 96rpx; background: #F0F0F0; border-radius: 50%; display: flex; justify-content: center; align-items: center; transition: all 0.3s;
    &.active { background: linear-gradient(135deg, #FF6B6B, #FF4757); box-shadow: 0 8rpx 24rpx rgba(255, 71, 87, 0.4); transform: scale(1.05);}
    .s-ic { width: 48rpx; height: 48rpx; margin-left: 6rpx; }
  }
}
</style>
