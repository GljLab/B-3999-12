<template>
  <div class="p-6 space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">证书统计</h2>

    <el-skeleton :loading="loading" animated :rows="5">
      <template #default>
        <div v-if="!stats" class="py-10">
          <el-empty description="暂无统计数据" />
        </div>

        <template v-else>
          <div class="grid grid-cols-4 gap-5">
            <el-card shadow="hover" class="stat-card stat-card--green">
              <div class="stat-card__label">证书总数</div>
              <div class="stat-card__value">{{ stats.totalCertificates }}</div>
            </el-card>
            <el-card shadow="hover" class="stat-card stat-card--blue">
              <div class="stat-card__label">今日新增</div>
              <div class="stat-card__value">{{ stats.todayCertificates }}</div>
            </el-card>
            <el-card shadow="hover" class="stat-card stat-card--orange">
              <div class="stat-card__label">本周新增</div>
              <div class="stat-card__value">{{ stats.weekCertificates }}</div>
            </el-card>
            <el-card shadow="hover" class="stat-card stat-card--purple">
              <div class="stat-card__label">本月新增</div>
              <div class="stat-card__value">{{ stats.monthCertificates }}</div>
            </el-card>
          </div>

          <el-card shadow="hover">
            <template #header>
              <span class="font-semibold text-gray-700">每日趋势（近30天）</span>
            </template>
            <div v-if="dailyTrend.length" class="daily-chart">
              <div class="daily-chart__bars">
                <div
                  v-for="item in dailyTrend"
                  :key="item.date"
                  class="daily-chart__bar-wrapper"
                >
                  <div
                    class="daily-chart__bar"
                    :style="{ height: barHeight(item.count) + '%' }"
                    :title="`${item.date}: ${item.count}`"
                  ></div>
                  <span class="daily-chart__label">{{ formatDate(item.date) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无趋势数据" :image-size="60" />
          </el-card>

          <div class="grid grid-cols-2 gap-5">
            <el-card shadow="hover">
              <template #header>
                <span class="font-semibold text-gray-700">模板使用统计</span>
              </template>
              <div v-if="stats.templateStats && stats.templateStats.length" class="space-y-3">
                <div
                  v-for="item in stats.templateStats"
                  :key="item.templateType"
                  class="template-bar"
                >
                  <div class="template-bar__header">
                    <el-tag :type="templateTagType(item.templateType)" effect="dark" size="small">
                      {{ item.templateType }}
                    </el-tag>
                    <span class="template-bar__pct">{{ templatePct(item.count) }}%</span>
                  </div>
                  <div class="template-bar__track">
                    <div
                      class="template-bar__fill"
                      :class="'template-bar__fill--' + templateTagType(item.templateType)"
                      :style="{ width: templatePct(item.count) + '%' }"
                    ></div>
                  </div>
                  <span class="template-bar__count">{{ item.count }} 次</span>
                </div>
              </div>
              <el-empty v-else description="暂无模板数据" :image-size="60" />
            </el-card>

            <el-card shadow="hover">
              <template #header>
                <span class="font-semibold text-gray-700">分享与浏览统计</span>
              </template>
              <div class="space-y-5">
                <div>
                  <div class="text-sm text-gray-500 mb-2">分享统计</div>
                  <div class="flex items-center gap-4 mb-2">
                    <span class="text-2xl font-bold text-green-600">{{ stats.shareStats?.total || 0 }}</span>
                    <span class="text-sm text-gray-400">总分享次数</span>
                  </div>
                  <div class="flex gap-3">
                    <el-tag type="success" effect="plain">
                      社区 {{ stats.shareStats?.community || 0 }}
                    </el-tag>
                    <el-tag type="warning" effect="plain">
                      二维码 {{ stats.shareStats?.qrcode || 0 }}
                    </el-tag>
                    <el-tag type="danger" effect="plain">
                      海报 {{ stats.shareStats?.poster || 0 }}
                    </el-tag>
                  </div>
                </div>
                <el-divider />
                <div>
                  <div class="text-sm text-gray-500 mb-2">浏览统计</div>
                  <div class="flex items-center gap-4 mb-2">
                    <span class="text-2xl font-bold text-blue-600">{{ stats.viewStats?.total || 0 }}</span>
                    <span class="text-sm text-gray-400">总浏览量</span>
                  </div>
                  <div class="flex gap-3">
                    <el-tag type="primary" effect="plain">
                      分享浏览 {{ stats.viewStats?.shareViews || 0 }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </template>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import certificateApi from '@/api'

const loading = ref(true)
const stats = ref(null)

const dailyTrend = computed(() => {
  if (!stats.value?.dailyTrend) return []
  return stats.value.dailyTrend.slice(-30)
})

const maxDailyCount = computed(() => {
  if (!dailyTrend.value.length) return 0
  return Math.max(...dailyTrend.value.map((d) => d.count), 1)
})

function barHeight(count) {
  return Math.max((count / maxDailyCount.value) * 100, 2)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(-5)
}

const tagTypes = ['success', 'primary', 'warning', 'danger', 'info']

function templateTagType(type) {
  let hash = 0
  for (let i = 0; i < type.length; i++) {
    hash = type.charCodeAt(i) + ((hash << 5) - hash)
  }
  return tagTypes[Math.abs(hash) % tagTypes.length]
}

function templatePct(count) {
  const total = stats.value?.templateStats?.reduce((s, t) => s + t.count, 0) || 0
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

onMounted(async () => {
  try {
    const res = await certificateApi.adminStats()
    stats.value = res.data || res
  } catch {
    stats.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 20px 24px;
}
.stat-card__label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 8px;
}
.stat-card__value {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
}
.stat-card--green :deep(.el-card__body) {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  border-radius: 8px;
}
.stat-card--blue :deep(.el-card__body) {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  border-radius: 8px;
}
.stat-card--orange :deep(.el-card__body) {
  background: linear-gradient(135deg, #fa709a, #fee140);
  border-radius: 8px;
}
.stat-card--purple :deep(.el-card__body) {
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
  border-radius: 8px;
}

.daily-chart {
  overflow-x: auto;
}
.daily-chart__bars {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 200px;
  padding-bottom: 28px;
  position: relative;
}
.daily-chart__bar-wrapper {
  flex: 1;
  min-width: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  position: relative;
}
.daily-chart__bar {
  width: 100%;
  max-width: 24px;
  background: linear-gradient(180deg, #4facfe, #00f2fe);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 2px;
}
.daily-chart__label {
  position: absolute;
  bottom: -24px;
  font-size: 10px;
  color: #999;
  white-space: nowrap;
  transform: rotate(-45deg);
  transform-origin: top center;
}

.template-bar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.template-bar__pct {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}
.template-bar__track {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}
.template-bar__fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
.template-bar__fill--success {
  background: linear-gradient(90deg, #43e97b, #38f9d7);
}
.template-bar__fill--primary {
  background: linear-gradient(90deg, #4facfe, #00f2fe);
}
.template-bar__fill--warning {
  background: linear-gradient(90deg, #fa709a, #fee140);
}
.template-bar__fill--danger {
  background: linear-gradient(90deg, #f5576c, #ff6a88);
}
.template-bar__fill--info {
  background: linear-gradient(90deg, #a18cd1, #fbc2eb);
}
.template-bar__count {
  font-size: 12px;
  color: #999;
}
</style>
