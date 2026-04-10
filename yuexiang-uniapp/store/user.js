import { defineStore } from 'pinia';
import userApi from '../api/user';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: uni.getStorageSync('userInfo') || null,
    token: uni.getStorageSync('token') || '',
    refreshToken: uni.getStorageSync('refreshToken') || '',
    accessTokenExpiresAt: uni.getStorageSync('accessTokenExpiresAt') || 0,
    refreshTokenExpiresAt: uni.getStorageSync('refreshTokenExpiresAt') || 0,
    profileLoaded: !!uni.getStorageSync('userInfo')
  }),
  actions: {
    setToken(token) {
      this.token = token;
      uni.setStorageSync('token', token);
    },
    setRefreshToken(refreshToken) {
      this.refreshToken = refreshToken;
      uni.setStorageSync('refreshToken', refreshToken);
    },
    setTokenExpiry(expiresIn, absoluteExpiresIn) {
      const now = Date.now();
      const accessTokenExpiresAt = expiresIn ? now + Number(expiresIn) * 1000 : 0;
      const refreshTokenExpiresAt = absoluteExpiresIn ? now + Number(absoluteExpiresIn) * 1000 : this.refreshTokenExpiresAt || 0;
      this.accessTokenExpiresAt = accessTokenExpiresAt;
      this.refreshTokenExpiresAt = refreshTokenExpiresAt;
      uni.setStorageSync('accessTokenExpiresAt', accessTokenExpiresAt);
      uni.setStorageSync('refreshTokenExpiresAt', refreshTokenExpiresAt);
    },
    setSession(session) {
      this.setToken(session?.accessToken || '');
      this.setRefreshToken(session?.refreshToken || '');
      this.setTokenExpiry(session?.expiresIn, session?.absoluteExpiresIn);
      if (session?.user) {
        this.userInfo = session.user;
        this.profileLoaded = true;
        uni.setStorageSync('userInfo', session.user);
      }
    },
    applyTokenRefresh(payload) {
      this.setToken(payload?.accessToken || '');
      this.setRefreshToken(payload?.refreshToken || this.refreshToken || '');
      this.setTokenExpiry(payload?.expiresIn, null);
    },
    setUserInfo(info) {
      this.userInfo = info;
      this.profileLoaded = true;
      uni.setStorageSync('userInfo', info);
    },
    async fetchProfile(force = false) {
      if (!this.token) {
        this.userInfo = null;
        this.profileLoaded = false;
        uni.removeStorageSync('userInfo');
        return null;
      }
      if (this.profileLoaded && !force) {
        return this.userInfo;
      }
      try {
        const info = await userApi.getMe();
        this.userInfo = info;
        this.profileLoaded = true;
        uni.setStorageSync('userInfo', info);
        return info;
      } catch (error) {
        const unauthorized = error?.statusCode === 401 || error?.code === 401;
        if (unauthorized) {
          this.logout();
        } else {
          this.profileLoaded = !!this.userInfo;
        }
        throw error;
      }
    },
    logout() {
      this.token = '';
      this.refreshToken = '';
      this.accessTokenExpiresAt = 0;
      this.refreshTokenExpiresAt = 0;
      this.userInfo = null;
      this.profileLoaded = false;
      uni.removeStorageSync('token');
      uni.removeStorageSync('refreshToken');
      uni.removeStorageSync('accessTokenExpiresAt');
      uni.removeStorageSync('refreshTokenExpiresAt');
      uni.removeStorageSync('userInfo');
    }
  }
});
