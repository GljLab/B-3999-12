<template>
  <div class="classic-cert">
    <div class="classic-border">
      <div class="inner-border">
        <div class="cert-content">
          <div class="cert-header">
            <div class="header-deco left-deco"></div>
            <div class="header-center">
              <div class="cert-title">产品质量溯源证书</div>
              <div class="cert-subtitle">PRODUCT TRACEABILITY CERTIFICATE</div>
            </div>
            <div class="header-deco right-deco"></div>
          </div>

          <div class="cert-no">
            <span class="no-label">证书编号：</span>
            <span class="no-value">{{ cert.certificateNo }}</span>
          </div>

          <div class="cert-body">
            <div class="product-hero">
              <div class="product-img-wrap" v-if="cert.productImageUrl">
                <img :src="getImageUrl(cert.productImageUrl)" :alt="cert.productName" />
              </div>
              <div class="product-img-wrap placeholder" v-else>
                <el-icon :size="48" color="#c49a6c"><Picture /></el-icon>
              </div>
              <div class="product-main">
                <h2 class="product-name">{{ cert.productName }}</h2>
                <div class="product-tags">
                  <el-tag type="warning" effect="dark" size="small">{{ cert.productCategory }}</el-tag>
                  <el-tag effect="dark" size="small" v-if="cert.qualityGrade">{{ cert.qualityGrade }}</el-tag>
                </div>
                <p class="product-desc" v-if="cert.productDescription">{{ cert.productDescription }}</p>
              </div>
            </div>

            <el-divider>
              <el-icon color="#c49a6c"><Stamp /></el-icon>
            </el-divider>

            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon"><el-icon color="#c49a6c"><Location /></el-icon></div>
                <div class="info-text">
                  <span class="info-label">产地</span>
                  <span class="info-value">{{ cert.productOrigin }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon color="#c49a6c"><Calendar /></el-icon></div>
                <div class="info-text">
                  <span class="info-label">采摘日期</span>
                  <span class="info-value">{{ formatDate(cert.harvestDate) }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon color="#c49a6c"><Box /></el-icon></div>
                <div class="info-text">
                  <span class="info-label">批次号</span>
                  <span class="info-value">{{ cert.batchNo }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-icon"><el-icon color="#c49a6c"><Timer /></el-icon></div>
                <div class="info-text">
                  <span class="info-label">生产日期</span>
                  <span class="info-value">{{ formatDate(cert.productionDate) }}</span>
                </div>
              </div>
            </div>

            <div class="farmer-section" v-if="cert.farmerName">
              <h3 class="section-title">
                <el-icon class="mr-1" color="#c49a6c"><User /></el-icon>
                农户信息
              </h3>
              <div class="farmer-card">
                <div class="farmer-avatar" v-if="cert.farmPhotoUrl">
                  <img :src="getImageUrl(cert.farmPhotoUrl)" alt="农场照片" />
                </div>
                <div class="farmer-info">
                  <div class="farmer-name">{{ cert.farmerName }}</div>
                  <div class="farmer-brand" v-if="cert.brandIntro">
                    <img v-if="cert.brandLogoUrl" :src="getImageUrl(cert.brandLogoUrl)" class="brand-logo" alt="品牌Logo" />
                    <span>{{ cert.brandIntro }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="logistics-section" v-if="logistics.length">
              <h3 class="section-title">
                <el-icon class="mr-1" color="#c49a6c"><Van /></el-icon>
                物流追溯
              </h3>
              <el-timeline>
                <el-timeline-item
                  v-for="(item, idx) in logistics"
                  :key="idx"
                  :timestamp="item.time || item.recordedAt"
                  color="#c49a6c"
                >
                  <span class="logistics-location">{{ item.location || item.statusDesc }}</span>
                  <span class="logistics-desc" v-if="item.statusDesc && item.location">{{ item.statusDesc }}</span>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>

          <div class="cert-footer">
            <div class="qr-placeholder">
              <div class="qr-box">
                <el-icon :size="28" color="#c49a6c"><Connection /></el-icon>
                <span>扫码验证</span>
              </div>
            </div>
            <div class="sig-section">
              <div class="sig-label">数字签名</div>
              <div class="sig-value">{{ cert.digitalSignature }}</div>
              <div class="sig-date">签发日期：{{ formatDate(cert.createdAt) }}</div>
            </div>
            <div class="seal-wrap">
              <div class="seal">
                <div class="seal-text">溯源认证</div>
                <div class="seal-star">★</div>
              </div>
            </div>
          </div>

          <div class="stats-bar">
            <span><el-icon><View /></el-icon> {{ cert.viewCount || 0 }}</span>
            <span><el-icon><Share /></el-icon> {{ cert.shareCount || 0 }}</span>
            <span><el-icon><CircleCheck /></el-icon> {{ cert.verifyCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Picture, Stamp, Location, Calendar, Box, Timer, User, Van,
  Connection, View, Share, CircleCheck
} from '@element-plus/icons-vue'

const props = defineProps({
  cert: { type: Object, required: true }
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const formatDate = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
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
.classic-cert {
  --gold: #c49a6c;
  --gold-light: #e8d5b7;
  --cream: #fdf8f0;
  --ivory: #faf3e8;
  font-family: 'Georgia', 'SimSun', serif;
  background: linear-gradient(135deg, var(--cream), var(--ivory));
  padding: 24px;
  border-radius: 8px;
  min-height: 100%;
}

.classic-border {
  border: 3px solid var(--gold);
  border-radius: 8px;
  padding: 6px;
  background: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 4px,
    var(--gold-light) 4px,
    var(--gold-light) 5px
  );
}

.inner-border {
  border: 2px solid var(--gold);
  border-radius: 4px;
  background: var(--cream);
  padding: 32px 28px;
  position: relative;
}

.inner-border::before,
.inner-border::after {
  content: '❧';
  position: absolute;
  font-size: 32px;
  color: var(--gold);
  opacity: 0.5;
}
.inner-border::before { top: 8px; left: 12px; }
.inner-border::after { bottom: 8px; right: 12px; transform: rotate(180deg); }

.cert-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}

.header-deco {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--gold), transparent);
}

.cert-title {
  font-size: 28px;
  font-weight: bold;
  color: #5a3e2b;
  text-align: center;
  letter-spacing: 8px;
}

.cert-subtitle {
  font-size: 11px;
  color: var(--gold);
  text-align: center;
  letter-spacing: 3px;
  margin-top: 4px;
}

.cert-no {
  text-align: center;
  margin: 16px 0 24px;
  font-size: 13px;
  color: #8b7355;
}

.no-value {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  color: #5a3e2b;
}

.product-hero {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}

.product-img-wrap {
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid var(--gold-light);
}
.product-img-wrap img { width: 100%; height: 100%; object-fit: cover; }
.product-img-wrap.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ivory);
}

.product-name {
  font-size: 22px;
  color: #5a3e2b;
  margin: 0 0 8px;
  font-weight: bold;
}

.product-tags { display: flex; gap: 6px; margin-bottom: 8px; }

.product-desc {
  font-size: 13px;
  color: #7a6a55;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--ivory);
  border: 1px solid var(--gold-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-text { display: flex; flex-direction: column; }
.info-label { font-size: 11px; color: #a08c72; }
.info-value { font-size: 14px; font-weight: bold; color: #5a3e2b; }

.section-title {
  font-size: 15px;
  color: #5a3e2b;
  margin: 20px 0 10px;
  display: flex;
  align-items: center;
  border-bottom: 1px dashed var(--gold-light);
  padding-bottom: 6px;
}

.farmer-card {
  display: flex;
  gap: 12px;
  align-items: center;
  background: var(--ivory);
  border-radius: 8px;
  padding: 12px;
  border: 1px solid var(--gold-light);
}

.farmer-avatar {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid var(--gold);
}
.farmer-avatar img { width: 100%; height: 100%; object-fit: cover; }

.farmer-name { font-weight: bold; color: #5a3e2b; font-size: 15px; }
.farmer-brand { font-size: 12px; color: #8b7355; margin-top: 4px; display: flex; align-items: center; gap: 6px; }
.brand-logo { width: 20px; height: 20px; border-radius: 4px; object-fit: contain; }

.logistics-location { font-weight: bold; color: #5a3e2b; font-size: 14px; }
.logistics-desc { font-size: 12px; color: #8b7355; margin-left: 8px; }

.cert-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px dashed var(--gold-light);
}

.qr-box {
  width: 80px;
  height: 80px;
  border: 2px solid var(--gold);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  gap: 4px;
}
.qr-box span { font-size: 10px; color: var(--gold); }

.sig-section { text-align: center; flex: 1; }
.sig-label { font-size: 11px; color: #a08c72; margin-bottom: 4px; }
.sig-value {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: #5a3e2b;
  word-break: break-all;
  max-width: 200px;
  margin: 0 auto;
  line-height: 1.4;
}
.sig-date { font-size: 11px; color: #a08c72; margin-top: 4px; }

.seal-wrap {
  position: relative;
}
.seal {
  width: 72px;
  height: 72px;
  border: 3px solid #c0392b;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0392b;
  transform: rotate(-15deg);
  opacity: 0.85;
}
.seal-text { font-size: 11px; font-weight: bold; letter-spacing: 2px; }
.seal-star { font-size: 18px; }

.stats-bar {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  font-size: 12px;
  color: #a08c72;
}
.stats-bar span { display: flex; align-items: center; gap: 4px; }

@media (max-width: 640px) {
  .product-hero { flex-direction: column; align-items: center; text-align: center; }
  .product-img-wrap { width: 120px; height: 120px; }
  .info-grid { grid-template-columns: 1fr; }
  .cert-footer { flex-direction: column; align-items: center; gap: 16px; }
  .cert-title { font-size: 22px; letter-spacing: 4px; }
}
</style>
