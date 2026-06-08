<template>
  <div class="tech-cert">
    <div class="tech-circuit-bg"></div>
    <div class="tech-content">
      <div class="tech-header">
        <div class="tech-scan-line"></div>
        <div class="tech-title-group">
          <div class="tech-system-label">TRACEABILITY SYSTEM v2.0</div>
          <h1 class="tech-title">数字溯源证书</h1>
          <div class="tech-cert-no">
            <span class="blink">▸</span>
            CERT_ID: <span class="mono highlight">{{ cert.certificateNo }}</span>
          </div>
        </div>
        <div class="tech-status">
          <div class="status-dot"></div>
          <span>VERIFIED</span>
        </div>
      </div>

      <div class="tech-dashboard">
        <div class="tech-card tech-hero-card">
          <div class="card-header">
            <span class="card-icon">◈</span>
            <span class="card-title">产品数据</span>
            <span class="card-tag">PRODUCT</span>
          </div>
          <div class="hero-body">
            <div class="hero-img" v-if="cert.productImageUrl">
              <img :src="getImageUrl(cert.productImageUrl)" :alt="cert.productName" />
              <div class="img-scan-effect"></div>
            </div>
            <div class="hero-img empty" v-else>
              <el-icon :size="36" color="#00e5ff"><Monitor /></el-icon>
            </div>
            <div class="hero-data">
              <div class="data-name">{{ cert.productName }}</div>
              <div class="data-tags">
                <span class="tech-tag cyan">{{ cert.productCategory }}</span>
                <span class="tech-tag green" v-if="cert.qualityGrade">{{ cert.qualityGrade }}</span>
              </div>
              <div class="data-row">
                <span class="data-key">ORIGIN</span>
                <span class="data-val">{{ cert.productOrigin || 'N/A' }}</span>
              </div>
              <div class="data-row" v-if="cert.productDescription">
                <span class="data-key">DESC</span>
                <span class="data-val desc">{{ cert.productDescription }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="tech-row">
          <div class="tech-card tech-data-card">
            <div class="card-header">
              <span class="card-icon">◇</span>
              <span class="card-title">核心参数</span>
              <span class="card-tag">PARAMS</span>
            </div>
            <div class="data-grid">
              <div class="data-cell">
                <div class="cell-label">农户</div>
                <div class="cell-value">{{ cert.farmerName || 'N/A' }}</div>
              </div>
              <div class="data-cell">
                <div class="cell-label">采摘</div>
                <div class="cell-value">{{ formatDate(cert.harvestDate) }}</div>
              </div>
              <div class="data-cell">
                <div class="cell-label">批次</div>
                <div class="cell-value mono">{{ cert.batchNo || 'N/A' }}</div>
              </div>
              <div class="data-cell">
                <div class="cell-label">生产</div>
                <div class="cell-value">{{ formatDate(cert.productionDate) }}</div>
              </div>
              <div class="data-cell">
                <div class="cell-label">溯源码</div>
                <div class="cell-value mono highlight">{{ cert.traceCode || 'N/A' }}</div>
              </div>
              <div class="data-cell">
                <div class="cell-label">等级</div>
                <div class="cell-value">{{ cert.qualityGrade || 'N/A' }}</div>
              </div>
            </div>
          </div>

          <div class="tech-card tech-brand-card" v-if="cert.brandIntro">
            <div class="card-header">
              <span class="card-icon">◆</span>
              <span class="card-title">品牌数据</span>
              <span class="card-tag">BRAND</span>
            </div>
            <div class="brand-body">
              <img v-if="cert.brandLogoUrl" :src="getImageUrl(cert.brandLogoUrl)" class="brand-logo" alt="" />
              <div class="brand-info">
                <div class="brand-name">{{ cert.farmerName }}</div>
                <p class="brand-text">{{ cert.brandIntro }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="tech-card tech-logistics-card" v-if="logistics.length">
          <div class="card-header">
            <span class="card-icon">⬡</span>
            <span class="card-title">物流链路</span>
            <span class="card-tag">LOGISTICS</span>
            <span class="card-count">{{ logistics.length }} NODES</span>
          </div>
          <div class="logistics-flow">
            <div class="flow-line"></div>
            <div class="flow-node" v-for="(item, idx) in logistics" :key="idx">
              <div class="node-dot"></div>
              <div class="node-content">
                <div class="node-location">{{ item.location || item.statusDesc }}</div>
                <div class="node-desc" v-if="item.statusDesc && item.location">{{ item.statusDesc }}</div>
                <div class="node-time">{{ item.time || item.recordedAt }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tech-footer">
        <div class="tech-qr">
          <div class="qr-box">
            <div class="qr-scan-bar"></div>
            <el-icon :size="22" color="#00e5ff"><Connection /></el-icon>
            <span>扫码验证</span>
          </div>
        </div>

        <div class="tech-signature">
          <div class="sig-header">DIGITAL SIGNATURE</div>
          <div class="sig-value">{{ cert.digitalSignature }}</div>
          <div class="sig-meta">
            <span>ISSUED: {{ formatDate(cert.createdAt) }}</span>
          </div>
        </div>

        <div class="tech-stats">
          <div class="stat-item">
            <el-icon color="#00e5ff"><View /></el-icon>
            <div class="stat-val">{{ cert.viewCount || 0 }}</div>
            <div class="stat-label">VIEWS</div>
          </div>
          <div class="stat-item">
            <el-icon color="#00ff88"><Share /></el-icon>
            <div class="stat-val">{{ cert.shareCount || 0 }}</div>
            <div class="stat-label">SHARES</div>
          </div>
          <div class="stat-item">
            <el-icon color="#ffd700"><CircleCheck /></el-icon>
            <div class="stat-val">{{ cert.verifyCount || 0 }}</div>
            <div class="stat-label">VERIFY</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Monitor, Connection, View, Share, CircleCheck } from '@element-plus/icons-vue'

const props = defineProps({
  cert: { type: Object, required: true }
})

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8000' + url
}

const formatDate = (date) => {
  if (!date) return 'N/A'
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
.tech-cert {
  --bg-dark: #0a0e1a;
  --bg-card: #111827;
  --border: #1e293b;
  --cyan: #00e5ff;
  --green: #00ff88;
  --gold: #ffd700;
  --text: #e0e0e0;
  --text-dim: #6b7280;
  font-family: 'Courier New', 'Consolas', monospace;
  background: linear-gradient(135deg, #0a0e1a 0%, #0f172a 50%, #0a0e1a 100%);
  padding: 20px;
  min-height: 100%;
  position: relative;
  color: var(--text);
  overflow: hidden;
}

.tech-circuit-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--border) 1px, transparent 1px),
    linear-gradient(90deg, var(--border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.3;
  pointer-events: none;
}

.tech-content {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
}

.tech-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.tech-scan-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--cyan), transparent);
  animation: scanMove 3s ease-in-out infinite;
}

