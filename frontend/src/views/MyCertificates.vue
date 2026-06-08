<template>
  <div class="bg-white rounded-2xl shadow-sm p-6 min-h-full">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-gray-800">📜 我的证书</h2>
      <router-link to="/certificate/generate">
        <el-button type="primary">+ 生成证书</el-button>
      </router-link>
    </div>

    <div v-if="certificates.length === 0 && !loading" class="py-16">
      <el-empty description="暂无证书，快去生成第一张吧" />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
      <el-card
        v-for="cert in certificates"
        :key="cert.id"
        shadow="hover"
        class="cursor-pointer certificate-card"
        @click="router.push(`/certificate/${cert.id}`)"
      >
        <div class="flex gap-4">
          <div class="w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden border border-gray-200 bg-gray-50">
            <img
              v-if="cert.productImageUrl"
              :src="getImageUrl(cert.productImageUrl)"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
              <el-icon class="text-4xl"><Picture /></el-icon>
            </div>
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="font-bold text-gray-800 truncate">{{ cert.productName }}</h3>
            <div class="flex flex-wrap gap-1.5 mt-2">
              <el-tag size="small" type="success">{{ cert.productCategory }}</el-tag>
              <el-tag size="small" :type="templateTagType(cert.templateType)">{{ templateLabel(cert.templateType) }}</el-tag>
            </div>
            <p class="text-xs text-gray-500 mt-1.5">产地: {{ cert.productOrigin }}</p>
            <p class="text-xs text-gray-400 mt-1 font-mono">编号: {{ cert.certificateNo }}</p>
          </div>
        </div>

        <div class="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
          <span class="text-xs text-gray-400">{{ formatDate(cert.createdAt) }}</span>
          <div class="flex gap-1" @click.stop>
            <el-button size="small" text type="primary" @click="router.push(`/certificate/${cert.id}`)">查看</el-button>
            <el-button size="small" text type="success" @click="openShareDialog(cert)">分享</el-button>
            <el-button size="small" text @click="openDownloadDialog(cert)">下载</el-button>
            <el-popconfirm title="确定要删除此证书吗？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(cert.id)">
              <template #reference>
                <el-button size="small" text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="total > pageSize" class="flex justify-end mt-6">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="loadCertificates"
      />
    </div>

    <el-dialog v-model="shareDialogVisible" title="分享证书" width="460px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="分享方式">
          <el-radio-group v-model="shareForm.shareType">
            <el-radio value="COMMUNITY">分享到社区</el-radio>
            <el-radio value="QRCODE">二维码</el-radio>
            <el-radio value="POSTER">海报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="shareForm.shareType === 'COMMUNITY'" label="分享标题">
          <el-input v-model="shareForm.postTitle" placeholder="请输入分享标题（可选）" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shareLoading" @click="handleShare">确认分享</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="downloadDialogVisible" title="下载证书" width="520px" destroy-on-close>
      <div class="text-center">
        <p class="text-gray-500 mb-4">请截图保存以下证书信息，或使用浏览器打印功能导出为 PDF。</p>
        <div class="bg-gradient-to-br from-green-50 to-white border-2 border-green-200 rounded-xl p-6 text-left">
          <div class="text-center mb-4">
            <h3 class="text-xl font-bold text-green-800">农产品溯源证书</h3>
            <div class="w-16 h-0.5 bg-green-500 mx-auto mt-2"></div>
          </div>
          <div class="space-y-2 text-sm">
            <p><span class="text-gray-500">产品名称：</span><span class="font-medium text-gray-800">{{ downloadCert?.productName }}</span></p>
            <p><span class="text-gray-500">证书编号：</span><span class="font-mono text-green-700">{{ downloadCert?.certificateNo }}</span></p>
            <p><span class="text-gray-500">产品分类：</span>{{ downloadCert?.productCategory }}</p>
            <p><span class="text-gray-500">产地：</span>{{ downloadCert?.productOrigin }}</p>
            <p><span class="text-gray-500">模板类型：</span>{{ templateLabel(downloadCert?.templateType) }}</p>
            <p><span class="text-gray-500">颁发时间：</span>{{ formatDate(downloadCert?.createdAt) }}</p>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { certificateApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()

const certificates = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 9
const total = ref(0)

const shareDialogVisible = ref(false)
const shareLoading = ref(false)
const shareForm = ref({ certificateId: null, shareType: 'COMMUNITY', postTitle: '' })

const downloadDialogVisible = ref(false)
const downloadCert = ref(null)

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

const loadCertificates = async () => {
  loading.value = true
  try {
    const res = await certificateApi.getMy({ page: page.value - 1, size: pageSize })
    certificates.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await certificateApi.delete(id)
    ElMessage.success('证书已删除')
    await loadCertificates()
  } catch (e) {
    // error handled by interceptor
  }
}

const openShareDialog = (cert) => {
  shareForm.value = { certificateId: cert.id, shareType: 'COMMUNITY', postTitle: '' }
  shareDialogVisible.value = true
}

const handleShare = async () => {
  shareLoading.value = true
  try {
    await certificateApi.share(shareForm.value)
    ElMessage.success('分享成功')
    shareDialogVisible.value = false
    await loadCertificates()
  } catch (e) {
    // error handled by interceptor
  } finally {
    shareLoading.value = false
  }
}

const openDownloadDialog = (cert) => {
  downloadCert.value = cert
  downloadDialogVisible.value = true
}

onMounted(() => {
  loadCertificates()
})
</script>

<style scoped>
.certificate-card {
  transition: transform 0.2s, box-shadow 0.2s;
}
.certificate-card:hover {
  transform: translateY(-2px);
}
</style>
