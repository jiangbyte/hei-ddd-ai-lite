# hei-ddd-ai-lite

个人 **AI demo 开发脚手架**：按常见六层工程模型组织（types / api / trigger / domain / infrastructure / app）+ Spring Boot 4 / Spring AI 2 + MySQL / Redis + 简易账户体系 + Vue 前台/后台分离。  
坐标：`io.github.jiangbyte` / `io.github.jiangbyte.hei.*`。

适合在本地快速起一个可登录、可扩展 Agent 能力的 demo，而不是完整 AI 平台。

**做齐：** Entity、ValueObject、AggregateRoot（含事件登记）、DomainEvent、DomainEventPublisher、Repository、DomainService、Factory、Specification、用例编排（Command / Query）、分层依赖倒置；用户注册登录（PORTAL / ADMIN）；Spring AI 依赖接入；Portal / Admin 前端。  
**刻意不做：** Event Sourcing、Saga、强制 CQRS 总线、ACL、多限界上下文拆分、细粒度 RBAC、生产级多租户。

架构图源文件见 [`docs/diagrams/*.drawio`](docs/diagrams/)（可用 [diagrams.net](https://app.diagrams.net/) 打开编辑）；README 嵌入对应 SVG。前端说明见 [`web/README.md`](web/README.md)。

---

## 1. 模块结构（常见六层工程模型）

本仓库按工程实践中常见的六层划分，而不是教科书式「interfaces / application / domain / infrastructure」四层：

```text
hei-ddd-ai-lite/
├── hei-ddd-lite-types/             # 类型层：跨层异常、错误码
├── hei-ddd-lite-api/               # 契约层：I*Service + Request/Response + R
├── hei-ddd-lite-trigger/           # 触发器层：HTTP / JWT / Assembler
├── hei-ddd-lite-domain/            # 领域层：领域模型 + 用例编排（application 包）
├── hei-ddd-lite-infrastructure/    # 基础设施层：仓储实现 / 事件 / MySQL·Redis 等
├── hei-ddd-lite-app/               # 应用启动层：入口与 application.yml
├── web/                            # Vue3 pnpm monorepo（portal / admin / shared）
└── docs/                           # SQL、架构图
```

Maven 坐标统一为 `hei-ddd-lite-*`；Java 包为 `io.github.jiangbyte.hei.*`。  
其中用例编排以 `application` 包放在 domain 模块内（六层模型的常规做法：domain 承载模型与用例，不单独拆 application 模块）。

---

## 2. 六层职责与依赖

![分层架构](docs/diagrams/01-layered-architecture.svg)

| 层 | 模块 | 放什么 | 不放什么 |
|----|------|--------|----------|
| types | `hei-ddd-lite-types` | 业务异常、错误码 | 领域模型、HTTP、AI |
| api | `hei-ddd-lite-api` | 对外契约接口、Request/Response、`R` | Controller、JWT、领域服务 |
| trigger | `hei-ddd-lite-trigger` | Controller（implements `I*Service`）、Assembler、JWT | 业务规则、持久化、契约 DTO |
| domain | `hei-ddd-lite-domain` | 聚合/VO/事件/规约/领域服务 + 用例编排（Command/Query/事务） | HTTP、SQL、对外 DTO |
| infrastructure | `hei-ddd-lite-infrastructure` | 仓储实现、事件发布与消费、Druid / MyBatis-Plus / Redis 等 | 领域规则 |
| app | `hei-ddd-lite-app` | 启动类、`application.yml`、组件扫描 | 业务逻辑 |

依赖方向（**不可反向**）：

```text
hei-ddd-lite-app → hei-ddd-lite-trigger → hei-ddd-lite-api → hei-ddd-lite-types
                        ↘ hei-ddd-lite-domain ─────────────→ hei-ddd-lite-types
hei-ddd-lite-app → hei-ddd-lite-infrastructure → hei-ddd-lite-domain
```

JWT / CORS 属于 **trigger**；数据源 / MyBatis / Redis / S3 / Milvus 属于 **infrastructure**。

---

## 3. DDD 核心概念

![领域概念](docs/diagrams/02-domain-concepts.svg)

| 抽象 | 包位置 | 扩展时怎么用 |
|------|--------|----------------|
| `Entity` | `domain.core` | 有标识、可变；按 ID 相等 |
| `ValueObject` | `domain.core` | 不可变、按值相等；参考 `Username` |
| `AggregateRoot` | `domain.core` | 继承它；行为内 `registerEvent` |
| `DomainEvent` | `domain.core` | 不可变事实；参考 `UserCreatedEvent` |
| `DomainEventPublisher` | `domain.core` | 领域端口；infra 提供 `SpringDomainEventPublisher` |
| `Repository` | `domain.core` | 以聚合为粒度；端口在 domain，实现在 infra |
| `Factory` | `domain.core` | 创建合法聚合；参考 `UserFactory` |
| `Specification` | `domain.core` | 可复用判定；参考 `UsernameFormatSpecification` |
| `DomainService` | `domain.core` / `domain.service` | 跨聚合无状态规则；参考 `UserClientAccessPolicy`（`application` 包 `@Bean` 装配） |
| `ApplicationService` | `application.core` | 编排用例 + `@Transactional`（位于 domain 模块） |
| `Command` / `Query` | `application.core` | 写/读用例入参 |

包级约定见各模块 `package-info.java`。

---

## 4. 账户体系

| 类型 | 说明 |
|------|------|
| `PORTAL` | 前台用户；公开注册创建；只能登录 Portal |
| `ADMIN` | 后台管理员；种子或后台创建；只能登录 Admin |

主要接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 注册 PORTAL 用户 |
| POST | `/auth/login` | 登录（body 需 `clientType`: `PORTAL` / `ADMIN`） |
| POST | `/auth/logout` | 登出（JWT 进 Redis 黑名单） |
| GET | `/auth/me` | 当前登录用户（需登录） |
| GET | `/users/public?userId=` | 公开资料（启用中的 PORTAL） |
| GET | `/admin/users` | 后台用户分页（需 ADMIN） |
| POST | `/admin/users` | 后台创建用户（需 ADMIN） |
| POST | `/admin/users/change-enabled` | 启用/禁用（需 ADMIN） |

建表脚本：[`docs/sql/schema-user.sql`](docs/sql/schema-user.sql)  
默认管理员：`admin` / `admin123`

时间字段 JSON 输出格式：`yyyy-MM-dd HH:mm:ss`。

---

## 5. 基于本脚手架开发

![用例扩展](docs/diagrams/03-usecase-extension.svg)

按**用户管理**竖切（`User` / `UserFactory` / `UserClientAccessPolicy` /
`AuthApplicationService` / `AdminUserApplicationService`）复制扩展，或在其上增加 Agent / 会话等 AI demo 能力：

1. **领域模型**（`domain.model`）：新建聚合继承 `AggregateRoot`，值对象实现 `ValueObject`（参考 `Username`）。
2. **Factory / Event / Spec / DomainService**（`domain.factory` / `event` / `specification` / `service`）：创建时校验、登记事件、跨聚合规则（参考 `UserFactory`、`UserCreatedEvent`、`UsernameFormatSpecification`、`UserClientAccessPolicy`）。
3. **Repository 端口**（`domain.repository`）：`XxxRepository extends Repository<Xxx, ID>`（参考 `UserRepository`）。
4. **用例编排**（domain 模块 `application` 包）：`CreateXxxCommand` / `GetXxxQuery` + `XxxApplicationService`（事务边界；保存后 `pullDomainEvents` 发布）；无 Spring 的领域服务用 `@Bean` 装配（参考 `UserDomainConfiguration`）。
5. **基础设施**（`infrastructure.persistence` / `event`）：实现仓储、事件发布与 `@EventListener` 消费（参考 `UserDomainEventListener`）；对接 Spring AI 客户端等。
6. **触发器**（`trigger.web`）：Controller + Assembler（参考 `AdminUserController` / `AuthController`）；契约 DTO 在 `api`。
7. **启动**：若新增需扫描的 infra 包，更新 `HeiDddAiLiteApplication` 的 `scanBasePackages`。

### 领域事件约定

![事件链路](docs/diagrams/04-domain-event-flow.svg)

1. 聚合行为内 `registerEvent(...)`。
2. 应用服务 `repository.save(aggregate)`。
3. `aggregate.pullDomainEvents()` 取出并清空。
4. `domainEventPublisher.publish(events)`。
5. 基础设施 `@EventListener` 消费（参考 `UserDomainEventListener`）。

事务边界在**应用服务**；默认同步 Spring 事件发布，可替换为 Outbox / MQ。

### 何时用 DomainService / Factory / Specification

- **Factory**：构造复杂或必须保证不变式的聚合创建。
- **Specification**：多处复用的业务判定，避免散落 if。
- **DomainService**：规则不属于单一聚合（如端类型访问策略），且仍是纯领域逻辑；由 `application` 包 `@Bean` 注册。

---

## 6. 快速启动

### 前置

- **JDK 21**
- **MySQL**（库名 `hei`，默认账号见 `application.yml`）
- **Redis**（默认密码见 `application.yml`）
- 执行 [`docs/sql/schema-user.sql`](docs/sql/schema-user.sql)

本地默认密码示例：`infra123!`（与 `hei-ddd-lite-app/src/main/resources/application.yml` 一致，可按环境修改）。

### 后端

```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"

mvn -pl hei-ddd-lite-app -am package -DskipTests
java -jar hei-ddd-lite-app/target/hei-ddd-lite-app-1.0-SNAPSHOT.jar
```

服务：`http://127.0.0.1:8080`  
API 文档：`http://127.0.0.1:8080/doc.html`（OpenAPI：`/v3/api-docs`）

登录示例（前台用户）：

```bash
curl -s -X POST http://127.0.0.1:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"your_portal_user","password":"******","clientType":"PORTAL"}'
```

后台管理员登录将 `clientType` 改为 `ADMIN`。

### 前端

详见 [`web/README.md`](web/README.md)：

```bash
cd web && pnpm install
pnpm dev:portal   # http://127.0.0.1:5173
pnpm dev:admin    # http://127.0.0.1:5174
```

---

## 7. 基础设施

MySQL（Druid + MyBatis-Plus）与 Redis **默认启用**。S3 / Milvus 按开关启用。开关见 `application.yml` 中 `hei.ddd.*`。

| 能力 | 说明 |
|------|------|
| 数据源 | Druid + MySQL，`hei.ddd.datasource.enabled` |
| MyBatis-Plus | 分页 / 元数据填充，`hei.ddd.mybatis.enabled` |
| Redis | JWT 黑名单等，`hei.ddd.redis.enabled` |
| JWT | 内置于 trigger，`hei.ddd.jwt.enabled` |
| S3 | AWS SDK v2（兼容 AWS S3 / MinIO / R2 等），`hei.ddd.s3.enabled`；注入 `S3Client` / `S3ObjectStorage` |
| Milvus | 向量库（milvus-sdk-java），`hei.ddd.milvus.enabled`（本地默认 `false`，起好服务后打开） |
| Knife4j | API 文档（Boot4 专用 `knife4j-openapi3-boot4-spring-boot-starter`），访问 `/doc.html` |
| Spring AI | BOM + openai starter；需 `OPENAI_API_KEY`，可用 `OPENAI_BASE_URL` / `OPENAI_CHAT_MODEL` / `OPENAI_EMBEDDING_MODEL` 覆盖 |

启用 Milvus 后可注入 `MilvusClientV2` 或 `MilvusOperations`。默认连接：`http://127.0.0.1:19530`。

版本在父 POM `<properties>` 中用 `${xxx.version}` 统一管理。

---

## 8. 「不做」清单

- 不做 Event Sourcing / 事件溯源存储
- 不做 Saga / 流程编排框架
- 不做强制 CQRS 总线（Command/Query 仅为入参约定）
- 不做 ACL / 防腐层脚手架
- 不做多限界上下文工程拆分
- 不做细粒度 RBAC（仅 PORTAL / ADMIN 端类型隔离）
- 不做生产级多租户 / 计费 / 模型路由平台

---

## 技术栈

- Java 21 / Spring Boot 4.1.x / Spring AI 2 / Maven 多模块
- Lombok / Hutool / MapStruct / JJWT / BCrypt
- Druid / MyBatis-Plus / Redis（默认）；S3 / Milvus（开关启用）
- Vue 3 / Vite / TypeScript / Pinia / Vue Router / Naive UI / pnpm workspace
