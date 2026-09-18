<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NFlex, NText, useMessage } from 'naive-ui'
import { ApiError } from '@hei/shared'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const message = useMessage()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
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
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/me'
    await router.replace(redirect)
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <NFlex justify="center" align="center" style="height: 100%; min-height: 320px">
    <NSpace vertical :size="12" style="width: 100%; max-width: 420px">
      <div>
        <NText style="font-size: 20px; font-weight: 600">前台登录</NText>
        <div><NText depth="3">仅 PORTAL 用户可登录；ADMIN 请使用后台管理端。</NText></div>
      </div>
      <NCard>
        <NForm @submit.prevent="onSubmit">
          <NFormItem label="用户名">
            <NInput v-model:value="form.username" autocomplete="username" placeholder="前台用户名" />
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
            <NButton text type="primary" @click="router.push('/register')">没有账号？去注册</NButton>
          </NSpace>
        </NForm>
      </NCard>
    </NSpace>
  </NFlex>
</template>
