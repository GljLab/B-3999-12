<template>
  <div class="min-h-screen bg-gray-50 py-6 px-4">
    <div class="max-w-5xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-800">📜 生成溯源证书</h1>
        <p class="text-gray-500 mt-1">为您的农产品生成官方溯源证书，支持单个或批量生成</p>
      </div>

      <el-steps :active="currentStep" finish-status="success" align-center class="mb-8">
        <el-step title="选择产品" description="输入溯源码添加产品" />
        <el-step title="选择模板" description="挑选证书样式" />
        <el-step title="预览生成" description="确认并生成证书" />
      </el-steps>

      <div v-if="currentStep === 0">
        <el-card shadow="never" class="rounded-2xl">
          <template #header>
            <div class="flex items-center gap-2">
              <span class="text-lg font-bold text-gray-800">🔍 查询并添加产品</span>
            </div>
          </template>

          <div class="flex gap-3 mb-6">
            <el-input
              v-model="traceCodeInput"
              placeholder="请输入溯源码 (如: TRC-AKAPPLE-231015-0001A)"
              size="large"
              clearable
              @keyup.enter="queryAndAdd"
              class="flex-1"
            />
            <el-button type="primary" size="large" @click="queryAndAdd" :loading="querying">
              查询添加
            </el-button>
          </div>

          <div v-if="selectedProducts.length === 0" class="text-center py-12 text-gray-400">
            <el-icon class="text-5xl mb-3"><Box /></el-icon>
            <p class="text-lg">暂无选中产品</p>
            <p class="text-sm mt-1">请在上方输入溯源码查询并添加产品</p>
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="(item, idx) in selectedProducts"
              :key="item.traceCode"
              class="flex items-center gap-4 p-4 bg-gray-50 rounded-xl border border-gray-100 hover:border-green-200 transition"
            >
              <div class="w-16 h-16 flex-shrink-0 rounded-lg overflow-hidden bg-gray-200">
                <img v-if="item.product.imageUrl" :src="getImageUrl(item.product.imageUrl)" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-xl">🌾</div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="font-bold text-gray-800 truncate">{{ item.product.productName }}</div>
                <div class="flex gap-2 mt-1">
                  <el-tag size="small" type="success">{{ item.product.category }}</el-tag>
                  <el-tag size="small" type="info">{{ item.product.origin }}</el-tag>
                </div>
                <div class="text-xs text-gray-400 mt-1 font-mono">溯源码: {{ item.traceCode }}</div>
              </div>
              <el-button type="danger" text @click="removeProduct(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <div class="flex justify-end mt-6">
            <el-button type="primary" @click="goToStep(1)" :disabled="selectedProducts.length === 0">
              下一步：选择模板
            </el-button>
          </div>
        </el-card>
      </div>

      <div v-if="currentStep === 1">
        <el-card shadow="never" class="rounded-2xl">
          <template #header>
            <div class="flex items-center gap-2">
              <span class="text-lg font-bold text-gray-800">🎨 选择证书模板</span>
            </div>
          </template>

          <div class="mb-6">
            <div class="text-sm text-gray-500 mb-3">模板应用方式</div>
            <el-radio-group v-model="templateMode">
              <el-radio value="unified">统一模板</el-radio>
              <el-radio value="individual">逐产品指定</el-radio>
            </el-radio-group>
          </div>

          <div v-if="templateMode === 'unified'">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div
                v-for="tpl in templates"
                :key="tpl.key"
                @click="unifiedTemplate = tpl.key"
                :class="[
                  'cursor-pointer rounded-xl border-2 p-4 transition hover:shadow-md',
                  unifiedTemplate === tpl.key ? 'border-green-500 bg-green-50 shadow-md' : 'border-gray-200 bg-white'
                ]"
              >
                <div :class="['w-full h-28 rounded-lg mb-3 flex items-center justify-center text-2xl', tpl.previewBg]">
                  <span :class="tpl.previewIcon">{{ tpl.previewEmoji }}</span>
                </div>
                <div class="text-center">
                  <div class="font-bold text-gray-800">{{ tpl.label }}</div>
                  <div class="text-xs text-gray-500 mt-1">{{ tpl.desc }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="(item, idx) in selectedProducts"
              :key="item.traceCode"
              class="p-4 bg-gray-50 rounded-xl border border-gray-100"
            >
              <div class="font-bold text-gray-800 mb-3">{{ item.product.productName }}</div>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div
                  v-for="tpl in templates"
                  :key="tpl.key"
                  @click="item.template = tpl.key"
                  :class="[
                    'cursor-pointer rounded-lg border-2 p-3 transition text-center',
                    item.template === tpl.key ? 'border-green-500 bg-green-50' : 'border-gray-200 bg-white'
                  ]"
                >
                  <div :class="['w-full h-14 rounded flex items-center justify-center text-lg', tpl.previewBg]">
                    {{ tpl.previewEmoji }}
                  </div>
                  <div class="text-sm font-medium mt-1">{{ tpl.label }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-between mt-6">
            <el-button @click="goToStep(0)">上一步</el-button>
            <el-button type="primary" @click="goToStep(2)" :disabled="!hasValidTemplate">
              下一步：预览生成
            </el-button>
          </div>
        </el-card>
      </div>

      <div v-if="currentStep === 2">
        <el-card shadow="never" class="rounded-2xl">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="text-lg font-bold text-gray-800">👁️ 预览与生成</span>
              <el-button type="primary" plain size="small" @click="openPreviewDialog">
                <el-icon class="mr-1"><View /></el-icon>放大预览
              </el-button>
            </div>
          </template>

          <div class="space-y-4">
            <div
              v-for="(item, idx) in selectedProducts"
              :key="item.traceCode"
              class="p-4 bg-gray-50 rounded-xl border border-gray-100"
            >
              <div class="flex items-center gap-3 mb-3">
                <el-tag type="success">{{ getTemplateLabel(item.template || unifiedTemplate) }}</el-tag>
                <span class="font-bold text-gray-800">{{ item.product.productName }}</span>
                <span class="text-gray-400 text-sm">/ {{ item.product.origin }}</span>
              </div>
              <div class="bg-white rounded-lg border border-gray-200 p-6 shadow-inner">
                <component
                  :is="getTemplateComponent(item.template || unifiedTemplate)"
                  :product="item.product"
                  :trace-code="item.traceCode"
                  :template="item.template || unifiedTemplate"
                />
              </div>
            </div>
          </div>

          <div class="flex justify-between mt-6">
            <el-button @click="goToStep(1)">上一步</el-button>
            <el-button type="success" size="large" @click="handleGenerate" :loading="generating">
              <el-icon class="mr-1"><Stamp /></el-icon>
              {{ selectedProducts.length > 1 ? '批量生成证书' : '生成证书' }}
            </el-button>
          </div>
        </el-card>
      </div>

      <el-dialog v-model="previewDialogVisible" title="证书预览" width="800px" top="5vh">
        <div v-if="previewItem" class="bg-white rounded-lg border border-gray-200 p-8">
          <component
            :is="getTemplateComponent(previewItem.template || unifiedTemplate)"
            :product="previewItem.product"
            :trace-code="previewItem.traceCode"
            :template="previewItem.template || unifiedTemplate"
          />
        </div>
        <div class="flex justify-center gap-4 mt-4" v-if="selectedProducts.length > 1">
          <el-button :disabled="previewIndex === 0" @click="previewIndex--">上一个</el-button>
          <span class="text-gray-500 self-center">{{ previewIndex + 1 }} / {{ selectedProducts.length }}</span>
          <el-button :disabled="previewIndex === selectedProducts.length - 1" @click="previewIndex++">下一个</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box, Delete, View, Stamp } from '@element-plus/icons-vue'
