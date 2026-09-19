# Hei Web（前台 / 后台分离）

`hei-ddd-ai-lite` 的前端工程：Vue 3 + Vite + TypeScript + **Naive UI** 的 pnpm monorepo。主题主色对齐 Ant Design：`#1677FF`。

| 包 | 说明 | 开发端口 |
|----|------|----------|
| `@hei/portal` | 用户前台（PORTAL） | http://127.0.0.1:5173 |
| `@hei/admin` | 后台管理（ADMIN） | http://127.0.0.1:5174 |
| `@hei/shared` | 共享 http / auth / 主题 / 时间格式化 | — |

**硬规则**：`portal` 与 `admin` 互不引用对方源码，复用只走 `@hei/shared`。

## 用户类型

| 类型 | 说明 |
|------|------|
| `PORTAL` | 前台用户，公开注册创建；只能登录 Portal |
| `ADMIN` | 后台管理员，由种子或后台「新建用户」创建；只能登录 Admin |

登录请求需带 `clientType`：`PORTAL` / `ADMIN`。Token 分端存储，互不串号。

## 准备

```bash
# 后端先就绪：执行 docs/sql/schema-user.sql，启动 hei-ddd-lite-app（默认 8080）

cd web
pnpm install
```

默认管理员：`admin` / `admin123`

## 启动

```bash
pnpm dev:portal   # 前台
pnpm dev:admin    # 后台
```

Vite 将 `/api/**` 代理到 `http://127.0.0.1:8080`。

## 能力概览

**Portal**

- 注册 / 登录 / 登出
- 我的账号、公开用户页
- 顶栏左右分布：左侧导航，右侧登录注册

**Admin**

- 可折叠侧栏（折叠按钮在顶部）
- 仪表盘、用户管理（分页、类型筛选、新建、启用/禁用）

## 其它

```bash
pnpm typecheck
pnpm build:portal
pnpm build:admin
```
