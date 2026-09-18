<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NDescriptions, NDescriptionsItem, NSkeleton, NSpace, NTag, NText, useMessage } from 'naive-ui'
import { ApiError, formatDateTime } from '@hei/shared'
import type { UserProfile } from '@hei/shared'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const message = useMessage()
const loading = ref(true)
const profile = ref<UserProfile | null>(null)

onMounted(async () => {
  try {
    profile.value = await auth.fetchMe()
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <NSpace vertical :size="12">
    <div>
      <NText style="font-size: 20px; font-weight: 600">我的账号</NText>
      <div><NText depth="3">GET /auth/me · 展示当前登录前台用户信息</NText></div>
    </div>
    <NSkeleton v-if="loading" :repeat="4" text />
    <NDescriptions v-else-if="profile" bordered :column="1" label-placement="left">
      <NDescriptionsItem label="用户 ID">{{ profile.userId }}</NDescriptionsItem>
      <NDescriptionsItem label="用户名">{{ profile.username }}</NDescriptionsItem>
      <NDescriptionsItem label="用户类型">
        <NTag type="success" size="small">{{ profile.userType }}</NTag>
      </NDescriptionsItem>
      <NDescriptionsItem label="启用">{{ profile.enabled ? '是' : '否' }}</NDescriptionsItem>
      <NDescriptionsItem label="创建时间">{{ formatDateTime(profile.createTime) }}</NDescriptionsItem>
      <NDescriptionsItem label="更新时间">{{ formatDateTime(profile.updateTime) }}</NDescriptionsItem>
    </NDescriptions>
  </NSpace>
</template>
