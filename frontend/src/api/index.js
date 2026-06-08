import axios from 'axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: 'http://localhost:8000/api',
  timeout: 5000
})

api.interceptors.request.use(config => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

api.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message || '请求错误')
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      const store = useUserStore()
      store.logout()
      router.push('/login')
      ElMessage.error('认证失败，请重新登录')
    } else {
      ElMessage.error(err.response?.data?.message || '网络或服务器错误')
    }
    return Promise.reject(err)
  }
)

export const userApi = {
  getProfile: (id) => api.get(`/users/${id}`),
  updateProfile: (data) => api.put('/users/profile', data),
  toggleFollow: (id) => api.post(`/users/${id}/follow`),
  getUserPosts: (id, params) => api.get(`/users/${id}/posts`, { params }),
  getFollowing: (id, params) => api.get(`/users/${id}/following`, { params }),
  getFollowers: (id, params) => api.get(`/users/${id}/followers`, { params }),
  getUserProducts: (id) => api.get(`/users/${id}/products`),
  getUserLogistics: (id, params) => api.get(`/users/${id}/logistics`, { params }),
  getFollowFeed: (params) => api.get('/users/follow-feed', { params }),
  getRecommendUsers: (params) => api.get('/users/recommend', { params }),
  getAdminStats: () => api.get('/users/admin/stats'),
  getFollowerRankings: (params) => api.get('/users/admin/rankings/followers', { params })
}

export const certificateApi = {
  generate: (data) => api.post('/certificate/generate', data),
  batchGenerate: (data) => api.post('/certificate/batch-generate', data),
  getMy: (params) => api.get('/certificate/my', { params }),
  getDetail: (id) => api.get(`/certificate/${id}`),
  delete: (id) => api.delete(`/certificate/${id}`),
  verify: (certificateNo) => api.get(`/certificate/verify/${certificateNo}`),
  share: (data) => api.post('/certificate/share', data),
  getProductCount: (productId) => api.get(`/certificate/product/${productId}/count`),
  getFarmerProductStats: () => api.get('/certificate/farmer/products-stats'),
  getCertificateUsers: (productId, params) => api.get(`/certificate/farmer/certificate-users/${productId}`, { params }),
  productRankings: () => api.get('/certificate/rankings/products'),
  farmerRankings: () => api.get('/certificate/rankings/farmers'),
  adminStats: () => api.get('/certificate/admin/stats'),
  customizeProduct: (data) => api.put('/certificate/product/customize', data)
}

export default api
