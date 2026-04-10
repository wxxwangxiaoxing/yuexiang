<template>
  <view class="publish-container">
    <view class="nav-bar">
      <text class="cancel" @click="goBack">取消</text>
      <text class="title">发布笔记</text>
      <view class="publish-btn" :class="{ disabled: !canPublish }" @click="handlePublish">发布</view>
    </view>
    
    <scroll-view class="scroll-area" scroll-y>
      <!-- 添加图片 -->
      <view class="img-uploader">
        <view class="add-btn" v-if="images.length < 9" @click="chooseImage">
          <AppIcon class="icon" name="camera-plus-outline" color="#999" />
          <text class="txt">添加图片</text>
        </view>
        <view class="img-preview" v-for="(img, idx) in images" :key="img._id">
          <image class="preview-img" :src="img.localPath || img.url" mode="aspectFill"></image>
          <view class="upload-loading" v-if="img.uploading">
            <text class="upload-text">上传中...</text>
          </view>
          <view class="upload-error" v-if="img.error" @click="retryUpload(img._id)">
            <text class="upload-text">点击重试</text>
          </view>
          <view class="del-btn" @click="removeImage(idx)" v-if="!img.uploading">
            <AppIcon class="del-icon" name="close" color="#fff" />
          </view>
        </view>
      </view>
      
      <!-- 标题 -->
      <view class="form-group">
        <input class="title-input" v-model="form.title" placeholder="填写标题，会有更多赞哦~" placeholder-style="color:#ccc; font-weight:normal;" maxlength="30" />
        <view class="ai-gen-btn" @click="aiGenerateTitle">
          <AppIcon class="ai-icon" name="robot-outline" color="#6C5CE7" />
          <text>AI 帮你写标题</text>
        </view>
      </view>
      <view class="divider"></view>
      <!-- 正文 -->
      <view class="form-group">
        <textarea class="content-input" v-model="form.content" placeholder="添加正文，分享你的探店真实体验..." :maxlength="1000"></textarea>
        <view class="ai-toolbar">
          <view class="ai-tool" @click="aiPolishContent">
            <AppIcon class="ai-icon" name="magic-staff" color="#6C5CE7" />
            <text>AI 润色</text>
          </view>
          <view class="ai-tool" @click="aiExpandContent">
            <AppIcon class="ai-icon" name="file-document-edit-outline" color="#6C5CE7" />
            <text>AI 扩写</text>
          </view>
          <text class="word-count">{{ contentLength }} / 1000</text>
        </view>
      </view>
      
      <view class="options-list">
        <view class="opt-item" @click="chooseShop">
          <AppIcon class="o-icon" name="map-marker-radius" color="#333" />
          <text class="o-tit">关联商户</text>
          <template v-if="selectedShop">
            <text class="o-value">{{ selectedShop.name }}</text>
            <view class="shop-remove" @click.stop="removeShop">
              <AppIcon class="remove-icon" name="close" color="#999" />
            </view>
          </template>
          <AppIcon v-else class="arrow" name="chevron-right" color="#ccc" />
        </view>
        <view class="opt-item" @click="aiSuggestTags">
          <AppIcon class="o-icon" name="tag-multiple-outline" color="#333" />
          <view class="o-tit-group">
            <text class="o-tit">参与话题</text>
            <view class="ai-tag-badge">AI自动推荐</view>
          </view>
          <view class="tag-list" v-if="tags.length > 0">
            <text class="tag-item" v-for="tag in tags" :key="tag.id">#{{ tag.name }}</text>
          </view>
          <AppIcon class="arrow" name="chevron-right" color="#ccc" />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from '../../components/AppIcon.vue'
import api from '../../api/index'

const form = ref({
  title: '',
  content: '',
  shopId: null
})

let imageIdCounter = 0
const images = ref([])
const tags = ref([])
const selectedShop = ref(null)
const contentLength = computed(() => form.value.content?.length || 0)
const canPublish = computed(() => 
  form.value.title && 
  form.value.content && 
  images.value.length > 0 && 
  images.value.every(img => img.url && !img.uploading && !img.error)
)

