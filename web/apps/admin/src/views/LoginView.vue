<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NText, useMessage } from 'naive-ui'
import { ApiError } from '@hei/shared'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const message = useMessage()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function onSubmit() {
  if (!form.username.trim() || !form.password) {
    message.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    message.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <NCard style="width: 100%; max-width: 420px" title="后台管理登录">
      <NText depth="3">仅 ADMIN 账号可登录。前台 PORTAL 用户请使用 Portal。</NText>
      <NForm style="margin-top: 16px" @submit.prevent="onSubmit">
        <NFormItem label="用户名">
          <NInput v-model:value="form.username" autocomplete="username" />
        </NFormItem>
        <NFormItem label="密码">
          <NInput
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            autocomplete="current-password"
          />
        </NFormItem>
        <NSpace>
          <NButton type="primary" attr-type="submit" :loading="loading">登录</NButton>
        </NSpace>
      </NForm>
    </NCard>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: #f5f7fa;
}
</style>
