<template>
  <view class="edit-page">
    <view class="hero-card">
      <view class="hero-left">
        <image class="avatar" :src="form.avatar || avatarPresets[0]" mode="aspectFill"></image>
        <view class="avatar-text">
          <text class="hero-title">编辑个人资料</text>
          <text class="hero-sub">完善昵称、头像、城市和个人介绍，让主页更完整</text>
        </view>
      </view>
      <view class="save-btn" :class="{ disabled: saving }" @click="saveProfile">
        {{ saving ? '保存中' : '保存' }}
      </view>
    </view>

    <LoadingSkeleton v-if="loading" variant="profile" :count="5" />

    <template v-else>
      <view class="section-card">
        <text class="section-title">头像设置</text>
        <view class="avatar-grid">
          <image
            class="preset-avatar"
            :class="{ active: form.avatar === item }"
            v-for="item in avatarPresets"
            :key="item"
            :src="item"
            mode="aspectFill"
            @click="form.avatar = item"
          ></image>
        </view>
        <view class="form-item">
          <text class="label">头像地址</text>
          <input class="input" v-model="form.avatar" placeholder="可粘贴图片链接作为头像" />
        </view>
      </view>

      <view class="section-card">
        <text class="section-title">基础信息</text>
        <view class="form-item">
          <text class="label">昵称</text>
          <input class="input" v-model="form.nickName" maxlength="20" placeholder="请输入昵称" />
        </view>
        <view class="form-item">
          <text class="label">性别</text>
          <view class="gender-row">
            <view
              class="gender-chip"
              :class="{ active: Number(form.gender) === item.value }"
              v-for="item in genderOptions"
              :key="item.value"
              @click="form.gender = item.value"
            >
              {{ item.label }}
            </view>
          </view>
        </view>
        <view class="form-item">
          <text class="label">城市</text>
          <input class="input" v-model="form.city" maxlength="20" placeholder="例如：北京 / 上海" />
        </view>
        <view class="form-item">
          <text class="label">生日</text>
          <picker mode="date" :value="form.birthday" start="1970-01-01" end="2099-12-31" @change="handleBirthdayChange">
            <view class="picker-input">{{ form.birthday || '请选择生日' }}</view>
          </picker>
        </view>
        <view class="form-item no-border">
          <text class="label">个人介绍</text>
          <textarea class="textarea" v-model="form.introduce" maxlength="80" placeholder="介绍一下你喜欢的店铺风格和探店偏好"></textarea>
          <text class="count">{{ form.introduce.length }}/80</text>
        </view>
      </view>

      <view class="section-card info-card">
        <text class="section-title">账号信息</text>
        <view class="info-row">
          <text class="info-label">用户 ID</text>
          <text class="info-value">{{ sourceInfo.userId || '--' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">会员等级</text>
          <text class="info-value">{{ sourceInfo.levelName || `Lv.${sourceInfo.level || 1}` }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">积分</text>
          <text class="info-value">{{ sourceInfo.points || 0 }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">注册时间</text>
          <text class="info-value">{{ formatDate(sourceInfo.createTime) }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import LoadingSkeleton from '../../components/LoadingSkeleton.vue'
import api from '../../api/index'

const loading = ref(true)
const saving = ref(false)
const sourceInfo = ref({})

const avatarPresets = [
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop'
]

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const form = reactive({
  nickName: '',
  avatar: '',
  gender: 0,
  city: '',
  birthday: '',
  introduce: ''
})

function fillForm(data = {}) {
  sourceInfo.value = data || {}
  form.nickName = data.nickName || ''
  form.avatar = data.avatar || avatarPresets[0]
  form.gender = Number(data.gender ?? 0)
  form.city = data.city || ''
  form.birthday = data.birthday || ''
  form.introduce = data.introduce || ''
}

function formatDate(value) {
  if (!value) return '暂未记录'
  const text = String(value)
  return text.includes('T') ? text.slice(0, 10) : text.slice(0, 10)
}

function handleBirthdayChange(event) {
  form.birthday = event.detail.value
}

async function loadProfile() {
  loading.value = true
  try {
    const res = await api.user.getMe()
    fillForm(res)
  } catch (e) {
    uni.showToast({ title: '资料加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  if (saving.value) return
  if (!form.nickName.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await api.user.updateInfo({
      nickName: form.nickName.trim(),
      avatar: form.avatar.trim(),
      gender: Number(form.gender),
      city: form.city.trim(),
      birthday: form.birthday || undefined,
      introduce: form.introduce.trim()
    })
    uni.showToast({ title: '资料已保存', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 700)
  } catch (e) {
    uni.showToast({ title: '保存失败，请重试', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onLoad(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #fff7f7 0%, #f7f9fc 100%);
}

.hero-card,
.section-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 36rpx;
  box-shadow: 0 18rpx 50rpx rgba(0, 0, 0, 0.05);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.hero-left {
  display: flex;
  align-items: center;
  flex: 1;
  margin-right: 24rpx;
}

.avatar {
  width: 108rpx;
  height: 108rpx;
  border-radius: 32rpx;
  margin-right: 24rpx;
}

.avatar-text {
  display: flex;
  flex-direction: column;
}

.hero-title {
  font-size: 34rpx;
  color: #1a1a1a;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.hero-sub {
  font-size: 24rpx;
  color: #8b8d9b;
  line-height: 1.5;
}

.save-btn {
  padding: 16rpx 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #ff6b6b, #ff5a5f);
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;

  &.disabled {
    opacity: 0.6;
  }
}

.section-card {
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #222;
  margin-bottom: 24rpx;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18rpx;
  margin-bottom: 24rpx;
}

.preset-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 28rpx;
  border: 4rpx solid transparent;

  &.active {
    border-color: #ff5a5f;
    box-shadow: 0 12rpx 24rpx rgba(255, 90, 95, 0.18);
  }
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f3f4f7;
}

.no-border {
  border-bottom: none;
  padding-bottom: 0;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  font-weight: 700;
}

.input,
.picker-input,
.textarea {
  width: 100%;
  background: #f7f8fb;
  border-radius: 22rpx;
  padding: 24rpx;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #333;
}

.picker-input {
  min-height: 88rpx;
}

.textarea {
  height: 180rpx;
}

.count {
  display: block;
  text-align: right;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #a0a3b2;
}

.gender-row {
  display: flex;
  gap: 16rpx;
}

.gender-chip {
  flex: 1;
  text-align: center;
  background: #f7f8fb;
  color: #666;
  padding: 20rpx 0;
  border-radius: 22rpx;
  font-size: 28rpx;
  font-weight: 700;

  &.active {
    background: #fff0f0;
    color: #ff5a5f;
  }
}

.info-card {
  padding-bottom: 10rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f3f4f7;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 26rpx;
  color: #888;
}

.info-value {
  font-size: 28rpx;
  color: #222;
  font-weight: 700;
}
</style>