const goBack = () => uni.navigateBack()

const chooseShop = () => {
  uni.navigateTo({
    url: '/pages/shop/select',
    events: {
      selectShop: (shop) => {
        selectedShop.value = {
          id: shop.id,
          name: shop.name,
          address: shop.addressText || '',
          avgPrice: shop.avgPrice || '0'
        }
        form.value.shopId = shop.id
      }
    }
  })
}

const removeShop = () => {
  selectedShop.value = null
  form.value.shopId = null
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - images.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const newImages = res.tempFilePaths.map(path => ({
        _id: ++imageIdCounter,
        localPath: path,
        url: null,
        uploading: false,
        error: false
      }))
      images.value = [...images.value, ...newImages]
      newImages.forEach(img => uploadByImageId(img._id))
    }
  })
}

const uploadByImageId = async (id) => {
  const img = images.value.find(i => i._id === id)
  if (!img || img.url || img.uploading) return
  
  img.uploading = true
  try {
    img.url = await api.blog.uploadImage(img.localPath)
  } catch (e) {
    img.error = true
    uni.showToast({ title: '图片上传失败', icon: 'none' })
  } finally {
    img.uploading = false
  }
}

const retryUpload = (id) => {
  const img = images.value.find(i => i._id === id)
  if (img) {
    img.error = false
    uploadByImageId(id)
  }
}

const removeImage = (idx) => {
  images.value.splice(idx, 1)
}

