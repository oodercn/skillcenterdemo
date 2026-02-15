# Agent-SkillCenter SDK 0.7.0 模块划分方案

## 一、模块架构总览

```
agent-skillcenter/
├── src/main/java/net/ooder/
│   ├── skillcenter/                    # 核心业务模块
│   │   ├── runtime/                    # 运行时管理 (SDK 0.7.0)
│   │   ├── resources/                  # 资源管理 (SDK 0.7.0)
│   │   ├── lifecycle/                  # 生命周期管理 (SDK 0.7.0)
│   │   │   └── deployment/             # 部署管理
│   │   ├── capability/                 # 能力管理 (SDK 0.7.0)
│   │   ├── scene/                      # 场景管理 (SDK 0.7.0)
│   │   ├── config/                     # 配置管理 (SDK 0.7.0)
│   │   ├── dto/                        # 数据传输对象
│   │   ├── service/                    # 服务接口层
│   │   │   └── impl/                   # 服务实现
│   │   ├── market/                     # 技能市场
│   │   ├── manager/                    # 管理器
│   │   ├── execution/                  # 执行管理
│   │   └── model/                      # 领域模型
│   └── nexus/skillcenter/              # Web层
│       ├── controller/                 # 控制器层
│       ├── dto/                        # Web DTO
│       └── model/                      # Web模型
└── src/main/resources/
    └── static/console/                 # 前端页面
```

---

## 二、SDK 0.7.0 核心模块

### 2.1 Runtime 运行时管理

| 文件 | 说明 |
|------|------|
| RuntimeManager.java | 运行时管理器 |
| RuntimeFactory.java | 运行时工厂 |
| RuntimeExecutor.java | 运行时执行器接口 |
| impl/JavaRuntimeExecutor.java | Java运行时实现 |
| impl/PythonRuntimeExecutor.java | Python运行时实现 |
| impl/NodeRuntimeExecutor.java | Node.js运行时实现 |
| model/RuntimeConfig.java | 运行时配置 |
| model/RuntimeStatus.java | 运行时状态 |

### 2.2 Resources 资源管理

| 文件 | 说明 |
|------|------|
| ResourceManager.java | 资源管理器 |
| ResourceAllocator.java | 资源分配器 |
| model/ResourceRequest.java | 资源请求 |
| model/ResourceLimit.java | 资源限制 |
| model/NetworkPolicy.java | 网络策略 |

### 2.3 Deployment 部署管理

| 文件 | 说明 |
|------|------|
| DeploymentManager.java | 部署管理器 |
| HealthChecker.java | 健康检查器 |
| StartupManager.java | 启动管理器 |
| model/DeploymentMode.java | 部署模式枚举 |
| model/DeploymentConfig.java | 部署配置 |
| model/HealthCheckConfig.java | 健康检查配置 |
| model/StartupConfig.java | 启动配置 |

### 2.4 Capability 能力管理

| 文件 | 说明 |
|------|------|
| CapabilityRegistry.java | 能力注册表 |
| model/CapabilityCategory.java | 能力分类枚举 |
| model/CapabilityDefinition.java | 能力定义 |
| model/CapabilityParameter.java | 能力参数 |
| model/CapabilityReturn.java | 返回值定义 |

### 2.5 Scene 场景管理

| 文件 | 说明 |
|------|------|
| SceneManager.java | 场景管理器 |
| model/SceneDefinition.java | 场景定义 |
| model/SceneRole.java | 场景角色 |
| model/CommunicationProtocol.java | 通信协议枚举 |
| model/SecurityPolicy.java | 安全策略枚举 |

### 2.6 Config 配置管理

| 文件 | 说明 |
|------|------|
| ConfigValidator.java | 配置验证器 |
| model/ConfigType.java | 配置类型枚举 |
| model/ConfigItem.java | 配置项 |
| model/ValidationRule.java | 验证规则 |

---

## 三、Service 服务层

### 3.1 服务接口

| Service | 说明 | Mock实现 | SDK实现 |
|---------|------|----------|---------|
| AdminService | 管理服务 | ✅ | ✅ |
| SkillService | 技能服务 | ✅ | ✅ |
| UserService | 用户服务 | ✅ | ✅ |
| GroupService | 群组服务 | ✅ | ✅ |
| MarketService | 市场服务 | ✅ | ✅ |
| StorageService | 存储服务 | ✅ | ✅ |
| ExecutionService | 执行服务 | ✅ | ✅ |
| ShareService | 分享服务 | ✅ | ✅ |
| PersonalService | 个人服务 | ✅ | ✅ |
| SystemService | 系统服务 | ✅ | ✅ |
| AuthenticationService | 认证服务 | ✅ | ✅ |
| **HostingService** | 托管服务 | ✅ | ✅ |

### 3.2 模式切换配置

```yaml
skillcenter:
  sdk:
    mode: mock  # mock | sdk
```

---

## 四、Controller 控制器层

### 4.1 模块分布

