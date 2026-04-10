import { defineStore } from 'pinia';

const CHAT_STORAGE_KEY = 'ai_chat_session';
const CHAT_DRAFT_KEY = 'ai_chat_draft';

function createWelcomeMessage() {
  return {
    id: `assistant-${Date.now()}`,
    role: 'assistant',
    content: '你好，我是 AI 探店助手。告诉我预算、场景和口味，我来帮你把推荐缩到更适合你的几家店。',
    cards: [],
    time: Date.now()
  };
}

function normalizeCards(shops = []) {
  return (shops || []).map((shop) => ({
    id: shop.shopId || shop.id,
    name: shop.name || '精选商户',
    score: shop.score || '0',
    desc: shop.reason || shop.address || '查看详情',
    price: shop.avgPrice ? `¥${Math.round(shop.avgPrice / 100)}/人` : '',
    distance: shop.distance ? `${Math.round(shop.distance)}m` : '',
    image: shop.image || ''
  }));
}

function normalizeMessages(messages = [], shops = []) {
  const cards = normalizeCards(shops);
  return (messages || []).map((item, index, list) => ({
    id: item.messageId || `${item.role}-${item.createTime || index}`,
    role: item.role || 'assistant',
    content: item.content || '',
    cards: item.role === 'assistant' && index === list.length - 1 ? cards : [],
    time: item.createTime || Date.now()
  }));
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessionId: '',
    sessionTitle: '',
    messages: [createWelcomeMessage()],
    location: null,
    draftPrompt: '',
    initialized: false
  }),
  actions: {
    bootstrap() {
      if (this.initialized) return;
      const cachedSession = uni.getStorageSync(CHAT_STORAGE_KEY);
      const cachedDraft = uni.getStorageSync(CHAT_DRAFT_KEY);

      if (cachedSession?.messages?.length) {
        this.sessionId = cachedSession.sessionId || '';
        this.sessionTitle = cachedSession.sessionTitle || '';
        this.messages = cachedSession.messages;
        this.location = cachedSession.location || null;
      }

      if (cachedDraft) {
        this.draftPrompt = cachedDraft;
      }

      if (!this.messages.length) {
        this.messages = [createWelcomeMessage()];
      }

      this.initialized = true;
    },
    persist() {
      uni.setStorageSync(CHAT_STORAGE_KEY, {
        sessionId: this.sessionId,
        sessionTitle: this.sessionTitle,
        messages: this.messages,
        location: this.location
      });
      if (this.draftPrompt) {
        uni.setStorageSync(CHAT_DRAFT_KEY, this.draftPrompt);
      } else {
        uni.removeStorageSync(CHAT_DRAFT_KEY);
      }
    },
    setDraftPrompt(prompt = '') {
      this.draftPrompt = prompt.trim();
      this.persist();
    },
    consumeDraftPrompt() {
      const prompt = this.draftPrompt;
      this.draftPrompt = '';
      this.persist();
      return prompt;
    },
    setLocation(location) {
      this.location = location || null;
      this.persist();
    },
    appendMessage(message) {
      this.messages.push({
        id: message.id || `${message.role}-${Date.now()}`,
        cards: [],
        time: Date.now(),
        ...message
      });
      this.persist();
    },
    replaceMessage(id, payload) {
      const index = this.messages.findIndex((item) => item.id === id);
      if (index === -1) return;
      this.messages[index] = {
        ...this.messages[index],
        ...payload
      };
      this.persist();
    },
    hydrateSession(detail) {
      this.sessionId = detail?.sessionId || this.sessionId;
      this.sessionTitle = detail?.title || this.sessionTitle;
      const nextMessages = normalizeMessages(detail?.messages, detail?.shops);
      this.messages = nextMessages.length ? nextMessages : [createWelcomeMessage()];
      this.persist();
    },
    resetSession() {
      this.sessionId = '';
      this.sessionTitle = '';
      this.messages = [createWelcomeMessage()];
      this.persist();
    }
  }
});
