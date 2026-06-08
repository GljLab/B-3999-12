<template>
  <div class="bg-white rounded-2xl shadow-sm p-6 min-h-full">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">🏅 证书统计</h2>
    </div>

    <div v-if="products.length === 0 && !loading" class="py-16">
      <el-empty description="暂无产品证书数据" />
    </div>

    <div v-else>
      <el-card shadow="never" class="border border-green-100">
        <el-table :data="products" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="productName" label="产品名称" min-width="160">
            <template #default="{ row }">
              <span class="font-medium text-gray-800">{{ row.productName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.category" size="small" type="success">{{ row.category }}</el-tag>
              <span v-else class="text-gray-400 text-sm">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="certificateCount" label="证书数量" width="140" align="center">
            <template #default="{ row }">
              <span
                class="font-bold text-lg"
                :class="row.certificateCount >= 10 ? 'text-green-600' : row.certificateCount >= 5 ? 'text-blue-600' : 'text-gray-400'"
              >
                {{ row.certificateCount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="`${currentProduct.productName} - 证书用户列表`"
      width="720px"
      destroy-on-close
    >
      <el-table :data="users" stripe v-loading="usersLoading" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120">
          <template #default="{ row }">
            {{ row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="certificateNo" label="证书编号" min-width="200">
          <template #default="{ row }">
            <span class="font-mono text-sm text-green-700">{{ row.certificateNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="生成时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div v-if="usersTotal > usersPageSize" class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="usersPage"
          :page-size="usersPageSize"
          :total="usersTotal"
          layout="prev, pager, next, total"
          @current-change="loadUsers"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { certificateApi } from '@/api'

const products = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const currentProduct = ref({})
const users = ref([])
const usersLoading = ref(false)
const usersPage = ref(1)
const usersPageSize = 10
const usersTotal = ref(0)

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await certificateApi.getFarmerProductStats()
    products.value = res.data
  } finally {
    loading.value = false
  }
}

const openDetail = (product) => {
  currentProduct.value = product
  usersPage.value = 1
  dialogVisible.value = true
  loadUsers()
}

const loadUsers = async () => {
  usersLoading.value = true
  try {
    const res = await certificateApi.getCertificateUsers(currentProduct.value.productId, {
      page: usersPage.value - 1,
      size: usersPageSize
    })
    users.value = res.data.content
    usersTotal.value = res.data.totalElements
  } finally {
    usersLoading.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
:deep(.el-card__body) {
  padding: 0;
}
</style>
