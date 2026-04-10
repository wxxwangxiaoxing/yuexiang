import { getBaseUrl } from '../config/app'

export function uploadFile({
  url,
  filePath,
  name = 'file',
  formData,
  header
}) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${getBaseUrl()}${url}`,
      filePath,
      name,
      formData: formData || {},
      header: {
        Authorization: uni.getStorageSync('token')
          ? `Bearer ${uni.getStorageSync('token')}`
          : '',
        ...header
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (res.statusCode === 200 && data.code === 200) {
            resolve(data.data)
            return
          }
          reject(data)
        } catch (error) {
          reject(error)
        }
      },
      fail: reject
    })
  })
}

export default uploadFile
