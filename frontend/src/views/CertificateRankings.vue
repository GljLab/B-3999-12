<template>
  <div class="p-6">
    <el-card shadow="never" class="rounded-xl">
      <template #header>
        <div class="flex items-center gap-2">
          <span class="text-xl font-bold text-green-700">🏆 证书排行榜</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="ranking-tabs">
        <el-tab-pane label="最受信赖产品TOP20" name="products">
          <el-table
            v-loading="productLoading"
            :data="productList"
            stripe
            class="w-full"
            :header-cell-style="{ background: '#f0fdf4', color: '#166534', fontWeight: 'bold' }"
          >
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <span class="text-lg">
                  {{ $index === 0 ? '🥇' : $index === 1 ? '🥈' : $index === 2 ? '🥉' : $index + 1 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="产品名称" min-width="160" />
            <el-table-column label="分类" width="120" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="origin" label="产地" width="140" />
            <el-table-column label="证书数量" width="120" align="center">
              <template #default="{ row }">
                <span class="text-lg font-bold text-green-700">{{ row.certificateCount }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!productLoading && productList.length === 0" description="暂无产品排行数据" />
        </el-tab-pane>

        <el-tab-pane label="最受欢迎农户TOP20" name="farmers">
          <el-table
            v-loading="farmerLoading"
            :data="farmerList"
            stripe
            class="w-full"
            :header-cell-style="{ background: '#f0fdf4', color: '#166534', fontWeight: 'bold' }"
          >
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <span class="text-lg">
                  {{ $index === 0 ? '🥇' : $index === 1 ? '🥈' : $index === 2 ? '🥉' : $index + 1 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="farmerName" label="农户名称" min-width="200" />
            <el-table-column label="证书数量" width="120" align="center">
              <template #default="{ row }">
                <span class="text-lg font-bold text-green-700">{{ row.certificateCount }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!farmerLoading && farmerList.length === 0" description="暂无农户排行数据" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { certificateApi } from '@/api'

const activeTab = ref('products')
const productLoading = ref(false)
const farmerLoading = ref(false)
const productList = ref([])
const farmerList = ref([])

const fetchProductRankings = async () => {
  productLoading.value = true
  try {
    const res = await certificateApi.productRankings()
    productList.value = res.data || []
  } finally {
    productLoading.value = false
  }
}

const fetchFarmerRankings = async () => {
  farmerLoading.value = true
  try {
    const res = await certificateApi.farmerRankings()
    farmerList.value = res.data || []
  } finally {
    farmerLoading.value = false
  }
}

onMounted(() => {
  fetchProductRankings()
  fetchFarmerRankings()
})
</script>

<style scoped>
.ranking-tabs :deep(.el-tabs__item.is-active) {
  color: #16a34a;
}
.ranking-tabs :deep(.el-tabs__active-bar) {
  background-color: #16a34a;
}
.ranking-tabs :deep(.el-tabs__item:hover) {
  color: #16a34a;
}
</style>
