<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NDescriptions, NDescriptionsItem, NSkeleton, NSpace, NTag, NText, useMessage } from 'naive-ui'
import { ApiError } from '@hei/shared'
import type { PublicUser } from '@hei/shared'
import { authApi } from '@/api'

const route = useRoute()
const message = useMessage()
const loading = ref(true)
const user = ref<PublicUser | null>(null)

async function load() {
  loading.value = true
  user.value = null
  try {
    user.value = await authApi.getPublicUser(String(route.params.id))
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <NSpace vertical :size="12">
    <div>
      <NText style="font-size: 20px; font-weight: 600">用户公开信息</NText>
      <div><NText depth="3">仅展示启用中的前台 PORTAL 用户</NText></div>
    </div>
    <NSkeleton v-if="loading" :repeat="2" text />
    <NDescriptions v-else-if="user" bordered :column="1" label-placement="left">
      <NDescriptionsItem label="用户 ID">{{ user.userId }}</NDescriptionsItem>
      <NDescriptionsItem label="用户名">{{ user.username }}</NDescriptionsItem>
      <NDescriptionsItem label="类型">
        <NTag size="small">{{ user.userType }}</NTag>
      </NDescriptionsItem>
    </NDescriptions>
  </NSpace>
</template>
