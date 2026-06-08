<template>
  <div class="min-h-screen bg-gray-50 py-8 px-4">
    <div class="max-w-2xl mx-auto">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-bold text-gray-800">🔐 证书验证</h1>
        <p class="text-gray-500 mt-1">输入证书编号，验证溯源证书真伪</p>
      </div>

      <el-card shadow="never" class="rounded-2xl mb-6">
        <div class="flex gap-3">
          <el-input
            v-model="certificateNo"
            placeholder="请输入证书编号"
            size="large"
            clearable
            @keyup.enter="handleVerify"
            class="flex-1"
          />
          <el-button type="primary" size="large" @click="handleVerify" :loading="verifying">
            验证
          </el-button>
        </div>
      </el-card>

      <div v-if="verifying" class="text-center py-12">
        <el-icon class="text-4xl text-green-500 animate-spin"><Loading /></el-icon>
        <p class="mt-4 text-gray-500">正在验证证书...</p>
      </div>

      <div v-else-if="verifyResult && verifyResult.valid">
        <el-card shadow="never" class="rounded-2xl mb-6">
          <el-result icon="success" title="验证通过" sub-title="该证书为有效溯源证书">
            <template #extra>
              <el-tag type="success" size="large" effect="dark">✓ 证书有效</el-tag>
            </template>
          </el-result>
        </el-card>

        <el-card shadow="never" class="rounded-2xl mb-6">
          <template #header>
            <div class="flex items-center gap-2">
              <span class="text-lg font-bold text-gray-800">📋 证书详情</span>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="证书编号">
              <span class="font-mono text-green-700 font-bold">{{ verifyResult.certificateNo }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="产品名称">{{ verifyResult.productName }}</el-descriptions-item>
            <el-descriptions-item label="产地">{{ verifyResult.origin }}</el-descriptions-item>
            <el-descriptions-item label="农户">{{ verifyResult.farmer }}</el-descriptions-item>
            <el-descriptions-item label="批次信息">{{ verifyResult.batchInfo || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="数字签名状态">
              <el-tag v-if="verifyResult.signatureValid" type="success" effect="dark">✓ 签名有效</el-tag>
              <el-tag v-else type="danger" effect="dark">⚠ 签名无效</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="logisticsTimeline.length > 0" shadow="never" class="rounded-2xl mb-6">
          <template #header>
            <div class="flex items-center gap-2">
              <span class="text-lg font-bold text-gray-800">🚚 物流轨迹</span>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="(item, idx) in logisticsTimeline"
              :key="idx"
              :timestamp="item.time"
              color="#10b981"
              size="large"
            >
              <div class="font-semibold text-gray-800">{{ item.location }}</div>
              <div class="text-green-600">{{ item.status }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card shadow="never" class="rounded-2xl mb-6">
          <template #header>
            <div class="flex items-center gap-2">
              <span class="text-lg font-bold text-gray-800">📊 互动统计</span>
            </div>
          </template>

          <div class="grid grid-cols-3 gap-4 text-center">
            <div class="bg-blue-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-blue-600">{{ verifyResult.viewCount || 0 }}</div>
              <div class="text-sm text-gray-500">浏览</div>
            </div>
            <div class="bg-green-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-green-600">{{ verifyResult.shareCount || 0 }}</div>
              <div class="text-sm text-gray-500">分享</div>
            </div>
            <div class="bg-purple-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-purple-600">{{ verifyResult.verifyCount || 0 }}</div>
              <div class="text-sm text-gray-500">验证</div>
            </div>
          </div>
        </el-card>

        <div v-if="verifyResult.traceCode" class="text-center">
          <el-button type="primary" link @click="goToTrace" class="text-lg">
            查看完整溯源信息 →
          </el-button>
        </div>
      </div>

      <div v-else-if="verifyResult && !verifyResult.valid">
        <el-card shadow="never" class="rounded-2xl">
          <el-result icon="error" title="验证失败" :sub-title="verifyResult.message || '该证书编号无效，请核实后重试'">
            <template #extra>
              <el-tag type="danger" size="large" effect="dark">✗ 证书无效</el-tag>
            </template>
          </el-result>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { certificateApi } from '@/api'

const route = useRoute()
const router = useRouter()

const certificateNo = ref('')
const verifying = ref(false)
const verifyResult = ref(null)

const logisticsTimeline = computed(() => {
  if (!verifyResult.value?.logisticsSummary) return []
  try {
    const data = typeof verifyResult.value.logisticsSummary === 'string'
      ? JSON.parse(verifyResult.value.logisticsSummary)
      : verifyResult.value.logisticsSummary
    return Array.isArray(data) ? data : []
  } catch {
    return []
  }
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const handleVerify = async () => {
  const no = certificateNo.value.trim()
  if (!no) {
    ElMessage.warning('请输入证书编号')
    return
  }
  verifying.value = true
  verifyResult.value = null
  try {
    const res = await certificateApi.verify(no)
    verifyResult.value = res.data
  } catch (e) {
    verifyResult.value = {
      valid: false,
      message: e.response?.data?.message || '证书验证失败，请检查编号是否正确'
    }
  } finally {
    verifying.value = false
  }
}

const goToTrace = () => {
  if (verifyResult.value?.traceCode) {
    router.push(`/trace/${verifyResult.value.traceCode}`)
  }
}

onMounted(() => {
  if (route.query.certNo) {
    certificateNo.value = route.query.certNo
    handleVerify()
  }
})
</script>

<style scoped>
:deep(.el-result__title p) {
  font-size: 22px;
}
</style>
