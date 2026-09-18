<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NSpace, NFlex, NText, useMessage } from 'naive-ui'
import { ApiError } from '@hei/shared'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

async function onSubmit() {
  if (!form.username.trim() || !form.password) {
    message.warning('请输入用户名和密码')
    return
  }
  if (form.password.length < 6) {
    message.warning('密码至少 6 位')
    return
  }
  if (form.password !== form.confirmPassword) {
    message.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    const result = await auth.register(form.username.trim(), form.password)
    message.success(`注册成功（#${result.userId}），请登录`)
    await router.push('/login')
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <NFlex justify="center" align="center" style="height: 100%; min-height: 320px">
    <NSpace vertical :size="12" style="width: 100%; max-width: 420px">
      <div>
        <NText style="font-size: 20px; font-weight: 600">注册前台账号</NText>
        <div><NText depth="3">公开注册固定创建 PORTAL 用户，不会生成后台管理员。</NText></div>
      </div>
      <NCard>
        <NForm @submit.prevent="onSubmit">
          <NFormItem label="用户名">
            <NInput v-model:value="form.username" autocomplete="username" />
          </NFormItem>
          <NFormItem label="密码">
            <NInput v-model:value="form.password" type="password" show-password-on="click" autocomplete="new-password" />
          </NFormItem>
          <NFormItem label="确认密码">
            <NInput
              v-model:value="form.confirmPassword"
              type="password"
              show-password-on="click"
              autocomplete="new-password"
            />
          </NFormItem>
          <NSpace>
            <NButton type="primary" attr-type="submit" :loading="loading">注册</NButton>
            <NButton text type="primary" @click="router.push('/login')">已有账号？去登录</NButton>
          </NSpace>
        </NForm>
      </NCard>
    </NSpace>
  </NFlex>
</template>
