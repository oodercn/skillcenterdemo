# SkillCenter API 规范文档

## 基础信息

- **基础URL**: `/skillcenter/api`
- **响应格式**: JSON
- **统一响应结构**:
  ```json
  {
    "success": true/false,
    "data": {},
    "message": "提示信息",
    "code": 200
  }
  ```

## API 模块列表

### 1. 技能管理 (SkillController) - `/api/skills`

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/skills` | 获取所有技能列表 | ✅ |
| GET | `/api/skills/{id}` | 获取技能详情 | ✅ |
| POST | `/api/skills` | 创建技能 | ✅ |
| PUT | `/api/skills/{id}` | 更新技能 | ✅ |
| DELETE | `/api/skills/{id}` | 删除技能 | ✅ |
| POST | `/api/skills/{id}/execute` | 执行技能 | ✅ |
| GET | `/api/skills/categories` | 获取技能分类 | ✅ |
| GET | `/api/skills/category/{category}` | 按分类获取技能 | ✅ |
| GET | `/api/skills/search` | 搜索技能 | ✅ |
| POST | `/api/skills/batch` | 批量操作技能 | ✅ |

### 2. 管理中心 (AdminController) - `/api/admin`

#### 2.1 仪表盘
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/dashboard/stats` | 获取仪表盘统计数据 | ✅ |

#### 2.2 技能管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/skills` | 获取所有技能 | ✅ |
| POST | `/api/admin/skills` | 添加技能 | ✅ |
| PUT | `/api/admin/skills/{skillId}` | 更新技能 | ✅ |
| DELETE | `/api/admin/skills/{skillId}` | 删除技能 | ✅ |
| POST | `/api/admin/skills/{skillId}/approve` | 审核通过 | ✅ |
| POST | `/api/admin/skills/{skillId}/reject` | 审核拒绝 | ✅ |

#### 2.3 市场管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/market/skills` | 获取市场技能 | ✅ |
| GET | `/api/admin/market/skills/{skillId}` | 获取市场技能详情 | ✅ |
| POST | `/api/admin/market/skills` | 添加市场技能 | ✅ |
| PUT | `/api/admin/market/skills/{skillId}` | 更新市场技能 | ✅ |
| DELETE | `/api/admin/market/skills/{skillId}` | 删除市场技能 | ✅ |
| GET | `/api/admin/market/skills/category/{category}` | 按分类获取 | ✅ |
| GET | `/api/admin/market/skills/popular` | 获取热门技能 | ✅ |
| GET | `/api/admin/market/skills/latest` | 获取最新技能 | ✅ |

#### 2.4 认证管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/authentication/requests` | 获取认证申请列表 | ✅ |
| GET | `/api/admin/authentication/requests/{id}` | 获取认证申请详情 | ✅ |
| POST | `/api/admin/authentication/requests` | 提交认证申请 | ✅ |
| PUT | `/api/admin/authentication/requests/{id}/status` | 更新认证状态 | ✅ |
| DELETE | `/api/admin/authentication/requests/{id}` | 删除认证申请 | ✅ |
| POST | `/api/admin/authentication/issue` | 签发认证 | ✅ |

#### 2.5 群组管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/groups` | 获取群组列表 | ✅ |
| GET | `/api/admin/groups/{groupId}` | 获取群组详情 | ✅ |
| POST | `/api/admin/groups` | 创建群组 | ✅ |
| PUT | `/api/admin/groups/{groupId}` | 更新群组 | ✅ |
| DELETE | `/api/admin/groups/{groupId}` | 删除群组 | ✅ |
| GET | `/api/admin/groups/search` | 搜索群组 | ✅ |
| GET | `/api/admin/groups/{groupId}/members` | 获取群组成员 | ✅ |

#### 2.6 用户管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/users` | 获取用户列表 | ✅ |
| GET | `/api/admin/users/{userId}` | 获取用户详情 | ✅ |
| POST | `/api/admin/users` | 创建用户 | ✅ |
| PUT | `/api/admin/users/{userId}` | 更新用户 | ✅ |
| DELETE | `/api/admin/users/{userId}` | 删除用户 | ✅ |
| GET | `/api/admin/users/search` | 搜索用户 | ✅ |

#### 2.7 托管管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/hosting/instances` | 获取托管实例列表 | ✅ |
| GET | `/api/admin/hosting/instances/{instanceId}` | 获取托管实例详情 | ✅ |
| POST | `/api/admin/hosting/instances` | 创建托管实例 | ✅ |
| PUT | `/api/admin/hosting/instances/{instanceId}` | 更新托管实例 | ✅ |
| DELETE | `/api/admin/hosting/instances/{instanceId}` | 删除托管实例 | ✅ |
| POST | `/api/admin/hosting/instances/{instanceId}/start` | 启动托管实例 | ✅ |
| POST | `/api/admin/hosting/instances/{instanceId}/stop` | 停止托管实例 | ✅ |
| GET | `/api/admin/hosting/instances/search` | 搜索托管实例 | ✅ |
| GET | `/api/admin/hosting/stats` | 获取托管统计 | ✅ |

