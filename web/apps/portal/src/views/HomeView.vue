<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NCard, NGrid, NGi, NSpace, NButton, NText } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
</script>

<template>
  <NSpace vertical :size="16">
    <div>
      <NText style="font-size: 22px; font-weight: 600">Hei 用户中心</NText>
      <div>
        <NText depth="3">前台站点：注册、登录与账号信息。后台管理请使用独立 Admin 工程。</NText>
      </div>
    </div>
    <NGrid :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="2 m:1">
        <NCard title="快速开始">
          <p v-if="!auth.isLoggedIn">尚未登录，可注册前台账号或直接登录。</p>
          <p v-else>
            已登录为 <strong>{{ auth.profile?.username }}</strong>（{{ auth.profile?.userType }}）。
          </p>
          <NSpace style="margin-top: 12px">
            <NButton v-if="!auth.isLoggedIn" type="primary" @click="router.push('/login')">去登录</NButton>
            <NButton v-if="!auth.isLoggedIn" @click="router.push('/register')">注册账号</NButton>
            <NButton v-else type="primary" @click="router.push('/me')">查看我的账号</NButton>
          </NSpace>
        </NCard>
      </NGi>
      <NGi span="2 m:1">
        <NCard title="说明">
          <ul class="tips">
            <li>公开注册只会创建 PORTAL 前台用户</li>
            <li>后台 ADMIN 账号不能在此登录</li>
            <li>公开资料路径：/users/:id</li>
          </ul>
        </NCard>
      </NGi>
    </NGrid>
  </NSpace>
</template>

<style scoped>
.tips {
  margin: 0;
  padding-left: 18px;
  color: #666;
  line-height: 1.8;
}
</style>