@keyframes scanMove {
  0%, 100% { opacity: 0.3; transform: scaleX(0.5); }
  50% { opacity: 1; transform: scaleX(1); }
}

.tech-system-label {
  font-size: 10px;
  color: var(--cyan);
  letter-spacing: 2px;
  margin-bottom: 4px;
}

.tech-title {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  margin: 0;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(0, 229, 255, 0.3);
}

.tech-cert-no {
  font-size: 12px;
  color: var(--text-dim);
  margin-top: 6px;
}

.blink {
  color: var(--green);
  animation: blinkAnim 1s step-end infinite;
}

@keyframes blinkAnim {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.highlight { color: var(--cyan); }
.mono { font-family: 'Courier New', monospace; }

.tech-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--green);
  border: 1px solid var(--green);
  padding: 4px 10px;
  border-radius: 2px;
  background: rgba(0, 255, 136, 0.05);
}

.status-dot {
  width: 6px;
  height: 6px;
  background: var(--green);
  border-radius: 50%;
  animation: pulseDot 2s ease-in-out infinite;
}

@keyframes pulseDot {
  0%, 100% { box-shadow: 0 0 0 0 rgba(0, 255, 136, 0.4); }
  50% { box-shadow: 0 0 0 4px rgba(0, 255, 136, 0); }
}

.tech-dashboard { display: flex; flex-direction: column; gap: 12px; }

.tech-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 16px;
  position: relative;
}

.tech-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--cyan), transparent);
  opacity: 0.4;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 12px;
}

.card-icon { color: var(--cyan); font-size: 10px; }
.card-title { color: var(--text); font-weight: bold; }
.card-tag {
  margin-left: auto;
  font-size: 9px;
  color: var(--text-dim);
  border: 1px solid var(--border);
  padding: 1px 6px;
  border-radius: 2px;
}
.card-count {
  font-size: 10px;
  color: var(--cyan);
  margin-left: 8px;
}

.hero-body { display: flex; gap: 16px; }

