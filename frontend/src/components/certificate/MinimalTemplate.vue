<template>
  <div class="minimal-cert">
    <div class="cert-card">
      <div class="cert-top">
        <div class="cert-badge">
          <el-icon :size="16"><CircleCheck /></el-icon>
          <span>已认证</span>
        </div>
        <div class="cert-number">
          <span class="label">NO.</span>
          <span class="value">{{ cert.certificateNo }}</span>
        </div>
      </div>

      <div class="cert-hero">
        <div class="hero-img" v-if="cert.productImageUrl">
          <img :src="getImageUrl(cert.productImageUrl)" :alt="cert.productName" />
        </div>
        <div class="hero-img empty" v-else>
          <el-icon :size="40" color="#d0d0d0"><Picture /></el-icon>
        </div>
        <div class="hero-info">
          <h1 class="product-name">{{ cert.productName }}</h1>
          <div class="product-meta">
            <el-tag size="small" type="info">{{ cert.productCategory }}</el-tag>
            <el-tag size="small" type="warning" v-if="cert.qualityGrade">{{ cert.qualityGrade }}</el-tag>
          </div>
        </div>
      </div>

      <div class="cert-grid">
        <div class="grid-item">
          <div class="grid-label">产地</div>
          <div class="grid-value">{{ cert.productOrigin || '-' }}</div>
        </div>
        <div class="grid-item">
          <div class="grid-label">农户</div>
          <div class="grid-value">{{ cert.farmerName || '-' }}</div>
        </div>
        <div class="grid-item">
          <div class="grid-label">采摘日期</div>
          <div class="grid-value">{{ formatDate(cert.harvestDate) }}</div>
        </div>
        <div class="grid-item">
          <div class="grid-label">批次号</div>
          <div class="grid-value mono">{{ cert.batchNo || '-' }}</div>
        </div>
        <div class="grid-item">
          <div class="grid-label">生产日期</div>
          <div class="grid-value">{{ formatDate(cert.productionDate) }}</div>
        </div>
        <div class="grid-item">
          <div class="grid-label">溯源码</div>
          <div class="grid-value mono">{{ cert.traceCode || '-' }}</div>
        </div>
      </div>

      <div class="cert-desc" v-if="cert.productDescription">
        <p>{{ cert.productDescription }}</p>
      </div>

      <div class="cert-brand" v-if="cert.brandIntro">
        <div class="brand-header">
          <img v-if="cert.brandLogoUrl" :src="getImageUrl(cert.brandLogoUrl)" class="brand-logo" alt="" />
          <span class="brand-title">品牌故事</span>
        </div>
        <p class="brand-text">{{ cert.brandIntro }}</p>
      </div>

      <div class="cert-logistics" v-if="logistics.length">
        <h3 class="block-title">物流追溯</h3>
        <el-timeline class="mini-timeline">
          <el-timeline-item
            v-for="(item, idx) in logistics"
            :key="idx"
            :timestamp="item.time || item.recordedAt"
            color="#409eff"
            size="small"
          >
            {{ item.location || item.statusDesc }}
            <span v-if="item.statusDesc && item.location" class="text-gray-400 text-xs ml-2">{{ item.statusDesc }}</span>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="cert-bottom">
        <div class="qr-placeholder">
          <div class="qr-box">
            <el-icon :size="20"><Connection /></el-icon>
            <span>扫码验证</span>
          </div>
        </div>
        <div class="sig-block">
          <div class="sig-label">数字签名</div>
          <div class="sig-value">{{ cert.digitalSignature }}</div>
          <div class="sig-date">{{ formatDate(cert.createdAt) }}</div>
        </div>
      </div>

      <div class="cert-stats">
        <span><el-icon><View /></el-icon> {{ cert.viewCount || 0 }}</span>
        <span><el-icon><Share /></el-icon> {{ cert.shareCount || 0 }}</span>
        <span><el-icon><CircleCheck /></el-icon> {{ cert.verifyCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Picture, CircleCheck, Connection, View, Share } from '@element-plus/icons-vue'

const props = defineProps({
  cert: { type: Object, required: true }
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const logistics = computed(() => {
  if (!props.cert.logisticsSummary) return []
  try {
    return JSON.parse(props.cert.logisticsSummary)
  } catch {
    return []
  }
})
</script>

<style scoped>
.minimal-cert {
  background: #ffffff;
  font-family: -apple-system, 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding: 20px;
  min-height: 100%;
}

.cert-card {
  max-width: 680px;
  margin: 0 auto;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 28px 24px;
  background: #fff;
}

.cert-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cert-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f0f9eb;
  color: #67c23a;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.cert-number {
  font-size: 12px;
  color: #999;
}
.cert-number .label { color: #bbb; }
.cert-number .value { font-family: 'Courier New', monospace; font-weight: 600; color: #666; }

.cert-hero {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.hero-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #eee;
}
.hero-img img { width: 100%; height: 100%; object-fit: cover; }
.hero-img.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.product-meta { display: flex; gap: 6px; }

.cert-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 1px;
  background: #eee;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.grid-item {
  background: #fafafa;
  padding: 12px 14px;
}
.grid-label { font-size: 11px; color: #999; margin-bottom: 4px; }
.grid-value { font-size: 13px; color: #333; font-weight: 500; }
.grid-value.mono { font-family: 'Courier New', monospace; font-size: 12px; }

.cert-desc {
  padding: 14px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #666;
  line-height: 1.7;
}

.cert-brand {
  padding: 14px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.brand-logo { width: 20px; height: 20px; border-radius: 4px; object-fit: contain; }
.brand-title { font-size: 13px; font-weight: 600; color: #333; }
.brand-text { font-size: 12px; color: #888; line-height: 1.6; margin: 0; }

.block-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.mini-timeline {
  padding-left: 4px;
}

.cert-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.qr-box {
  width: 64px;
  height: 64px;
  border: 1px solid #ddd;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #999;
}
.qr-box span { font-size: 9px; }

.sig-block { text-align: right; }
.sig-label { font-size: 10px; color: #bbb; }
.sig-value {
  font-family: 'Courier New', monospace;
  font-size: 10px;
  color: #999;
  word-break: break-all;
  max-width: 180px;
  line-height: 1.4;
  margin: 4px 0;
}
.sig-date { font-size: 10px; color: #ccc; }

.cert-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 16px;
  font-size: 11px;
  color: #ccc;
}
.cert-stats span { display: flex; align-items: center; gap: 3px; }

@media (max-width: 640px) {
  .cert-grid { grid-template-columns: 1fr 1fr; }
  .cert-hero { flex-direction: column; align-items: center; text-align: center; }
  .hero-img { width: 80px; height: 80px; }
}
</style>
