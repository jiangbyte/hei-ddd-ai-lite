<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NGrid, NGi, NEmpty, NSpace, NText, NButton } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  if (auth.isLoggedIn && !auth.profile) {
    try {
      await auth.fetchMe()
    } catch {
      // interceptor handles unauthorized
    }
  }
})
</script>

<template>
  <NSpace vertical :size="16">
    <NText style="font-size: 20px; font-weight: 600">仪表盘</NText>
    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="3 m:1">
        <NCard size="small">
          <NText depth="3">当前管理员</NText>
          <div style="margin-top: 8px; font-size: 22px; font-weight: 600">
            {{ auth.profile?.username || '-' }}
          </div>
        </NCard>
      </NGi>
      <NGi span="3 m:1">
        <NCard size="small">
          <NText depth="3">账号类型</NText>
          <div style="margin-top: 8px; font-size: 22px; font-weight: 600">
            {{ auth.profile?.userType || 'ADMIN' }}
          </div>
        </NCard>
      </NGi>
      <NGi span="3 m:1">
        <NCard size="small">
          <NText depth="3">功能入口</NText>
          <div style="margin-top: 8px">
            <NButton text type="primary" @click="router.push('/users')">用户管理</NButton>
          </div>
        </NCard>
      </NGi>
    </NGrid>
    <NCard>
      <NEmpty description="业务指标占位：后续可接入 Agent / 会话统计等。" />
    </NCard>
  </NSpace>
</template>
