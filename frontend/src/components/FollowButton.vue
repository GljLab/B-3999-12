<template>
  <el-button
    :type="buttonType"
    :size="size"
    :loading="loading"
    :disabled="disabled || isSelf"
    @click="handleClick"
    :class="['follow-button', { 'followed': isFollowed }]"
  >
    <template v-if="!isSelf">
      <template v-if="isFollowed">
        <span v-if="isMutual">已收藏🤝</span>
        <span v-else>已收藏</span>
      </template>
      <span v-else>收藏农友</span>
    </template>
    <span v-else>{{ selfText }}</span>
  </el-button>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'

const props = defineProps({
  userId: {
    type: [Number, String],
    required: true
  },
  initialFollowed: {
    type: Boolean,
    default: false
  },
  initialMutual: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  selfText: {
    type: String,
    default: '编辑资料'
  },
  showSelf: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:followed', 'toggle', 'self-click'])

const store = useUserStore()
const router = useRouter()
const loading = ref(false)
const isFollowed = ref(props.initialFollowed)
const isMutual = ref(props.initialMutual)

const isSelf = computed(() => {
  if (!props.showSelf) return false
  return store.userId && String(store.userId) === String(props.userId)
})

const disabled = computed(() => !store.token)

const buttonType = computed(() => {
  if (isSelf.value) return 'primary'
  return isFollowed.value ? 'success' : 'primary'
})

let debounceTimer = null

const handleClick = async () => {
  if (isSelf.value) {
    emit('self-click')
    return
  }
  if (!store.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (debounceTimer && clearTimeout(debounceTimer))

  if (isFollowed.value) {
    try {
      await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }

  loading.value = true
  debounceTimer = setTimeout(async () => {
    try {
      const res = await userApi.toggleFollow(props.userId)
      isFollowed.value = res.data.followed
      isMutual.value = res.data.isMutual
      emit('update:followed', isFollowed.value)
      emit('toggle', {
        followed: isFollowed.value,
        followerCount: res.data.followerCount,
        followingCount: res.data.followingCount
      })
      ElMessage.success(isFollowed.value ? '收藏成功' : '已取消收藏')
    } catch (err) {
      console.error(err)
    } finally {
      loading.value = false
    }
  }, 500)
}

defineExpose({
  setFollowed: (val, mutual = false) => {
    isFollowed.value = val
    isMutual.value = mutual
  }
})
</script>

<style scoped>
.follow-button {
  transition: all 0.3s ease;
}
.follow-button.followed {
  background-color: #67c23a;
  border-color: #67c23a;
}
</style>