.hero-img {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  border: 1px solid var(--border);
}
.hero-img img { width: 100%; height: 100%; object-fit: cover; }
.hero-img.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-dark);
}

.img-scan-effect {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 229, 255, 0.1) 50%, transparent 100%);
  animation: imgScan 4s ease-in-out infinite;
}

@keyframes imgScan {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

.data-name {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
}

.data-tags { display: flex; gap: 6px; margin-bottom: 10px; }

.tech-tag {
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 10px;
  font-weight: bold;
}
.tech-tag.cyan { background: rgba(0, 229, 255, 0.15); color: var(--cyan); border: 1px solid rgba(0, 229, 255, 0.3); }
.tech-tag.green { background: rgba(0, 255, 136, 0.15); color: var(--green); border: 1px solid rgba(0, 255, 136, 0.3); }

.data-row { margin-bottom: 4px; }
.data-key { font-size: 9px; color: var(--text-dim); margin-right: 8px; }
.data-val { font-size: 13px; color: var(--text); }
.data-val.desc {
  font-size: 12px;
  color: var(--text-dim);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tech-row { display: flex; gap: 12px; }
.tech-data-card { flex: 1; }
.tech-brand-card { flex: 1; }

.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}

.data-cell {
  padding: 8px;
  background: var(--bg-dark);
  border: 1px solid var(--border);
  border-radius: 4px;
}
.cell-label { font-size: 9px; color: var(--text-dim); margin-bottom: 4px; text-transform: uppercase; }
.cell-value { font-size: 12px; color: var(--text); font-weight: 500; }
.cell-value.mono { font-family: 'Courier New', monospace; }

.brand-body { display: flex; gap: 12px; align-items: flex-start; }
.brand-logo { width: 32px; height: 32px; border-radius: 4px; object-fit: contain; border: 1px solid var(--border); }
.brand-name { font-size: 13px; color: var(--cyan); font-weight: bold; margin-bottom: 4px; }
.brand-text { font-size: 11px; color: var(--text-dim); line-height: 1.6; margin: 0; }

.logistics-flow { position: relative; padding-left: 20px; }

.flow-line {
  position: absolute;
  left: 5px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(180deg, var(--cyan), var(--green), var(--cyan));
  opacity: 0.4;
}

.flow-node {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
  position: relative;
}

.node-dot {
  width: 10px;
  height: 10px;
  background: var(--cyan);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  margin-left: -17px;
  box-shadow: 0 0 8px rgba(0, 229, 255, 0.5);
}

.node-location { font-size: 13px; color: #fff; font-weight: 500; }
.node-desc { font-size: 11px; color: var(--green); }
.node-time { font-size: 10px; color: var(--text-dim); margin-top: 2px; }

.tech-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.qr-box {
  width: 68px;
  height: 68px;
  border: 1px solid var(--cyan);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  position: relative;
  background: rgba(0, 229, 255, 0.03);
}
.qr-box span { font-size: 9px; color: var(--cyan); }

.qr-scan-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--cyan);
  animation: qrScan 2s ease-in-out infinite;
}

@keyframes qrScan {
  0% { top: 0; opacity: 0.5; }
  50% { top: calc(100% - 2px); opacity: 1; }
  100% { top: 0; opacity: 0.5; }
}

.tech-signature { text-align: center; flex: 1; }
.sig-header { font-size: 9px; color: var(--cyan); letter-spacing: 2px; margin-bottom: 4px; }
.sig-value {
  font-family: 'Courier New', monospace;
  font-size: 10px;
  color: var(--text-dim);
  word-break: break-all;
  max-width: 200px;
  margin: 0 auto;
  line-height: 1.4;
}
.sig-meta { font-size: 10px; color: var(--text-dim); margin-top: 4px; }

.tech-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 6px 10px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 4px;
  min-width: 52px;
}
.stat-val { font-size: 16px; font-weight: bold; color: #fff; margin-top: 2px; }
.stat-label { font-size: 8px; color: var(--text-dim); margin-top: 2px; }

@media (max-width: 640px) {
  .hero-body { flex-direction: column; align-items: center; text-align: center; }
  .hero-img { width: 100px; height: 100px; }
  .data-grid { grid-template-columns: 1fr 1fr; }
  .tech-row { flex-direction: column; }
  .tech-footer { flex-direction: column; align-items: center; gap: 16px; }
  .tech-stats { justify-content: center; }
  .tech-title { font-size: 20px; }
}
</style>
