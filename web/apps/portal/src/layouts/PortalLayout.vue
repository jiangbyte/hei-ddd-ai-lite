<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { NLayoutHeader, NLayoutFooter, NSpace, NButton, NText } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loggedIn = computed(() => auth.isLoggedIn)
const displayName = computed(() => auth.profile?.username)

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="portal-shell">
    <NLayoutHeader bordered class="header">
      <NSpace align="center" :size="16">
        <RouterLink class="brand" to="/">
          <NText strong>Hei Portal</NText>
        </RouterLink>
        <NButton text @click="router.push('/')">首页</NButton>
        <NButton v-if="loggedIn" text @click="router.push('/me')">我的账号</NButton>
      </NSpace>
      <NSpace align="center" :size="12">
        <template v-if="!loggedIn">
          <NButton text @click="router.push('/login')">登录</NButton>
          <NButton type="primary" @click="router.push('/register')">注册</NButton>
        </template>
        <template v-else>
          <NText depth="3">{{ displayName }}</NText>
          <NButton text @click="onLogout">登出</NButton>
        </template>
      </NSpace>
    </NLayoutHeader>

    <main class="portal-main">
      <RouterView v-slot="{ Component }">
        <component :is="Component" class="portal-page" />
      </RouterView>
    </main>

    <NLayoutFooter bordered class="footer">
      <NText depth="3">用户前台 · 仅 PORTAL 账号可登录</NText>
    </NLayoutFooter>
  </div>
</template>

<style scoped>
.portal-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  flex-shrink: 0;
}

.brand {
  display: inline-flex;
  align-items: center;
  height: 100%;
  text-decoration: none;
  color: inherit;
}

.portal-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}

.portal-page {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.footer {
  padding: 16px 24px;
  text-align: center;
  flex-shrink: 0;
}
</style>
