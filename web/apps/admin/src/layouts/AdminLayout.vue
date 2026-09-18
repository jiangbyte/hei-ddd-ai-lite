<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NButton,
  NSpace,
  NTag,
  NText,
  NIcon,
  type MenuOption,
} from 'naive-ui'
import {
  SpeedometerOutline,
  PeopleOutline,
  LogOutOutline,
  ChevronBackOutline,
  ChevronForwardOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
const username = computed(() => auth.profile?.username || '管理员')

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}

function renderIcon(icon: typeof SpeedometerOutline) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: '仪表盘',
    key: '/',
    icon: renderIcon(SpeedometerOutline),
  },
  {
    label: '用户管理',
    key: '/users',
    icon: renderIcon(PeopleOutline),
  },
]

const activeKey = computed(() => (route.path.startsWith('/users') ? '/users' : '/'))

function onMenuUpdate(key: string) {
  router.push(key)
}

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <NLayout has-sider style="height: 100vh">
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      :show-trigger="false"
      :native-scrollbar="false"
    >
      <div class="logo" :class="{ collapsed }">
        {{ collapsed ? 'H' : 'Hei Admin' }}
      </div>
      <NMenu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="onMenuUpdate"
      />
    </NLayoutSider>
    <NLayout>
      <NLayoutHeader bordered class="header">
        <NSpace align="center">
          <NButton quaternary circle @click="toggleCollapsed">
            <template #icon>
              <NIcon size="20">
                <ChevronForwardOutline v-if="collapsed" />
                <ChevronBackOutline v-else />
              </NIcon>
            </template>
          </NButton>
          <NText depth="3">后台管理</NText>
        </NSpace>
        <NSpace align="center">
          <NTag type="warning" size="small">ADMIN</NTag>
          <NText>{{ username }}</NText>
          <NButton quaternary @click="onLogout">
            <template #icon>
              <NIcon><LogOutOutline /></NIcon>
            </template>
            登出
          </NButton>
        </NSpace>
      </NLayoutHeader>
      <NLayoutContent content-style="padding: 20px;" :native-scrollbar="false">
        <RouterView />
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.logo.collapsed {
  font-size: 18px;
}

.header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}
</style>
