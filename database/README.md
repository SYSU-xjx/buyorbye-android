# 数据库（MySQL）· buy or bye

本目录存放《钱包请等等 / Buy or Bye》服务端数据库的**建表脚本**与**表结构文档**。

> ⚠️ 这是 **服务端（Spring Boot + MySQL）** 的库，不是 Android 端本地库。
> Android App 不直连 MySQL，必须经由后端 REST API（`http://<host>:8080/api/`）。
> Android 端本地只保留 Room（`wheel_runs` 转盘历史这类离线数据）。

## 目录结构

```
database/
├── README.md                     ← 本文件
├── schema/
│   └── buy_or_bye_schema.sql     ← 建库建表脚本（可直接执行，幂等：CREATE DATABASE IF NOT EXISTS）
└── docs/
    ├── 数据库表结构_最终版.md      ← 表结构文档（GitHub 可直接渲染，推荐看这个）
    └── 数据库表结构_最终版.pdf     ← 同一份文档的 PDF 版（交作业/给组员看）
```

## 环境

| 项 | 值 |
|---|---|
| DBMS | MySQL 8.4+（8.0.16 起才真正强制执行 CHECK 约束） |
| 引擎 / 字符集 | InnoDB / `utf8mb4` / `utf8mb4_0900_ai_ci` |
| 库名 | `buy_or_bye` |
| 时区约定 | 所有 `DATETIME(6)` 存 **UTC**，展示层转时区 |
| 金额约定 | 一律 `DECIMAL`，Java 侧 `BigDecimal`，禁止 `float/double` |

## 建库步骤

### 方式一：命令行

```sh
mysql -u root -p < database/schema/buy_or_bye_schema.sql
```

脚本末尾自带自检，正常会输出：

```
table_count
30
```

以及 38 条外键记录。

### 方式二：图形客户端（Navicat / DataGrip / Workbench）

1. 新建连接（Host `127.0.0.1`、Port `3306`、User `root`）。
2. 打开 `schema/buy_or_bye_schema.sql`，全选执行。
3. 刷新左侧列表，应出现 `buy_or_bye` 库与 30 张表。

### 重跑 / 重置

脚本是 `CREATE TABLE`（非 `IF NOT EXISTS`），**表已存在时会报 `1050 Table already exists`**。
需要重来时，先解开脚本第 35–43 行被注释掉的「0. 重置脚本」段，或手动：

```sql
DROP DATABASE buy_or_bye;
```

## 表清单（30 张）

| 域 | 表 | 说明 |
|---|---|---|
| 身份鉴权 | `users` `auth_sessions` `user_devices` `reputation_accounts` `user_streaks` | 账号、刷新令牌、FCM 设备、信誉分汇总、坚持天数 |
| 群组 | `app_groups` `group_members` | 审批群与成员关系（群内权限唯一来源） |
| 广场频道 | `channels` `user_channels` `tags` | 频道订阅、16 个预设标签 |
| 审批核心 | `purchase_proposals` `poll_options` `purchase_proposal_images` `purchase_proposal_channels` `proposal_tags` | 二元裁决 / 选项投票共用一张主表 |
| 投票互动 | `decision_votes` `reputation_ledger` `proposal_watches` `proposal_comments` `proposal_updates` `flowers` | 投票 append-only，只取最新一票计票 |
| 汇率榜单 | `exchange_rate_snapshots` `weekly_rankings` `weekly_ranking_entries` | 跨币种榜单可复现 |
| 治理运营 | `blocked_words` `blocked_users` `feedback` `feedback_images` `notifications` | 屏蔽词、黑名单、反馈、站内通知 |
| 可选 P2 | `wheel_runs` | 转盘历史 |

初始化数据：`tags` 16 行、`channels` 5 行（脚本 §9）。

## 与后端对接

后端仓库（Spring Boot）里应把本脚本放到 Flyway 迁移目录：

```
src/main/resources/db/migration/V1__init.sql
```

即：复制 `schema/buy_or_bye_schema.sql` 并重命名为 `V1__init.sql`。
注意 Flyway 要求 `CREATE DATABASE` / `USE` 不出现（库由连接串指定），需要删掉脚本第 24–28 行。

`application.yml` 参考：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/buy_or_bye?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
    username: root
    password: <本地密码>
  jpa:
    hibernate:
      ddl-auto: validate   # 表由 SQL 建，禁止 update/create
    properties:
      hibernate.jdbc.time_zone: UTC
```

Android 端 `app/build.gradle.kts` 的 `API_BASE_URL` 默认 `http://10.0.2.2:8080/api/`（模拟器访问宿主机），真机需改成电脑局域网 IP。

## 已知待确认项

- `reputation_ledger.source_vote_id` / `proposal_id` 目前是 `NOT NULL`，但 `source_type` 含 `DAILY_ACTIVE`、`ADMIN_ADJUST` 这类**没有投票来源**的场景，届时插不进去。建议后续放宽为可空（`UNIQUE` 允许多个 NULL，不会破坏幂等）。
- `purchase_proposals.group_id` 未设 `ON DELETE`，群被删除时会被外键挡住，属预期行为（群只做 `CLOSED` 软关闭）。
