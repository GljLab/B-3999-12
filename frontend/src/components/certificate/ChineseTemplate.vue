<template>
  <div class="chinese-cert">
    <div class="cn-border">
      <div class="cn-corner cn-corner-tl"></div>
      <div class="cn-corner cn-corner-tr"></div>
      <div class="cn-corner cn-corner-bl"></div>
      <div class="cn-corner cn-corner-br"></div>
      <div class="cn-border-inner">
        <div class="cn-pattern-top"></div>

        <div class="cn-header">
          <div class="cn-header-deco">❖ ❖ ❖</div>
          <h1 class="cn-title">溯源凭证</h1>
          <p class="cn-subtitle">品 质 之 证 · 信 誉 之 书</p>
        </div>

        <div class="cn-cert-no">
          <span class="cn-no-icon">◈</span>
          <span>证书编号：{{ cert.certificateNo }}</span>
          <span class="cn-no-icon">◈</span>
        </div>

        <div class="cn-product">
          <div class="cn-product-img" v-if="cert.productImageUrl">
            <img :src="getImageUrl(cert.productImageUrl)" :alt="cert.productName" />
            <div class="cn-img-frame"></div>
          </div>
          <div class="cn-product-main">
            <div class="cn-product-name">{{ cert.productName }}</div>
            <div class="cn-product-tags">
              <span class="cn-tag gold">{{ cert.productCategory }}</span>
              <span class="cn-tag red" v-if="cert.qualityGrade">{{ cert.qualityGrade }}</span>
            </div>
            <p class="cn-product-desc" v-if="cert.productDescription">{{ cert.productDescription }}</p>
          </div>
        </div>

        <div class="cn-divider">
          <span class="divider-dot"></span>
          <span class="divider-line"></span>
          <span class="divider-icon">✦</span>
          <span class="divider-line"></span>
          <span class="divider-dot"></span>
        </div>

        <div class="cn-info-grid">
          <div class="cn-info-item">
            <div class="cn-info-icon">产</div>
            <div class="cn-info-content">
              <span class="cn-info-label">产地</span>
              <span class="cn-info-value">{{ cert.productOrigin }}</span>
            </div>
          </div>
          <div class="cn-info-item">
            <div class="cn-info-icon">农</div>
            <div class="cn-info-content">
              <span class="cn-info-label">农户</span>
              <span class="cn-info-value">{{ cert.farmerName }}</span>
            </div>
          </div>
          <div class="cn-info-item">
            <div class="cn-info-icon">期</div>
            <div class="cn-info-content">
              <span class="cn-info-label">采摘</span>
              <span class="cn-info-value">{{ formatDate(cert.harvestDate) }}</span>
            </div>
          </div>
          <div class="cn-info-item">
            <div class="cn-info-icon">批</div>
            <div class="cn-info-content">
              <span class="cn-info-label">批次</span>
              <span class="cn-info-value mono">{{ cert.batchNo }}</span>
            </div>
          </div>
        </div>

        <div class="cn-farmer" v-if="cert.brandIntro">
          <div class="cn-farmer-header">
            <img v-if="cert.brandLogoUrl" :src="getImageUrl(cert.brandLogoUrl)" class="cn-brand-logo" alt="" />
            <span class="cn-farmer-title">品牌传承</span>
            <div class="cn-farmer-farm" v-if="cert.farmPhotoUrl">
              <img :src="getImageUrl(cert.farmPhotoUrl)" alt="农场" />
            </div>
          </div>
          <p class="cn-farmer-text">{{ cert.brandIntro }}</p>
        </div>

        <div class="cn-logistics" v-if="logistics.length">
          <h3 class="cn-section-title">
            <span class="title-deco">◇</span>
            物流追溯
            <span class="title-deco">◇</span>
          </h3>
          <el-timeline class="cn-timeline">
            <el-timeline-item
              v-for="(item, idx) in logistics"
              :key="idx"
              :timestamp="item.time || item.recordedAt"
              color="#c0392b"
              size="large"
            >
              <span class="cn-log-location">{{ item.location || item.statusDesc }}</span>
              <span class="cn-log-desc" v-if="item.statusDesc && item.location">{{ item.statusDesc }}</span>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="cn-footer">
          <div class="cn-qr">
            <div class="cn-qr-box">
              <el-icon :size="24" color="#c0392b"><Connection /></el-icon>
              <span>扫码验证</span>
            </div>
          </div>

          <div class="cn-signature">
            <div class="cn-sig-label">数字签章</div>
            <div class="cn-sig-value">{{ cert.digitalSignature }}</div>
            <div class="cn-sig-date">{{ formatDate(cert.createdAt) }} 签发</div>
          </div>

          <div class="cn-seal">
            <div class="seal-outer">
              <div class="seal-inner">
                <div class="seal-top">溯源</div>
                <div class="seal-star">★</div>
                <div class="seal-bottom">认证</div>
              </div>
            </div>
          </div>
        </div>

        <div class="cn-stats">
          <span><el-icon><View /></el-icon> {{ cert.viewCount || 0 }} 次浏览</span>
          <span><el-icon><Share /></el-icon> {{ cert.shareCount || 0 }} 次分享</span>
          <span><el-icon><CircleCheck /></el-icon> {{ cert.verifyCount || 0 }} 次验证</span>
        </div>

        <div class="cn-pattern-bottom"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Connection, View, Share, CircleCheck } from '@element-plus/icons-vue'

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
.chinese-cert {
  --red: #c0392b;
  --red-light: #e74c3c;
  --red-bg: #fdf2f0;
  --gold: #d4a843;
  --gold-dark: #b8922e;
  --cream: #fef9f3;
  font-family: 'STSong', 'SimSun', 'FangSong', serif;
  background: linear-gradient(160deg, var(--cream) 0%, var(--red-bg) 50%, var(--cream) 100%);
  padding: 24px;
  min-height: 100%;
  position: relative;
}