import api from '@/api'
import { certificateApi } from '@/api'

const router = useRouter()
const route = useRoute()

const currentStep = ref(0)
const traceCodeInput = ref('')
const querying = ref(false)
const generating = ref(false)
const selectedProducts = reactive([])
const templateMode = ref('unified')
const unifiedTemplate = ref('CLASSIC')
const previewDialogVisible = ref(false)
const previewIndex = ref(0)

const templates = [
  { key: 'CLASSIC', label: '经典', desc: '传统庄重风格，适合正式场合', previewBg: 'bg-amber-50 border border-amber-200', previewEmoji: '📜' },
  { key: 'MINIMAL', label: '简约', desc: '简洁现代设计，突出核心信息', previewBg: 'bg-gray-50 border border-gray-200', previewEmoji: '✨' },
  { key: 'CHINESE', label: '国风', desc: '中式传统纹样，文化韵味浓厚', previewBg: 'bg-red-50 border border-red-200', previewEmoji: '🏯' },
  { key: 'TECH', label: '科技', desc: '科技感设计，突出数据溯源', previewBg: 'bg-blue-50 border border-blue-200', previewEmoji: '🔬' }
]

const templateComponents = {
  CLASSIC: defineAsyncComponent(() => import('@/components/certificate/ClassicTemplate.vue')),
  MINIMAL: defineAsyncComponent(() => import('@/components/certificate/MinimalTemplate.vue')),
  CHINESE: defineAsyncComponent(() => import('@/components/certificate/ChineseTemplate.vue')),
  TECH: defineAsyncComponent(() => import('@/components/certificate/TechTemplate.vue'))
}

