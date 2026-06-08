<template>
  <div class="space-y-6">
    <div v-if="cert && cert.status === 0" class="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3">
      <span class="text-2xl">⚠️</span>
      <div>
        <p class="text-red-700 font-bold">此证书已被撤销</p>
        <p class="text-red-500 text-sm">该证书不再具有效力，请谨慎参考</p>
      </div>
    </div>

    <div v-if="!cert" class="bg-white rounded-2xl p-12 shadow-sm border border-gray-100 text-center">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold text-gray-800">📜 证书详情</h2>
          <el-button text @click="router.push('/certificates')" class="text-gray-500 hover:text-green-600">
            ← 返回列表
          </el-button>
        </div>

        <div class="flex justify-center mb-6">
          <div class="w-full max-w-2xl">
            <component :is="currentTemplate" :cert="cert" />
          </div>
        </div>

        <div class="flex flex-wrap gap-3 justify-center mt-6">
          <el-button type="primary" @click="shareDialogVisible = true">分享到社区</el-button>
          <el-button type="success" @click="handleQrcode">生成二维码</el-button>
          <el-button type="warning" @click="handlePoster">生成海报</el-button>
          <el-button @click="downloadDialogVisible = true">下载证书</el-button>
          <el-popconfirm title="确定要删除此证书吗？删除后不可恢复。" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete">
            <template #reference>
              <el-button type="danger">删除证书</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <div v-if="cert" class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <el-card shadow="never" class="rounded-2xl border border-gray-100">
        <template #header>
          <span class="font-bold text-gray-700">📊 统计数据</span>
        </template>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-gray-500">浏览次数</span>
            <span class="font-bold text-gray-800">{{ cert.viewCount ?? 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-500">分享次数</span>
            <span class="font-bold text-gray-800">{{ cert.shareCount ?? 0 }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-500">验证次数</span>
            <span class="font-bold text-gray-800">{{ cert.verifyCount ?? 0 }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-2xl border border-gray-100">
        <template #header>
          <span class="font-bold text-gray-700">📋 证书信息</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="证书编号">
            <span class="font-mono text-green-700">{{ cert.certificateNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="数字签名">
            <div class="flex items-center gap-2">
              <span class="font-mono text-xs text-gray-600 truncate max-w-[200px]">{{ cert.digitalSignature }}</span>
              <el-button size="small" text type="primary" @click="copySignature">复制</el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(cert.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="模板类型">
            <el-tag size="small" :type="templateTagType(cert.templateType)">{{ templateLabel(cert.templateType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="证书状态">
            <el-tag :type="cert.status === 0 ? 'danger' : 'success'" size="small">
              {{ cert.status === 0 ? '已撤销' : '有效' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <el-dialog v-model="shareDialogVisible" title="分享到社区" width="460px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="分享标题">
          <el-input v-model="postTitle" placeholder="请输入分享标题（可选）" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shareLoading" @click="handleShare">确认分享</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="downloadDialogVisible" title="下载证书" width="640px" destroy-on-close>
      <div class="text-center">
        <p class="text-gray-500 mb-4">请截图保存以下证书，或使用浏览器打印功能导出为 PDF。</p>
        <div class="flex justify-center">
          <div class="w-full max-w-lg">
            <component :is="currentTemplate" :cert="cert" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="downloadDialogVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineAsyncComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { certificateApi } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const cert = ref(null)
const shareDialogVisible = ref(false)
const downloadDialogVisible = ref(false)
const postTitle = ref('')
const shareLoading = ref(false)

const ClassicTemplate = defineAsyncComponent(() => import('@/components/certificate/ClassicTemplate.vue'))
const MinimalTemplate = defineAsyncComponent(() => import('@/components/certificate/MinimalTemplate.vue'))
const ChineseTemplate = defineAsyncComponent(() => import('@/components/certificate/ChineseTemplate.vue'))
const TechTemplate = defineAsyncComponent(() => import('@/components/certificate/TechTemplate.vue'))

const templateMap = {
  CLASSIC: ClassicTemplate,
  MINIMAL: MinimalTemplate,
  CHINESE: ChineseTemplate,
  TECH: TechTemplate
}

const currentTemplate = computed(() => {
  return templateMap[cert.value?.templateType] || ClassicTemplate
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const templateTagType = (type) => {
  const map = { CLASSIC: 'warning', MINIMAL: '', CHINESE: 'danger', TECH: 'success' }
  return map[type] || 'info'
}

const templateLabel = (type) => {
  const map = { CLASSIC: '经典', MINIMAL: '简约', CHINESE: '中式', TECH: '科技' }
  return map[type] || type || '未知'
}

const copySignature = () => {
  if (!cert.value?.digitalSignature) return
  navigator.clipboard.writeText(cert.value.digitalSignature).then(() => {
    ElMessage.success('签名已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动选择复制')
  })
}

const loadDetail = async () => {
  try {
    const res = await certificateApi.getDetail(route.params.id)
    cert.value = res.data
  } catch (e) {
    ElMessage.error('证书加载失败')
  }
}

const handleShare = async () => {
  shareLoading.value = true
  try {
    await certificateApi.share({
      certificateId: cert.value.id,
      shareType: 'COMMUNITY',
      postTitle: postTitle.value
    })
    ElMessage.success('分享成功')
    shareDialogVisible.value = false
    postTitle.value = ''
  } catch (e) {
    // handled by interceptor
  } finally {
    shareLoading.value = false
  }
}

const handleQrcode = async () => {
  try {
    await certificateApi.share({
      certificateId: cert.value.id,
      shareType: 'QRCODE'
    })
    ElMessage.success('二维码生成成功')
  } catch (e) {
    // handled by interceptor
  }
}

const handlePoster = async () => {
  try {
    await certificateApi.share({
      certificateId: cert.value.id,
      shareType: 'POSTER'
    })
    ElMessage.success('海报生成成功')
  } catch (e) {
    // handled by interceptor
  }
}

const handleDelete = async () => {
  try {
    await certificateApi.delete(cert.value.id)
    ElMessage.success('证书已删除')
    router.push('/certificates')
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
</style>