.cn-border {
  border: 2px solid var(--red);
  position: relative;
  padding: 8px;
}

.cn-border-inner {
  border: 1px solid var(--gold);
  padding: 28px 24px;
  position: relative;
}

.cn-corner {
  position: absolute;
  width: 24px;
  height: 24px;
  border-color: var(--red);
  border-style: solid;
}
.cn-corner-tl { top: 0; left: 0; border-width: 3px 0 0 3px; }
.cn-corner-tr { top: 0; right: 0; border-width: 3px 3px 0 0; }
.cn-corner-bl { bottom: 0; left: 0; border-width: 0 0 3px 3px; }
.cn-corner-br { bottom: 0; right: 0; border-width: 0 3px 3px 0; }

.cn-pattern-top,
.cn-pattern-bottom {
  height: 6px;
  background: repeating-linear-gradient(
    90deg,
    var(--red) 0px,
    var(--red) 8px,
    transparent 8px,
    transparent 12px,
    var(--gold) 12px,
    var(--gold) 20px,
    transparent 20px,
    transparent 24px
  );
  border-radius: 2px;
}
.cn-pattern-bottom { margin-top: 16px; }

.cn-header { text-align: center; margin: 16px 0 12px; }
.cn-header-deco { color: var(--gold); font-size: 12px; letter-spacing: 8px; margin-bottom: 8px; }
.cn-title {
  font-size: 36px;
  color: var(--red);
  font-weight: bold;
  letter-spacing: 16px;
  margin: 0;
  text-shadow: 1px 1px 0 rgba(212, 168, 67, 0.3);
}
.cn-subtitle {
  font-size: 12px;
  color: var(--gold-dark);
  letter-spacing: 6px;
  margin: 6px 0 0;
}