const aiGenerateTitle = async () => {
  if (!form.value.content) {
    uni.showToast({ title: '请先填写内容', icon: 'none' })
    return
  }
  uni.showLoading({ title: 'AI生成中...' })
  try {
    const res = await api.ai.generateTitle({ content: form.value.content })
    if (res?.titles?.length > 0) {
      form.value.title = res.titles[0]
      uni.showToast({ title: '标题已生成', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: 'AI生成失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const aiPolishContent = async () => {
  if (!form.value.content) {
    uni.showToast({ title: '请先填写内容', icon: 'none' })
    return
  }
  uni.showLoading({ title: 'AI润色中...' })
  try {
    const res = await api.ai.polishContent({ content: form.value.content, style: 'literary' })
    if (res?.polished) {
      form.value.content = res.polished
      uni.showToast({ title: '润色完成', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: 'AI润色失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const aiExpandContent = async () => {
  if (!form.value.content) {
    uni.showToast({ title: '请先填写内容', icon: 'none' })
    return
  }
  uni.showLoading({ title: 'AI扩写中...' })
  try {
    const res = await api.ai.expandContent({ content: form.value.content, targetLength: 'medium' })
    if (res?.expanded) {
      form.value.content = res.expanded
      uni.showToast({ title: '扩写完成', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: 'AI扩写失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const aiSuggestTags = async () => {
  if (!form.value.content && !form.value.title) {
    uni.showToast({ title: '请先填写内容', icon: 'none' })
    return
  }
  uni.showLoading({ title: 'AI推荐中...' })
  try {
    const res = await api.ai.suggestTags({ title: form.value.title, content: form.value.content })
    if (res?.tags?.length > 0) {
      tags.value = res.tags
      uni.showToast({ title: '标签已推荐', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: 'AI推荐失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const handlePublish = async () => {
  if (!canPublish.value) {
    uni.showToast({ title: '请填写标题、内容和图片', icon: 'none' })
    return
  }
  uni.showLoading({ title: '发布中...' })
  try {
    const uploadedUrls = images.value.map(img => img.url).filter(Boolean)

    await api.blog.publishBlog({
      title: form.value.title,
      content: form.value.content,
      shopId: form.value.shopId,
      images: uploadedUrls,
      tagIds: tags.value.map(t => t.id)
    })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.showToast({ title: '发布失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.publish-container { height: 100vh; background: #fff; display: flex; flex-direction: column; }
.nav-bar {
  padding: 2rpx 30rpx 16rpx; display: flex; justify-content: space-between; align-items: center; border-bottom: 2rpx solid #f5f5f5;
  .cancel { font-size: 32rpx; color: #666; }
  .title { font-size: 34rpx; font-weight: bold; color: #333; }
  .publish-btn { background: #FF5A5F; color: #fff; font-size: 28rpx; padding: 12rpx 36rpx; border-radius: 32rpx; font-weight: bold; }
}
.scroll-area { flex: 1; height: 0; padding: 30rpx; box-sizing: border-box;}
.img-uploader {
  margin-bottom: 40rpx; display: flex; flex-wrap: wrap; gap: 20rpx;
  .add-btn { width: 220rpx; height: 220rpx; background: #F5F6F8; border-radius: 20rpx; display: flex; flex-direction: column; justify-content: center; align-items: center; .icon { width: 64rpx; height: 64rpx; margin-bottom: 12rpx;} .txt { font-size: 26rpx; color: #999; }}
  .img-preview { width: 220rpx; height: 220rpx; border-radius: 20rpx; overflow: hidden; position: relative;
    .preview-img { width: 100%; height: 100%; }
    .upload-loading, .upload-error { position: absolute; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; .upload-text { color: #fff; font-size: 24rpx; font-weight: bold; }}
    .upload-error { background: rgba(255,0,0,0.5); cursor: pointer; }
    .del-btn { position: absolute; top: 8rpx; right: 8rpx; width: 44rpx; height: 44rpx; background: rgba(0,0,0,0.5); border-radius: 50%; display: flex; justify-content: center; align-items: center; .del-icon { width: 28rpx; height: 28rpx; }}
  }
}
.form-group {
  margin-bottom: 30rpx;
  .title-input { font-size: 36rpx; font-weight: bold; color: #333; padding: 20rpx 0; }
  .ai-gen-btn { display: inline-flex; align-items: center; background: #F0EDFF; padding: 12rpx 24rpx; border-radius: 30rpx; margin-top: 12rpx; .ai-icon { width: 32rpx; height: 32rpx; margin-right: 8rpx;} color: #6C5CE7; font-size: 24rpx; font-weight: bold;}
  .content-input { width: 100%; height: 300rpx; font-size: 32rpx; color: #333; padding: 20rpx 0; line-height: 1.6; }
  .ai-toolbar {
    display: flex; align-items: center; margin-top: 20rpx;
    .ai-tool { display: flex; align-items: center; background: #F0EDFF; padding: 12rpx 24rpx; border-radius: 30rpx; margin-right: 20rpx; .ai-icon { width: 32rpx; height: 32rpx; margin-right: 6rpx;} color: #6C5CE7; font-size: 24rpx; font-weight: bold;}
    .word-count { margin-left: auto; font-size: 24rpx; color: #ccc; }
  }
}
.divider { height: 2rpx; background: #f0f0f0; margin: 20rpx 0; }
.options-list {
  border-top: 2rpx solid #f5f5f5; margin-top: 40rpx;
  .opt-item {
    display: flex; align-items: center; padding: 40rpx 0; border-bottom: 2rpx solid #f5f5f5;
    .o-icon { width: 44rpx; height: 44rpx; margin-right: 24rpx;}
    .o-tit { font-size: 32rpx; color: #333; flex: 1; }
    .o-tit-group { flex: 1; display: flex; align-items: center; .ai-tag-badge { background: #FFEBEE; color: #FF5A5F; font-size: 22rpx; font-weight: bold; padding: 6rpx 16rpx; border-radius: 12rpx; margin-left: 16rpx; }}
    .o-value { font-size: 28rpx; color: #FF5A5F; font-weight: 500; margin-right: 12rpx; max-width: 300rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .shop-remove { width: 48rpx; height: 48rpx; display: flex; justify-content: center; align-items: center; margin-right: 8rpx; .remove-icon { width: 36rpx; height: 36rpx; }}
    .arrow { width: 40rpx; height: 40rpx; }
  }
}
</style>