#### 2.8 存储管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/storage/list` | 获取存储列表 | ✅ |
| GET | `/api/admin/storage/list/type/{type}` | 按类型获取存储 | ✅ |
| GET | `/api/admin/storage/{storageId}` | 获取存储详情 | ✅ |
| POST | `/api/admin/storage` | 创建存储 | ✅ |
| DELETE | `/api/admin/storage/{storageId}` | 删除存储 | ✅ |
| GET | `/api/admin/storage/stats` | 获取存储统计 | ✅ |

#### 2.9 系统管理
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/admin/system/info` | 获取系统信息 | ✅ |
| GET | `/api/admin/system/stats` | 获取系统统计 | ✅ |
| GET | `/api/admin/system/config` | 获取系统配置 | ✅ |
| PUT | `/api/admin/system/config` | 更新系统配置 | ✅ |
| GET | `/api/admin/system/logs` | 获取系统日志 | ✅ |
| POST | `/api/admin/system/restart` | 重启系统 | ✅ |
| POST | `/api/admin/system/shutdown` | 关闭系统 | ✅ |

### 3. 个人中心 (PersonalController) - `/api/personal`

#### 3.1 仪表盘
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/dashboard/stats` | 获取个人仪表盘统计 | ✅ |

#### 3.2 我的技能
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/skills` | 获取我的技能 | ✅ |
| GET | `/api/personal/skills/{skillId}` | 获取技能详情 | ✅ |
| POST | `/api/personal/skills` | 创建技能 | ✅ |
| PUT | `/api/personal/skills/{skillId}` | 更新技能 | ✅ |
| DELETE | `/api/personal/skills/{skillId}` | 删除技能 | ✅ |
| POST | `/api/personal/skills/{skillId}/execute` | 执行技能 | ✅ |

#### 3.3 我的群组
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/groups` | 获取我的群组 | ✅ |
| GET | `/api/personal/groups/{groupId}` | 获取群组详情 | ✅ |
| POST | `/api/personal/groups` | 创建群组 | ✅ |
| PUT | `/api/personal/groups/{groupId}` | 更新群组 | ✅ |
| DELETE | `/api/personal/groups/{groupId}` | 删除群组 | ✅ |

#### 3.4 群组技能
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/groups/skills` | 获取所有群组技能 | ✅ |
| GET | `/api/personal/groups/{groupId}/skills` | 获取指定群组技能 | ✅ |
| POST | `/api/personal/groups/skills` | 添加群组技能 | ✅ |
| DELETE | `/api/personal/groups/skills/{skillId}` | 删除群组技能 | ✅ |

#### 3.5 群组成员
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/groups/{groupId}/members` | 获取群组成员 | ✅ |
| POST | `/api/personal/groups/{groupId}/members` | 添加群组成员 | ✅ |
| PUT | `/api/personal/groups/{groupId}/members/{memberId}` | 更新群组成员 | ✅ |
| DELETE | `/api/personal/groups/{groupId}/members/{memberId}` | 删除群组成员 | ✅ |

#### 3.6 执行历史
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/executions` | 获取执行历史 | ✅ |
| GET | `/api/personal/executions/{executionId}` | 获取执行详情 | ✅ |
| DELETE | `/api/personal/executions/{executionId}` | 删除执行记录 | ✅ |
| DELETE | `/api/personal/executions` | 清空执行历史 | ✅ |

#### 3.7 个人身份
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/identity` | 获取个人身份 | ✅ |
| PUT | `/api/personal/identity` | 更新个人身份 | ✅ |
| GET | `/api/personal/identity/mappings` | 获取身份映射 | ✅ |

#### 3.8 帮助与设置
| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/personal/help` | 获取帮助文档 | ✅ |
| GET | `/api/personal/settings` | 获取个人设置 | ✅ |
| PUT | `/api/personal/settings` | 更新个人设置 | ✅ |
| GET | `/api/personal/features` | 获取功能开关 | ✅ |

## 数据模型规范

### 技能 (Skill)
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "category": "string",
  "status": "string (active/pending/inactive)",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### 群组 (Group)
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "memberCount": "number",
  "createdAt": "datetime",
  "status": "string"
}
```

### 群组成员 (GroupMember)
```json
{
  "id": "string",
  "groupId": "string",
  "userId": "string",
  "username": "string",
  "role": "string (admin/member)",
  "joinedAt": "datetime",
  "status": "string"
}
```

### 群组技能 (GroupSkill)
```json
{
  "id": "string",
  "groupId": "string",
  "groupName": "string",
  "skillId": "string",
  "skillName": "string",
  "sharedBy": "string",
  "sharedAt": "datetime",
  "description": "string",
  "status": "string"
}
```

### 用户 (User)
```json
{
  "id": "string",
  "username": "string",
  "name": "string",
  "email": "string",
  "phone": "string",
  "avatar": "string",
  "bio": "string",
  "createdAt": "datetime",
  "status": "string"
}
```

## 状态码规范

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有 API 响应都遵循统一的响应结构
2. 列表数据支持分页（通过 page 和 pageSize 参数）
3. 搜索功能支持 keyword 参数
4. 时间字段使用 ISO 8601 格式
5. 状态字段使用小写字符串
