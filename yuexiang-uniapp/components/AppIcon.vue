<template>
  <text class="app-icon" :style="iconStyle">{{ glyph }}</text>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    default: ''
  },
  color: {
    type: String,
    default: 'currentColor'
  },
  size: {
    type: String,
    default: ''
  }
})

const glyphMap = {
  'account-multiple-plus-outline': '👥',
  'arrow-top-left': '↖',
  'auto-fix': '✨',
  'baby-carriage': '👶',
  'bed-outline': '🛏',
  'bell-outline': '🔔',
  'bell-ring-outline': '🔔',
  'broom': '🧹',
  'calendar-check-outline': '📅',
  'camera-plus-outline': '📷',
  'cart-outline': '🛒',
  'cellphone': '📱',
  'check-bold': '✓',
  'chevron-down': '⌄',
  'chevron-left': '‹',
  'chevron-right': '›',
  'clock-outline': '◷',
  'close': '×',
  'close-circle': '⊗',
  'cog-outline': '⚙',
  'comment-outline': '💬',
  'comment-processing-outline': '💬',
  'content-copy': '⧉',
  'crown-outline': '♛',
  'dots-horizontal': '⋯',
  'dumbbell': '🏋',
  'email-outline': '✉',
  'face-woman-shimmer-outline': '美',
  'file-document-edit-outline': '📝',
  'fire': '🔥',
  'food-drumstick-outline': '🍗',
  'gamepad-variant-outline': '🎮',
  'heart': '♥',
  'heart-outline': '♡',
  'inbox-outline': '✉',
  'lightbulb-on-outline': '💡',
  'magnify': '⌕',
  'magic-staff': '✦',
  'map-marker': '⌖',
  'map-marker-radius': '◎',
  'message-badge-outline': '✉',
  'microphone': '🎤',
  'microphone-variant': '🎤',
  'notebook-heart-outline': '❤',
  'pencil-outline': '✎',
  'pencil-plus-outline': '✎',
  'phone': '☎',
  'plus': '+',
  'qqchat': 'Q',
  'refresh': '↻',
  'robot-excited-outline': 'AI',
  'robot-outline': 'AI',
  'send-variant': '➤',
  'shape-outline': '⬢',
  'share-variant-outline': '↗',
  'shield-check-outline': '🛡',
  'shoe-print': '👣',
  'shopping-outline': '🛍',
  'star': '★',
  'star-circle': '⭑',
  'star-face': '★',
  'star-outline': '☆',
  'store-outline': '店',
  'store-search-outline': '店',
  'tag-multiple-outline': '🏷',
  'thumb-up': '👍',
  'thumb-up-outline': '👍',
  'ticket-percent-outline': '券',
  'tune': '☰',
  'wallet-bifold-outline': '¥',
  'wechat': '微'
}

const glyph = computed(() => glyphMap[props.name] || '?')

const iconStyle = computed(() => ({
  color: props.color,
  fontSize: props.size || '1em',
  lineHeight: 1,
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontWeight: glyph.value.length > 1 ? 700 : 600,
  textAlign: 'center'
}))
</script>

<style scoped>
.app-icon {
  width: 1em;
  height: 1em;
}
</style>