const previewItem = computed(() => selectedProducts[previewIndex.value] || null)

const hasValidTemplate = computed(() => {
  if (templateMode.value === 'unified') return !!unifiedTemplate.value
  return selectedProducts.every(item => !!item.template)
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const getTemplateLabel = (key) => {
  const tpl = templates.find(t => t.key === key)
  return tpl ? tpl.label : key
}

const getTemplateComponent = (key) => {
  return templateComponents[key] || templateComponents.CLASSIC
}

const goToStep = (step) => {
  currentStep.value = step
}

const queryAndAdd = async () => {
  const code = traceCodeInput.value.trim()
  if (!code) {
    ElMessage.warning('请输入溯源码')
    return
  }
  if (selectedProducts.some(p => p.traceCode === code)) {
    ElMessage.warning('该溯源码已添加')
    return
  }
  querying.value = true
  try {
    const res = await api.get(`/public/trace/${code}`)
    selectedProducts.push({
      traceCode: code,
      product: res.data.product,
      batch: res.data.batch,
      logistics: res.data.logistics,
      spec: res.data.spec,
      template: templateMode.value === 'unified' ? unifiedTemplate.value : 'CLASSIC'
    })
    traceCodeInput.value = ''
    ElMessage.success('产品添加成功')
  } catch (e) {
    ElMessage.error('查询失败，请检查溯源码')
  } finally {
    querying.value = false
  }
}

const removeProduct = (idx) => {
  selectedProducts.splice(idx, 1)
}

const openPreviewDialog = () => {
  previewIndex.value = 0
  previewDialogVisible.value = true
}

const handleGenerate = async () => {
  generating.value = true
  try {
    if (selectedProducts.length === 1) {
      const item = selectedProducts[0]
      const tplKey = item.template || unifiedTemplate.value
      await certificateApi.generate({
        traceCode: item.traceCode,
        productId: item.product.id,
        template: tplKey
      })
    } else {
      const items = selectedProducts.map(item => ({
        traceCode: item.traceCode,
        productId: item.product.id,
        template: item.template || unifiedTemplate.value
      }))
      await certificateApi.batchGenerate({ items })
    }
    ElMessage.success('证书生成成功！')
    router.push('/certificates')
  } catch (e) {
    ElMessage.error('证书生成失败，请重试')
  } finally {
    generating.value = false
  }
}

onMounted(() => {
  if (route.query.traceCode) {
    traceCodeInput.value = route.query.traceCode
    queryAndAdd()
  }
})
</script>

<style scoped>
:deep(.el-step__title.is-finish) {
  color: #10b981;
}
:deep(.el-step__head.is-finish) {
  color: #10b981;
  border-color: #10b981;
}
:deep(.el-step__head.is-process) {
  color: #10b981;
  border-color: #10b981;
}
</style>
