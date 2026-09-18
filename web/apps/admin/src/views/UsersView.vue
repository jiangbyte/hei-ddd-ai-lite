<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPagination,
  NSelect,
  NSpace,
  NTag,
  NText,
  useDialog,
  useMessage,
  type DataTableColumns,
  type SelectOption,
} from 'naive-ui'
import { ApiError, formatDateTime } from '@hei/shared'
import type { UserProfile, UserType } from '@hei/shared'
import { adminUserApi } from '@/api'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const creating = ref(false)
const createVisible = ref(false)
const records = ref<UserProfile[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  userType: null as UserType | null,
})

const createForm = reactive({
  username: '',
  password: '',
  userType: 'PORTAL' as UserType,
})

const typeOptions: SelectOption[] = [
  { label: '前台 PORTAL', value: 'PORTAL' },
  { label: '后台 ADMIN', value: 'ADMIN' },
]

const columns: DataTableColumns<UserProfile> = [
  { title: 'ID', key: 'userId', width: 90 },
  { title: '用户名', key: 'username', minWidth: 140 },
  {
    title: '类型',
    key: 'userType',
    width: 120,
    render(row) {
      return h(
        NTag,
        { type: row.userType === 'ADMIN' ? 'warning' : 'success', size: 'small' },
        { default: () => row.userType },
      )
    },
  },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render(row) {
      return h(
        NTag,
        { type: row.enabled ? 'success' : 'default', size: 'small' },
        { default: () => (row.enabled ? '启用' : '禁用') },
      )
    },
  },
  {
    title: '创建时间',
    key: 'createTime',
    minWidth: 170,
    render(row) {
      return formatDateTime(row.createTime)
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render(row) {
      return h(
        NButton,
        {
          text: true,
          type: 'primary',
          onClick: () => onToggleEnabled(row),
        },
        { default: () => (row.enabled ? '禁用' : '启用') },
      )
    },
  },
]

async function loadUsers() {
  loading.value = true
  try {
    const page = await adminUserApi.list({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      username: query.username || undefined,
      userType: query.userType || undefined,
    })
    records.value = page.records
    total.value = page.total
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.pageNo = 1
  loadUsers()
}

function onToggleEnabled(row: UserProfile) {
  const next = !row.enabled
  dialog.warning({
    title: '提示',
    content: `确认将用户「${row.username}」${next ? '启用' : '禁用'}？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await adminUserApi.changeEnabled(row.userId, next)
        message.success('已更新')
        await loadUsers()
      } catch (error) {
        message.error(error instanceof ApiError ? error.message : '操作失败')
      }
    },
  })
}

async function onCreate() {
  if (!createForm.username.trim() || !createForm.password) {
    message.warning('请填写用户名和密码')
    return
  }
  creating.value = true
  try {
    await adminUserApi.create({
      username: createForm.username.trim(),
      password: createForm.password,
      userType: createForm.userType,
    })
    message.success('创建成功')
    createVisible.value = false
    createForm.username = ''
    createForm.password = ''
    createForm.userType = 'PORTAL'
    await loadUsers()
  } catch (error) {
    message.error(error instanceof ApiError ? error.message : '创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <NSpace vertical :size="16">
    <NSpace justify="space-between" align="center">
      <NText style="font-size: 20px; font-weight: 600">用户管理</NText>
      <NButton type="primary" @click="createVisible = true">新建用户</NButton>
    </NSpace>

    <NCard>
      <NSpace>
        <NInput v-model:value="query.username" clearable placeholder="用户名" style="width: 180px" @keyup.enter="onSearch" />
        <NSelect
          v-model:value="query.userType"
          clearable
          placeholder="用户类型"
          :options="typeOptions"
          style="width: 160px"
        />
        <NButton type="primary" @click="onSearch">查询</NButton>
      </NSpace>

      <NDataTable
        style="margin-top: 16px"
        :loading="loading"
        :columns="columns"
        :data="records"
        :bordered="false"
        :single-line="false"
      />

      <NSpace justify="end" style="margin-top: 16px">
        <NPagination
          v-model:page="query.pageNo"
          v-model:page-size="query.pageSize"
          :item-count="total"
          @update:page="loadUsers"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="createVisible" preset="card" title="新建用户" style="width: 420px">
      <NForm>
        <NFormItem label="用户名">
          <NInput v-model:value="createForm.username" />
        </NFormItem>
        <NFormItem label="密码">
          <NInput v-model:value="createForm.password" type="password" show-password-on="click" />
        </NFormItem>
        <NFormItem label="用户类型">
          <NSelect v-model:value="createForm.userType" :options="typeOptions" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="createVisible = false">取消</NButton>
          <NButton type="primary" :loading="creating" @click="onCreate">创建</NButton>
        </NSpace>
      </template>
    </NModal>
  </NSpace>
</template>