.cn-cert-no {
  text-align: center;
  font-size: 13px;
  color: #8b5e3c;
  margin: 12px 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.cn-no-icon { color: var(--gold); }

.cn-product {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.cn-product-img {
  width: 130px;
  height: 130px;
  flex-shrink: 0;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
}
.cn-product-img img { width: 100%; height: 100%; object-fit: cover; }
.cn-img-frame {
  position: absolute;
  inset: 4px;
  border: 1px solid var(--gold);
  border-radius: 2px;
  pointer-events: none;
}

.cn-product-name {
  font-size: 24px;
  font-weight: bold;
  color: var(--red);
  margin-bottom: 8px;
  letter-spacing: 4px;
}

.cn-product-tags { display: flex; gap: 8px; margin-bottom: 8px; }
.cn-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 2px;
  font-size: 12px;
}
.cn-tag.gold { background: var(--gold); color: #fff; }
.cn-tag.red { background: var(--red); color: #fff; }

.cn-product-desc {
  font-size: 13px;
  color: #6b5240;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cn-divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0;
}
.divider-dot { width: 6px; height: 6px; background: var(--red); border-radius: 50%; }
.divider-line { flex: 1; height: 1px; background: linear-gradient(90deg, transparent, var(--gold), transparent); }
.divider-icon { color: var(--gold); font-size: 14px; }

.cn-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.cn-info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(254, 249, 243, 0.8);
  border: 1px solid var(--gold);
  border-radius: 4px;
}

.cn-info-icon {
  width: 32px;
  height: 32px;
  border: 1px solid var(--red);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--red);
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.cn-info-content { display: flex; flex-direction: column; }
.cn-info-label { font-size: 10px; color: #a08060; }
.cn-info-value { font-size: 14px; color: #5a3a2a; font-weight: 600; }
.cn-info-value.mono { font-family: 'Courier New', monospace; }

.cn-farmer {
  margin-top: 16px;
  padding: 14px;
  background: rgba(254, 249, 243, 0.8);
  border: 1px solid var(--gold);
  border-radius: 4px;
}

.cn-farmer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.cn-brand-logo { width: 22px; height: 22px; border-radius: 4px; object-fit: contain; }
.cn-farmer-title { font-size: 14px; font-weight: bold; color: var(--red); letter-spacing: 2px; }
.cn-farmer-farm {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  margin-left: auto;
  border: 1px solid var(--gold);
}
.cn-farmer-farm img { width: 100%; height: 100%; object-fit: cover; }
.cn-farmer-text { font-size: 12px; color: #6b5240; line-height: 1.7; margin: 0; }

.cn-section-title {
  font-size: 15px;
  color: var(--red);
  text-align: center;
  letter-spacing: 4px;
  margin: 20px 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.title-deco { color: var(--gold); font-size: 12px; }

.cn-log-location { font-weight: bold; color: #5a3a2a; }
.cn-log-desc { font-size: 12px; color: #8b5e3c; margin-left: 6px; }

.cn-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px dashed var(--gold);
}

.cn-qr-box {
  width: 72px;
  height: 72px;
  border: 2px solid var(--red);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: #fff;
}
.cn-qr-box span { font-size: 10px; color: var(--red); }

.cn-signature { text-align: center; flex: 1; }
.cn-sig-label { font-size: 11px; color: #a08060; }
.cn-sig-value {
  font-family: 'Courier New', monospace;
  font-size: 10px;
  color: #6b5240;
  word-break: break-all;
  max-width: 160px;
  margin: 4px auto;
  line-height: 1.4;
}
.cn-sig-date { font-size: 11px; color: #a08060; }

.seal-outer {
  width: 76px;
  height: 76px;
  border: 3px solid var(--red);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: rotate(-20deg);
  opacity: 0.85;
}
.seal-inner {
  text-align: center;
  color: var(--red);
  line-height: 1;
}
.seal-top { font-size: 11px; font-weight: bold; letter-spacing: 2px; }
.seal-star { font-size: 16px; margin: 2px 0; }
.seal-bottom { font-size: 11px; font-weight: bold; letter-spacing: 2px; }

.cn-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 14px;
  font-size: 11px;
  color: #a08060;
}
.cn-stats span { display: flex; align-items: center; gap: 3px; }

@media (max-width: 640px) {
  .cn-product { flex-direction: column; align-items: center; text-align: center; }
  .cn-product-img { width: 110px; height: 110px; }
  .cn-info-grid { grid-template-columns: 1fr; }
  .cn-title { font-size: 28px; letter-spacing: 8px; }
  .cn-footer { flex-direction: column; align-items: center; gap: 16px; }
}
</style>