| 模块 | Controller | API端点数 |
|------|------------|-----------|
| 系统模块 | SystemController | 12 |
| 管理模块 | AdminController | 14 |
| 技能模块 | SkillController, SkillDiscoveryController | 12 |
| 市场模块 | MarketController | 10 |
| 存储模块 | StorageController | 10 |
| 执行模块 | ExecutionController | 7 |
| 个人模块 | PersonalController | 17 |
| 仪表盘模块 | DashboardController | 4 |
| 分享模块 | ShareController | 4 |
| P2P网络模块 | P2PController | 11 |
| 菜单模块 | MenuController | 1 |
| **托管模块** | **HostingController** | **14** |
| **总计** | **14个Controller** | **116个端点** |

### 4.2 Hosting托管模块API

| 端点 | 说明 |
|------|------|
| POST /api/hosting/instances | 获取所有托管实例 |
| POST /api/hosting/instances/page | 分页获取托管实例 |
| POST /api/hosting/instances/get | 获取单个实例 |
| POST /api/hosting/instances/create | 创建托管实例 |
| POST /api/hosting/instances/update | 更新托管实例 |
| POST /api/hosting/instances/delete | 删除托管实例 |
| POST /api/hosting/instances/start | 启动实例 |
| POST /api/hosting/instances/stop | 停止实例 |
| POST /api/hosting/instances/restart | 重启实例 |
| POST /api/hosting/instances/status | 获取实例状态 |
| POST /api/hosting/instances/health | 获取实例健康状态 |
| POST /api/hosting/instances/scale | 扩缩容实例 |
| POST /api/hosting/instances/resources | 更新资源配置 |
| POST /api/hosting/stats | 获取托管统计 |

---

## 五、前端页面模块

### 5.1 页面与菜单匹配

| 菜单 | 页面 | API | 状态 |
|------|------|-----|------|
| 仪表盘 | dashboard.html | /api/dashboard | ✅ |
| 个人中心 | personal/* | /api/personal/* | ✅ |
| 技能管理 | skill.html | /api/skills/* | ✅ |
| 技能市场 | market.html | /api/market/* | ✅ |
| 技能执行 | execution.html | /api/execution/* | ✅ |
| 技能安装 | install.html | /api/discovery/* | ✅ |
| 存储管理 | storage.html | /api/storage/* | ✅ |
| 系统设置 | system.html | /api/system/* | ✅ |
| 帮助中心 | help.html | /api/personal/help | ✅ |
| 管理中心 | admin/* | /api/admin/* | ✅ |
| **远程托管** | **remote-hosting.html** | **/api/hosting/*** | **✅** |

---

## 六、SDK 0.7.0 闭环流程

### 6.1 技能生命周期

```
┌─────────────────────────────────────────────────────────────┐
│                    技能生命周期闭环                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐  │
│  │  发现   │───▶│  安装   │───▶│  配置   │───▶│  部署   │  │
│  │Discovery│    │ Install │    │ Config  │    │Deploy   │  │
│  └─────────┘    └─────────┘    └─────────┘    └─────────┘  │
│       ▲                                            │        │
│       │                                            ▼        │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐  │
│  │  市场   │◀───│  分享   │◀───│  执行   │◀───│  托管   │  │
│  │ Market  │    │  Share  │    │Execute  │    │ Hosting │  │
│  └─────────┘    └─────────┘    └─────────┘    └─────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 模块依赖关系

```
┌─────────────────────────────────────────────────────────────┐
│                    模块依赖关系图                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                     ┌─────────────┐                         │
│                     │ Controller  │                         │
│                     └──────┬──────┘                         │
│                            │                                │
│                     ┌──────▼──────┐                         │
│                     │   Service   │                         │
│                     └──────┬──────┘                         │
│                            │                                │
│        ┌───────────────────┼───────────────────┐           │
│        │                   │                   │           │
│  ┌─────▼─────┐       ┌─────▼─────┐       ┌─────▼─────┐     │
│  │  Runtime  │       │Deployment │       │  Hosting  │     │
│  └─────┬─────┘       └─────┬─────┘       └─────┬─────┘     │
│        │                   │                   │           │
│  ┌─────▼─────┐       ┌─────▼─────┐       ┌─────▼─────┐     │
│  │ Resources │       │  Health   │       │ Capability│     │
│  └─────┬─────┘       └─────┬─────┘       └─────┬─────┘     │
│        │                   │                   │           │
│        └───────────────────┼───────────────────┘           │
│                            │                                │
│                     ┌──────▼──────┐                         │
│                     │   Config    │                         │
│                     └─────────────┘                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 七、配置与部署

### 7.1 应用配置

```yaml
server:
  port: 8082
  servlet:
    context-path: /skillcenter

skillcenter:
  sdk:
    mode: mock  # mock | sdk
  execution-timeout: 30000
  max-concurrent-executions: 100
  storage-path: ./skillcenter-data
```

### 7.2 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| HTTP | 8082 | Web服务 |
| Context-Path | /skillcenter | 应用上下文 |

---

## 八、版本信息

| 项目 | 版本 |
|------|------|
| SDK版本 | 0.7.0 |
| Spring Boot | 2.7.0 |
| Java兼容 | Java 8 |
| 更新日期 | 2026-02-15 |
