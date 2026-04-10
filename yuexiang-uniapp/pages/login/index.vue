<template>
  <view class="login-container">
    <view class="dynamic-bg">
      <view class="blob blob-1"></view>
      <view class="blob blob-2"></view>
    </view>

    <view class="status-bar"></view>
    
    <view class="header fade-up">
      <view class="logo-wrap shadow-glass">
        <AppIcon class="logo" name="fire" color="#fff" />
      </view>
      <text class="title">悦享生活</text>
      <text class="subtitle">发现身边的美好时刻</text>
    </view>
    
    <view class="form-area glass-panel fade-up delay-1">
      <view class="input-group">
        <AppIcon class="icon" name="cellphone" color="#333" />
        <input class="input" type="number" maxlength="11" v-model="phone" placeholder="请输入手机号" placeholder-style="color:#aaa;" />
      </view>
      
        <view class="input-group code-group">
        <AppIcon class="icon" name="shield-check-outline" color="#333" />
        <input class="input" type="number" maxlength="6" v-model="code" placeholder="请输入验证码" placeholder-style="color:#aaa;" />
        <view class="send-btn" :class="{ disabled: countdown > 0 }" @click="sendCode">
          {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
        </view>
      </view>

        <view class="captcha-row" v-show="showCaptcha">
          <view class="captcha-left">
            <image
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-img"
              mode="aspectFit"
              @click="refreshCaptcha"
            />
          </view>
          <view class="captcha-right">
            <input
              class="captcha-input"
              type="text"
              maxlength="10"
              v-model="captchaCode"
              placeholder="请输入图形验证码"
              placeholder-style="color:#aaa;"
            />
            <view class="captcha-hint">点击图片刷新</view>
          </view>
        </view>
      
      <view class="login-btn" :class="{ active: canLogin }" @click="handleLogin">
        <text class="txt">登 录</text>
      </view>
      
      <view class="agreement">
        <view class="checkbox" :class="{ checked: isAgreed }" @click="isAgreed = !isAgreed">
          <AppIcon v-if="isAgreed" class="check-icon" name="check-bold" color="#fff" />
        </view>
        <text class="text">已阅读并同意<text class="link">《用户协议》</text>和<text class="link">《隐私政策》</text></text>
      </view>
    </view>
    
    <view class="other-login fade-up delay-2">
      <view class="divider">
        <view class="line"></view>
        <text class="text">其他方式快速登录</text>
        <view class="line"></view>
      </view>
      <view class="icon-list">
        <view class="icon-wrap wechat">
          <AppIcon class="img" name="wechat" color="#07C160" />
        </view>
        <view class="icon-wrap qq">
          <AppIcon class="img" name="qqchat" color="#12B7F5" />
        </view>
        <view class="icon-wrap email">
          <AppIcon class="img" name="email-outline" color="#FFB300" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../api'
import { useUserStore } from '../../store/user'
import AppIcon from '../../components/AppIcon.vue'

const phone = ref('')
const code = ref('')
// 短信发送/登录的图形验证码输入
const captchaCode = ref('')
// 后端返回的 captchaId（调用 `/api/auth/sms-code` 时需透传）
const captchaId = ref('')
// base64 图片
const captchaImage = ref('')
const showCaptcha = ref(false)
const userStore = useUserStore()

const isAgreed = ref(false)
const countdown = ref(0)
let timer = null

const canLogin = computed(() => {
  const phoneStr = String(phone.value)
  return (
    phoneStr.length === 11 &&
    code.value.length === 6 &&
    isAgreed.value
  )
})

const refreshCaptcha = async () => {
  try {
    const res = await api.auth.getCaptcha()
    // 后端 CaptchaVO: { captchaId, image, expireSeconds }
    captchaId.value = res.captchaId
    captchaImage.value = res.image
  } catch (e) {
    uni.showToast({ title: '获取验证码失败', icon: 'none' })
  }
}

const sendCode = async () => {
  if (countdown.value > 0) return
  if (String(phone.value).length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  if (!captchaId.value || !captchaCode.value) {
    showCaptcha.value = true
    if (!captchaId.value) {
      await refreshCaptcha()
    }
    uni.showToast({ title: '请先输入图形验证码', icon: 'none' })
    return
  }

  try {
    await api.auth.sendSmsCode({
      phone: `+86${phone.value}`,
      type: 0,
      captchaId: captchaId.value,
      captchaCode: captchaCode.value
    })
  } catch (e) {
    uni.showToast({ title: '短信验证码发送失败', icon: 'none' })
    await refreshCaptcha()
    return
  }
  
  uni.showToast({ title: '验证码已发送', icon: 'success' })
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = () => {
  if (!isAgreed.value) {
    uni.showToast({ title: '请先阅读并同意用户协议', icon: 'none' })
    return
  }
  if (!canLogin.value) return
  
  uni.showLoading({ title: '安全校验中...' })

  // 直接走后端登录短信验证码换 token
  api.auth.loginBySms({
      phone: `+86${phone.value}`,
      code: code.value
  }).then((res) => {
    uni.hideLoading()
    // LoginVO: { accessToken, refreshToken, expiresIn, user... }
    userStore.setSession(res)
    uni.showToast({ title: '欢迎来到悦享生活', icon: 'none' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' })
    }, 800)
  }).catch(() => {
    uni.hideLoading()
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
    refreshCaptcha()
  })
}

onMounted(() => {
  // 进入页面不加载验证码，点击获取验证码时再加载
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh; background-color: #F8F9FA; display: flex; flex-direction: column; position: relative; overflow-x: hidden;
}

/* 动态水墨流体背景 */
.dynamic-bg {
  position: absolute; top: 0; left: 0; width: 100%; height: 100vh; z-index: 0; overflow: hidden;
  background: #fdfbfb;
  .blob {
    position: absolute; border-radius: 50%; filter: blur(100rpx); opacity: 0.5; animation: floatAnim 10s infinite alternate ease-in-out;
  }
  .blob-1 { width: 700rpx; height: 700rpx; background: #ff9a9e; top: -100rpx; left: -200rpx; animation-delay: 0s;}
  .blob-2 { width: 600rpx; height: 600rpx; background: #fecfef; bottom: 100rpx; right: -200rpx; animation-delay: -5s;}
}

@keyframes floatAnim {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(80rpx, 120rpx) scale(1.2); }
}

.status-bar { height: var(--status-bar-height, 44px); position: relative; z-index: 10;}

.fade-up { animation: fadeUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) both; }
.delay-1 { animation-delay: 0.15s; }
.delay-2 { animation-delay: 0.3s; }
@keyframes fadeUp { from { opacity: 0; transform: translateY(60rpx); } to { opacity: 1; transform: translateY(0); } }

.header {
  padding: 100rpx 60rpx 60rpx; display: flex; flex-direction: column; position: relative; z-index: 10;
  
  .logo-wrap {
    width: 120rpx; height: 120rpx; background: linear-gradient(135deg, #FF6B6B, #FF4757); border-radius: 40rpx;
    display: flex; justify-content: center; align-items: center; margin-bottom: 40rpx;
    box-shadow: 0 16rpx 40rpx rgba(255, 71, 87, 0.3); border: 2rpx solid rgba(255, 255, 255, 0.4);
    
    .logo { width: 64rpx; height: 64rpx; filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.2));}
  }
  
  .title { font-size: 64rpx; font-weight: 900; color: #1a1a1a; margin-bottom: 16rpx; letter-spacing: 2rpx;}
  .subtitle { font-size: 32rpx; color: #777; font-weight: 600; letter-spacing: 1rpx;}
}

/* 高级登录表单 */
.form-area.glass-panel {
  margin: 0 50rpx; background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(40px);
  border-radius: 48rpx; padding: 50rpx 40rpx; position: relative; z-index: 10;
  box-shadow: 0 30rpx 80rpx rgba(0,0,0,0.06), inset 0 2rpx 0 rgba(255,255,255,1); border: 2rpx solid rgba(255,255,255,0.7);
  
  .input-group {
    display: flex; align-items: center; background: rgba(255, 255, 255, 0.8); height: 110rpx; border-radius: 55rpx;
    padding: 0 40rpx; margin-bottom: 30rpx; box-shadow: inset 0 4rpx 10rpx rgba(0,0,0,0.02);
    border: 2rpx solid rgba(255,255,255,0.4); transition: border-color 0.3s, background 0.3s;
    
    &:focus-within { background: #fff; border-color: #FF5A5F; box-shadow: 0 8rpx 30rpx rgba(255, 90, 95, 0.1);}
    
    .icon { width: 44rpx; height: 44rpx; margin-right: 20rpx; }
    .input { flex: 1; font-size: 32rpx; color: #1a1a1a; font-weight: bold;}
  }
  
  .code-group {
    .input { flex: 1; }
    .send-btn {
      font-size: 28rpx; font-weight: 800; color: #FF5A5F; padding-left: 30rpx; height: 40rpx; line-height: 40rpx;
      border-left: 2rpx solid #EAEAEA;
      &.disabled { color: #aaa; }
    }
  }
  
  .login-btn {
    margin-top: 60rpx; height: 110rpx; background: #e0e0e0; border-radius: 55rpx;
    display: flex; justify-content: center; align-items: center; transition: all 0.3s; pointer-events: none;
    .txt { font-size: 34rpx; font-weight: 900; letter-spacing: 4rpx; color: #fff;}
    
    &.active {
      background: linear-gradient(135deg, #FF6B6B, #FF4757); pointer-events: auto;
      box-shadow: 0 16rpx 40rpx rgba(255, 71, 87, 0.35); text-shadow: 0 4rpx 8rpx rgba(0,0,0,0.2);
      &:active { transform: scale(0.96); }
    }
  }
  
  .agreement {
    display: flex; justify-content: center; align-items: center; margin-top: 40rpx;
    
    .checkbox {
      width: 36rpx; height: 36rpx; border: 4rpx solid #d0d0d0; border-radius: 10rpx; margin-right: 16rpx;
      display: flex; justify-content: center; align-items: center; transition: all 0.2s; box-sizing: border-box; background: #fff;
      
      &.checked { background: #FF5A5F; border-color: #FF5A5F; }
      .check-icon { width: 24rpx; height: 24rpx; }
    }
    
    .text { font-size: 24rpx; color: #888; font-weight: 500; .link { color: #FF5A5F; font-weight: bold; } }
  }
}

/* 图形验证码行 */
.captcha-row {
  margin: 0 0 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 0 10rpx;
}
.captcha-left {
  flex-shrink: 0;
}
.captcha-img {
  width: 200rpx;
  height: 70rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.7);
}
.captcha-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.captcha-input {
  height: 70rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.8);
  border: 2rpx solid rgba(255, 255, 255, 0.6);
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1a1a1a;
}
.captcha-hint {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #aaa;
}

.other-login {
  margin-top: auto; padding-bottom: 80rpx; padding-top: 60rpx; position: relative; z-index: 10;
  
  .divider {
    display: flex; align-items: center; padding: 0 100rpx; margin-bottom: 50rpx;
    .line { flex: 1; height: 2rpx; background: rgba(0,0,0,0.06); }
    .text { font-size: 24rpx; color: #aaa; font-weight: bold; margin: 0 24rpx; }
  }
  
  .icon-list {
    display: flex; justify-content: center;
    .icon-wrap {
      width: 100rpx; height: 100rpx; border-radius: 50%; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px);
      display: flex; justify-content: center; align-items: center; margin: 0 36rpx; transition: transform 0.2s;
      box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.05); border: 2rpx solid rgba(255,255,255,1);
      &:active { transform: scale(0.9); }
      .img { width: 50rpx; height: 50rpx; }
    }
  }
}
</style>
