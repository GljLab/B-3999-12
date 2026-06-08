<template>
  <div class="user-home min-h-screen bg-gray-50">
    <div v-loading="profileLoading" class="max-w-6xl mx-auto px-4 py-6">
      <div v-if="profile" class="space-y-6">
        <!-- 头部信息区 -->
        <div class="bg-white rounded-2xl shadow-sm p-6">
          <div class="flex flex-col md:flex-row gap-6 items-start md:items-center">
            <div class="flex-shrink-0">
              <div class="w-24 h-24 rounded-full bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center text-white text-3xl font-bold overflow-hidden">
                <img v-if="profile.user.avatar" :src="profile.user.avatar" class="w-full h-full object-cover" alt="头像">
                <span v-else>{{ avatarLetter }}</span>
              </div>
            </div>
            <div class="flex-1">
              <div class="flex flex-wrap items-center gap-3 mb-2">
                <h1 class="text-2xl font-bold text-gray-800">{{ profile.user.realName }}</h1>
                <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
                <span class="text-sm text-gray-400">加入 {{ profile.daysJoined }} 天</span>
              </div>
              <p class="text-gray-600 mb-3">{{ profile.user.signature || '这个人很懒，什么都没写~' }}</p>
              <div class="flex flex-wrap gap-6 text-sm">
                <div class="flex items-center gap-2">
                  <span class="text-gray-500">支持者</span>
                  <span class="font-bold text-green-600 text-lg">{{ profile.user.followerCount }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-gray-500">收藏农友</span>
                  <span class="font-bold text-green-600 text-lg">{{ profile.user.followingCount }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-gray-500">互相收藏</span>
                  <span class="font-bold text-green-600 text-lg">{{ profile.user.mutualFollowCount }}</span>
                </div>
              </div>
            </div>
            <div class="flex-shrink-0">
              <FollowButton
                :user-id="userId"
                :initial-followed="profile.user.isFollowed"
                :initial-mutual="profile.user.isMutual"
                size="large"
                @self-click="showEditDialog = true"
                @toggle="onFollowToggle"
              />
            </div>
          </div>
        </div>

        <!-- 数据统计面板 -->
        <div class="bg-white rounded-2xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">📊 数据统计</h3>
          <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4">
            <div class="text-center p-3 bg-green-50 rounded-xl">
              <div class="text-2xl font-bold text-green-600">{{ profile.stats.postCount }}</div>
              <div class="text-sm text-gray-500">社区发帖</div>
            </div>
            <div class="text-center p-3 bg-blue-50 rounded-xl">
              <div class="text-2xl font-bold text-blue-600">{{ profile.stats.totalLikes }}</div>
              <div class="text-sm text-gray-500">获赞总数</div>
            </div>
            <div class="text-center p-3 bg-yellow-50 rounded-xl">
              <div class="text-2xl font-bold text-yellow-600">{{ profile.stats.totalBookmarks }}</div>
              <div class="text-sm text-gray-500">收藏总数</div>
            </div>
            <div class="text-center p-3 bg-purple-50 rounded-xl">
              <div class="text-2xl font-bold text-purple-600">{{ profile.stats.commentCount }}</div>
              <div class="text-sm text-gray-500">评论发表</div>
            </div>
            <template v-if="profile.user.role === 'FARMER'">
              <div class="text-center p-3 bg-orange-50 rounded-xl">
                <div class="text-2xl font-bold text-orange-600">{{ profile.roleStats.productCount || 0 }}</div>
                <div class="text-sm text-gray-500">发布产品</div>
              </div>
              <div class="text-center p-3 bg-red-50 rounded-xl">
                <div class="text-2xl font-bold text-red-600">{{ profile.roleStats.tracingCodeCount || 0 }}</div>
                <div class="text-sm text-gray-500">溯源码数</div>
              </div>
            </template>
            <template v-else-if="profile.user.role === 'LOGS_ADMIN'">
              <div class="text-center p-3 bg-cyan-50 rounded-xl">
                <div class="text-2xl font-bold text-cyan-600">{{ profile.roleStats.logisticsCount || 0 }}</div>
                <div class="text-sm text-gray-500">物流节点</div>
              </div>
            </template>
            <template v-else-if="profile.user.role === 'SYS_ADMIN'">
              <div class="text-center p-3 bg-indigo-50 rounded-xl">
                <div class="text-2xl font-bold text-indigo-600">{{ profile.roleStats.managedUserCount || 0 }}</div>
                <div class="text-sm text-gray-500">管理用户</div>
              </div>
            </template>
          </div>
        </div>

        <!-- Tab内容区 -->
        <div class="bg-white rounded-2xl shadow-sm p-6">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 动态 -->
            <el-tab-pane label="📝 动态" name="posts">
              <div class="mb-4 flex flex-wrap gap-3">
                <el-radio-group v-model="postFilter" size="small" @change="loadPosts(0)">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="hasImage">有图片</el-radio-button>
                  <el-radio-button label="highLike">高赞</el-radio-button>
                </el-radio-group>
                <el-radio-group v-model="postSort" size="small" @change="loadPosts(0)">
                  <el-radio-button label="latest">最新</el-radio-button>
                  <el-radio-button label="likes">点赞</el-radio-button>
                  <el-radio-button label="comments">评论</el-radio-button>
                </el-radio-group>
              </div>
              <div v-if="posts.length === 0 && !postsLoading" class="py-12 text-center text-gray-400">
                暂无动态
              </div>
              <div v-else class="space-y-4">
                <div v-for="post in posts" :key="post.id" class="border border-gray-100 rounded-xl p-4 hover:shadow-md transition-shadow">
                  <router-link :to="`/community/${post.id}`" class="block">
                    <div class="flex gap-4">
                      <div v-if="post.coverImage" class="w-32 h-24 rounded-lg overflow-hidden flex-shrink-0">
                        <img :src="post.coverImage" class="w-full h-full object-cover" alt="">
                      </div>
                      <div class="flex-1 min-w-0">
                        <h4 class="font-semibold text-gray-800 mb-2 truncate">{{ post.title }}</h4>
                        <p class="text-sm text-gray-500 line-clamp-2 mb-2">{{ post.description }}</p>
                        <div class="flex flex-wrap gap-2 mb-2">
                          <el-tag v-for="topic in post.topics" :key="topic.id" size="small" type="success">{{ topic.name }}</el-tag>
                        </div>
                        <div class="flex items-center gap-4 text-xs text-gray-400">
                          <span>💬 {{ post.commentCount }}</span>
                          <span>👍 {{ post.likeCount }}</span>
                          <span>⭐ {{ post.bookmarkCount }}</span>
                          <span>{{ formatDate(post.createdAt) }}</span>
                        </div>
                      </div>
                    </div>
                  </router-link>
                </div>
              </div>
              <div v-if="postsTotal > 20" class="flex justify-end mt-6">
                <el-pagination
                  v-model:current-page="postsPage"
                  :page-size="20"
                  :total="postsTotal"
                  layout="prev, pager, next, total"
                  @current-change="loadPosts"
                />
              </div>
            </el-tab-pane>

            <!-- 收藏农友 -->
            <el-tab-pane label="👥 收藏农友" name="following">
              <div v-if="following.length === 0 && !followingLoading" class="py-12 text-center text-gray-400">
                还没有收藏的农友
              </div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div v-for="user in following" :key="user.id" class="border border-gray-100 rounded-xl p-4 hover:shadow-md transition-shadow">
                  <div class="flex items-start gap-3">
                    <router-link :to="`/user-home/${user.id}`" class="flex-shrink-0">
                      <div class="w-12 h-12 rounded-full bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center text-white font-bold">
                        {{ user.realName?.charAt(0) || user.username?.charAt(0) }}
                      </div>
                    </router-link>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2 mb-1">
                        <router-link :to="`/user-home/${user.id}`" class="font-semibold text-gray-800 hover:text-green-600">{{ user.realName }}</router-link>
                        <el-tag v-if="user.isMutual" size="small" type="success">互关</el-tag>
                      </div>
                      <p class="text-xs text-gray-500 mb-2 truncate">{{ user.signature || '暂无签名' }}</p>
                      <div class="flex items-center justify-between">
                        <span class="text-xs text-gray-400">{{ user.followerCount }} 支持者</span>
                        <FollowButton
                          :user-id="user.id"
                          :initial-followed="true"
                          :initial-mutual="user.isMutual"
                          size="small"
                          :show-self="false"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="followingTotal > 20" class="flex justify-end mt-6">
                <el-pagination
                  v-model:current-page="followingPage"
                  :page-size="20"
                  :total="followingTotal"
                  layout="prev, pager, next, total"
                  @current-change="loadFollowing"
                />
              </div>
            </el-tab-pane>

            <!-- 支持者 -->
            <el-tab-pane label="❤️ 支持者" name="followers">
              <div v-if="followers.length === 0 && !followersLoading" class="py-12 text-center text-gray-400">
                暂无支持者
              </div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div v-for="user in followers" :key="user.id" class="border border-gray-100 rounded-xl p-4 hover:shadow-md transition-shadow">
                  <div class="flex items-start gap-3">
                    <router-link :to="`/user-home/${user.id}`" class="flex-shrink-0">
                      <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white font-bold">
                        {{ user.realName?.charAt(0) || user.username?.charAt(0) }}
                      </div>
                    </router-link>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2 mb-1">
                        <router-link :to="`/user-home/${user.id}`" class="font-semibold text-gray-800 hover:text-green-600">{{ user.realName }}</router-link>
                        <el-tag v-if="user.isMutual" size="small" type="success">互关</el-tag>
                      </div>
                      <p class="text-xs text-gray-500 mb-2 truncate">{{ user.signature || '暂无签名' }}</p>
                      <div class="flex items-center justify-between">
                        <span class="text-xs text-gray-400">{{ user.followerCount }} 支持者</span>
                        <FollowButton
                          :user-id="user.id"
                          :initial-followed="user.isMutual"
                          :initial-mutual="user.isMutual"
                          size="small"
                          :show-self="false"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="followersTotal > 20" class="flex justify-end mt-6">
                <el-pagination
                  v-model:current-page="followersPage"
                  :page-size="20"
                  :total="followersTotal"
                  layout="prev, pager, next, total"
                  @current-change="loadFollowers"
                />
              </div>
            </el-tab-pane>

            <!-- FARMER专属：我的农场 -->
            <el-tab-pane v-if="profile.user.role === 'FARMER'" label="🌱 我的农场" name="products">
              <div v-if="products.length === 0 && !productsLoading" class="py-12 text-center text-gray-400">
                暂无产品
              </div>
              <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div v-for="product in products" :key="product.id" class="border border-gray-100 rounded-xl overflow-hidden hover:shadow-md transition-shadow">
                  <div class="h-40 bg-gradient-to-br from-green-100 to-green-200 flex items-center justify-center">
                    <span class="text-4xl">🥬</span>
                  </div>
                  <div class="p-4">
                    <h4 class="font-semibold text-gray-800 mb-2">{{ product.productName }}</h4>
                    <div class="text-sm text-gray-500 space-y-1">
                      <p>🏷️ {{ product.category }}</p>
                      <p>📍 {{ product.origin }}</p>
                      <p>📅 采摘日期: {{ product.harvestDate }}</p>
                    </div>
                    <el-button size="small" type="primary" class="mt-3 w-full" @click="goToTrace(product)">查看溯源</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- LOGS_ADMIN专属：服务记录 -->
            <el-tab-pane v-if="profile.user.role === 'LOGS_ADMIN'" label="📦 服务记录" name="logistics">
              <div v-if="logistics.length === 0 && !logisticsLoading" class="py-12 text-center text-gray-400">
                暂无服务记录
              </div>
              <div v-else class="relative">
                <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-green-200"></div>
                <div class="space-y-4">
                  <div v-for="log in logistics" :key="log.id" class="relative pl-10">
                    <div class="absolute left-2 w-4 h-4 rounded-full bg-green-500 border-2 border-white shadow"></div>
                    <div class="bg-gray-50 rounded-xl p-4">
                      <div class="flex items-center justify-between mb-2">
                        <span class="font-medium text-gray-800">{{ log.statusDesc }}</span>
                        <span class="text-xs text-gray-400">{{ log.recordedAt }}</span>
                      </div>
                      <p class="text-sm text-gray-600">📍 {{ log.location }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.realName" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.signature" type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="最多100字" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import FollowButton from '@/components/FollowButton.vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const userId = computed(() => route.params.id)
const profile = ref(null)
const profileLoading = ref(false)
const activeTab = ref('posts')

const avatarLetter = computed(() => {
  if (!profile.value) return '?'
  return profile.value.user.realName?.charAt(0) || profile.value.user.username?.charAt(0) || '?'
})

const roleLabel = computed(() => {
  const map = {
    'USER': '普通用户',
    'FARMER': '农户',
    'LOGS_ADMIN': '物流管理员',
    'SYS_ADMIN': '系统管理员'
  }
  return map[profile.value?.user.role] || '未知'
})

const roleTagType = computed(() => {
  const map = {
    'USER': 'info',
    'FARMER': 'success',
    'LOGS_ADMIN': 'warning',
    'SYS_ADMIN': 'danger'
  }
  return map[profile.value?.user.role] || 'info'
})

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const res = await userApi.getProfile(userId.value)
    profile.value = res.data
  } catch (err) {
    console.error(err)
    ElMessage.error('加载用户信息失败')
  } finally {
    profileLoading.value = false
  }
}

const onFollowToggle = (data) => {
  if (profile.value) {
    profile.value.user.followerCount = data.followerCount
    if (profile.value.isOwner) {
      profile.value.user.followingCount = data.followingCount
    }
  }
}

// 帖子相关
const posts = ref([])
const postsLoading = ref(false)
const postsPage = ref(0)
const postsTotal = ref(0)
const postFilter = ref('all')
const postSort = ref('latest')

const loadPosts = async (page = 0) => {
  postsPage.value = page
  postsLoading.value = true
  try {
    const res = await userApi.getUserPosts(userId.value, {
      page,
      size: 20,
      sort: postSort.value,
      filter: postFilter.value
    })
    posts.value = res.data.content
    postsTotal.value = res.data.totalElements
  } catch (err) {
    console.error(err)
  } finally {
    postsLoading.value = false
  }
}

// 收藏农友
const following = ref([])
const followingLoading = ref(false)
const followingPage = ref(0)
const followingTotal = ref(0)

const loadFollowing = async (page = 0) => {
  followingPage.value = page
  followingLoading.value = true
  try {
    const res = await userApi.getFollowing(userId.value, { page, size: 20 })
    following.value = res.data.content
    followingTotal.value = res.data.totalElements
  } catch (err) {
    console.error(err)
  } finally {
    followingLoading.value = false
  }
}

// 支持者
const followers = ref([])
const followersLoading = ref(false)
const followersPage = ref(0)
const followersTotal = ref(0)

const loadFollowers = async (page = 0) => {
  followersPage.value = page
  followersLoading.value = true
  try {
    const res = await userApi.getFollowers(userId.value, { page, size: 20 })
    followers.value = res.data.content
    followersTotal.value = res.data.totalElements
  } catch (err) {
    console.error(err)
  } finally {
    followersLoading.value = false
  }
}

// 产品
const products = ref([])
const productsLoading = ref(false)

const loadProducts = async () => {
  productsLoading.value = true
  try {
    const res = await userApi.getUserProducts(userId.value)
    products.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    productsLoading.value = false
  }
}

// 物流记录
const logistics = ref([])
const logisticsLoading = ref(false)

const loadLogistics = async () => {
  logisticsLoading.value = true
  try {
    const res = await userApi.getUserLogistics(userId.value, { page: 0, size: 100 })
    logistics.value = res.data.content
  } catch (err) {
    console.error(err)
  } finally {
    logisticsLoading.value = false
  }
}

const handleTabChange = (tab) => {
  switch (tab) {
    case 'posts':
      if (posts.value.length === 0) loadPosts()
      break
    case 'following':
      if (following.value.length === 0) loadFollowing()
      break
    case 'followers':
      if (followers.value.length === 0) loadFollowers()
      break
    case 'products':
      if (products.value.length === 0) loadProducts()
      break
    case 'logistics':
      if (logistics.value.length === 0) loadLogistics()
      break
  }
}

const goToTrace = (product) => {
  ElMessage.info('跳转到溯源查询页面')
}

// 编辑资料
const showEditDialog = ref(false)
const editLoading = ref(false)
const editForm = ref({
  realName: '',
  signature: '',
  phone: ''
})

const saveProfile = async () => {
  editLoading.value = true
  try {
    const res = await userApi.updateProfile(editForm.value)
    profile.value.user = res.data
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (err) {
    console.error(err)
  } finally {
    editLoading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadProfile()
  if (store.userId) {
    editForm.value = {
      realName: store.userInfo?.realName || '',
      signature: '',
      phone: store.userInfo?.phone || ''
    }
  }
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
