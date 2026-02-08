# SkillCenter Web访问仿真测试用例

## 测试用例设计原则

1. **前后端数据校验**：所有输入数据前后端都需校验
2. **边界值测试**：测试最小值、最大值、临界值
3. **异常流程测试**：测试错误处理、超时、网络异常
4. **并发测试**：测试多用户同时操作
5. **安全测试**：测试越权访问、SQL注入、XSS攻击

---

## 一、技能管理模块测试用例

### TC-SKILL-001: 技能发布完整流程

```yaml
测试ID: TC-SKILL-001
测试名称: 技能发布完整流程测试
优先级: P0
测试类型: 功能测试

前置条件:
  - 用户已登录
  - 用户有技能发布权限
  - 网络连接正常

测试步骤:
  步骤1:
    操作: 访问技能发布页面
    输入: URL: /skillcenter/console/pages/skill-publish.html
    预期结果:
      - HTTP状态码: 200
      - 页面标题: "发布新技能"
      - 表单字段完整显示
      
  步骤2:
    操作: 填写技能名称（有效值）
    输入: 
      field: skillName
      value: "文本转换技能"
    预期结果:
      - 前端校验通过
      - 无错误提示
      
  步骤3:
    操作: 填写技能名称（边界值-最小长度）
    输入:
      field: skillName
      value: "a"
    预期结果:
      - 前端校验失败
      - 错误提示: "技能名称至少2个字符"
      
  步骤4:
    操作: 填写技能名称（边界值-最大长度）
    输入:
      field: skillName
      value: "a" * 51
    预期结果:
      - 前端校验失败
      - 错误提示: "技能名称最多50个字符"
      
  步骤5:
    操作: 填写技能描述（XSS攻击测试）
    输入:
      field: skillDescription
      value: "<script>alert('xss')</script>"
    预期结果:
      - 前端转义处理
      - 后端存储时转义
      - 页面显示时转义
      
  步骤6:
    操作: 提交完整表单
    输入:
      skillName: "测试技能"
      description: "这是一个测试技能"
      category: "text"
      code: "console.log('hello')"
    API调用:
      method: POST
      url: /api/skills
      headers:
        Content-Type: application/json
      body:
        name: "测试技能"
        description: "这是一个测试技能"
        category: "text"
        code: "console.log('hello')"
    预期结果:
      - HTTP状态码: 200
      - 响应格式:
          success: true
          code: 200
          message: "技能发布成功"
          data:
            id: "skill-xxx"
            name: "测试技能"
            status: "PENDING"
      - 数据库验证: skills表新增记录
      
  步骤7:
    操作: 验证技能列表
    API调用:
      method: GET
      url: /api/skills
    预期结果:
      - 响应包含新发布的技能
      - 技能状态为PENDING

后置条件:
  - 清理测试数据
  - 删除测试技能

测试数据:
  有效数据:
    - name: "文本转换"
    - description: "将文本转换为大写"
    - category: "text"
    - code: "return input.toUpperCase();"
  
  无效数据:
    - name: "" (空)
    - name: "a" (太短)
    - name: "a" * 100 (太长)
    - description: "" (空)
    - code: "" (空)
```

### TC-SKILL-002: 技能执行流程

```yaml
测试ID: TC-SKILL-002
测试名称: 技能执行流程测试
优先级: P0
测试类型: 功能测试

前置条件:
  - 技能已发布并通过审核
  - 技能状态为ACTIVE

测试步骤:
  步骤1:
    操作: 获取技能详情
    API调用:
      method: GET
      url: /api/skills/{skillId}
    预期结果:
      - HTTP状态码: 200
      - 技能信息完整
      
  步骤2:
    操作: 同步执行技能
    API调用:
      method: POST
      url: /api/execution/execute/{skillId}
      body:
        input: "hello world"
    预期结果:
      - HTTP状态码: 200
      - 响应时间: < 5000ms
      - 响应格式:
          success: true
          data:
            executionId: "exec-xxx"
            status: "SUCCESS"
            output: "HELLO WORLD"
            duration: 100
            
  步骤3:
    操作: 异步执行技能
    API调用:
      method: POST
      url: /api/execution/execute-async/{skillId}
      body:
        input: "test"
    预期结果:
      - HTTP状态码: 200
      - 立即返回executionId
      - status: "PENDING"
      
  步骤4:
    操作: 查询执行状态
    API调用:
      method: GET
      url: /api/execution/status/{executionId}
    预期结果:
      - 状态最终变为SUCCESS或FAILED
      
  步骤5:
    操作: 获取执行结果
    API调用:
      method: GET
      url: /api/execution/result/{executionId}
    预期结果:
      - 返回完整执行结果
      - 包含input、output、duration

测试数据:
  输入数据:
    - "hello world"
    - "12345"
    - "!@#$%"
    - "" (空字符串)
    - "a" * 10000 (长文本)
```

### TC-SKILL-003: 技能更新与删除

```yaml
测试ID: TC-SKILL-003
测试名称: 技能更新与删除测试
优先级: P1
测试类型: 功能测试

测试步骤:
  步骤1:
    操作: 更新技能信息
    API调用:
      method: PUT
      url: /api/skills/{skillId}
      body:
        name: "更新后的名称"
        description: "更新后的描述"
    预期结果:
      - HTTP状态码: 200
      - 数据库记录已更新
      
  步骤2:
    操作: 验证更新结果
    API调用:
      method: GET
      url: /api/skills/{skillId}
    预期结果:
      - 返回更新后的数据
      
  步骤3:
    操作: 删除技能
    API调用:
      method: DELETE
      url: /api/skills/{skillId}
    预期结果:
      - HTTP状态码: 200
      - 数据库记录已删除或标记删除
      
  步骤4:
    操作: 验证删除结果
    API调用:
      method: GET
      url: /api/skills/{skillId}
    预期结果:
      - HTTP状态码: 404
      - 或返回status为DELETED
```

---

## 二、群组管理模块测试用例

### TC-GROUP-001: 群组CRUD完整流程

```yaml
测试ID: TC-GROUP-001
测试名称: 群组增删改查完整流程
优先级: P0
测试类型: 功能测试

前置条件:
  - 管理员已登录

测试步骤:
  步骤1:
    操作: 创建群组
    API调用:
      method: POST
      url: /api/admin/groups
      body:
        name: "测试群组"
        description: "这是一个测试群组"
    预期结果:
      - HTTP状态码: 200
      - 返回群组ID
      
  步骤2:
    操作: 获取群组列表
    API调用:
      method: GET
      url: /api/admin/groups
    预期结果:
      - 包含新创建的群组
      - memberCount: 0
      
  步骤3:
    操作: 搜索群组
    API调用:
      method: GET
      url: /api/admin/groups/search?keyword=测试
    预期结果:
      - 返回匹配的群组列表
      
  步骤4:
    操作: 更新群组
    API调用:
      method: PUT
      url: /api/admin/groups/{groupId}
      body:
        name: "更新后的群组名"
        description: "更新后的描述"
    预期结果:
      - HTTP状态码: 200
      
  步骤5:
    操作: 删除群组
    API调用:
      method: DELETE
      url: /api/admin/groups/{groupId}
    预期结果:
      - HTTP状态码: 200

边界值测试:
  - 群组名称: 空、1字符、30字符、31字符
  - 群组描述: 空、200字符、201字符
```

---

## 三、存储管理模块测试用例

### TC-STORAGE-001: 存储备份恢复流程

```yaml
测试ID: TC-STORAGE-001
测试名称: 存储备份与恢复测试
优先级: P1
测试类型: 功能测试

测试步骤:
  步骤1:
    操作: 获取存储状态
    API调用:
      method: GET
      url: /api/storage/status
    预期结果:
      - 返回存储使用情况
      
  步骤2:
    操作: 创建备份
    API调用:
      method: POST
      url: /api/storage/backup
      body:
        name: "test-backup"
        description: "测试备份"
    预期结果:
      - HTTP状态码: 200
      - 备份任务开始执行
      
  步骤3:
    操作: 获取备份列表
    API调用:
      method: GET
      url: /api/storage/backups
    预期结果:
      - 包含新创建的备份
      
  步骤4:
    操作: 恢复备份
    API调用:
      method: POST
      url: /api/storage/restore/{backupName}
    预期结果:
      - HTTP状态码: 200
      - 数据恢复成功
      
  步骤5:
    操作: 删除备份
    API调用:
      method: DELETE
      url: /api/storage/backups/{backupName}
    预期结果:
      - HTTP状态码: 200
      - 备份文件已删除
```

---

## 四、系统管理模块测试用例

### TC-SYSTEM-001: 系统配置管理

```yaml
测试ID: TC-SYSTEM-001
测试名称: 系统配置管理测试
优先级: P1
测试类型: 功能测试

测试步骤:
  步骤1:
    操作: 获取系统配置
    API调用:
      method: GET
      url: /api/system/config
    预期结果:
      - 返回系统配置信息
      
  步骤2:
    操作: 更新系统配置
    API调用:
      method: PUT
      url: /api/system/config
      body:
        maxSkillsPerUser: 50
        autoBackup: true
    预期结果:
      - HTTP状态码: 200
      - 配置已更新
      
  步骤3:
    操作: 获取系统健康状态
    API调用:
      method: GET
      url: /api/system/health
    预期结果:
      - 返回各组件健康状态
      - status: HEALTHY/DEGRADED/UNHEALTHY
```

---

## 五、并发测试用例

### TC-CONCURRENT-001: 并发技能执行

```yaml
测试ID: TC-CONCURRENT-001
测试名称: 并发技能执行测试
优先级: P1
测试类型: 性能测试

测试场景:
  - 100个用户同时执行同一个技能
  - 持续时间: 60秒

测试步骤:
  步骤1:
    操作: 准备测试数据
    - 创建测试技能
    - 准备100个虚拟用户
    
  步骤2:
    操作: 启动并发测试
    - 使用JMeter或自定义脚本
    - 100线程同时执行
    
  预期结果:
    - 成功率: > 99%
    - 平均响应时间: < 3000ms
    - 95%响应时间: < 5000ms
    - 无内存泄漏
    - 无死锁
    
  验证点:
    - 所有执行记录正确保存
    - 执行结果准确
    - 系统资源使用正常
```

---

## 六、安全测试用例

### TC-SECURITY-001: SQL注入防护

```yaml
测试ID: TC-SECURITY-001
测试名称: SQL注入防护测试
优先级: P0
测试类型: 安全测试

测试步骤:
  步骤1:
    操作: 尝试SQL注入攻击
    API调用:
      method: GET
      url: /api/skills/search?keyword=' OR '1'='1
    预期结果:
      - 返回空结果或正常搜索结果
      - 不返回所有数据
      - 无SQL错误信息泄露
      
  步骤2:
    操作: 尝试SQL注入攻击（POST请求）
    API调用:
      method: POST
      url: /api/skills
      body:
        name: "test'; DROP TABLE skills; --"
        description: "正常描述"
    预期结果:
      - 数据被转义或拒绝
      - 数据库表未被删除
```

### TC-SECURITY-002: 越权访问测试

```yaml
测试ID: TC-SECURITY-002
测试名称: 越权访问测试
优先级: P0
测试类型: 安全测试

测试步骤:
  步骤1:
    操作: 普通用户访问管理员接口
    API调用:
      method: GET
      url: /api/admin/groups
      headers:
        Authorization: "普通用户Token"
    预期结果:
      - HTTP状态码: 403
      - 错误信息: "无权限访问"
      
  步骤2:
    操作: 用户A操作用户B的技能
    API调用:
      method: DELETE
      url: /api/skills/{userB-skill-id}
      headers:
        Authorization: "用户AToken"
    预期结果:
      - HTTP状态码: 403
      - 无法删除他人技能
```

---

## 七、异常场景测试用例

### TC-ERROR-001: 网络异常处理

```yaml
测试ID: TC-ERROR-001
测试名称: 网络异常处理测试
优先级: P1
测试类型: 异常测试

测试步骤:
  步骤1:
    操作: 模拟网络超时
    - 使用代理工具延迟响应
    - 延迟时间: 10秒
    API调用:
      method: POST
      url: /api/execution/execute/{skillId}
    预期结果:
      - 前端显示超时提示
      - 后端记录超时日志
      - 执行状态标记为TIMEOUT
      
  步骤2:
    操作: 模拟网络断开
    - 请求发送后断开网络
    API调用:
      method: POST
      url: /api/skills
    预期结果:
      - 前端显示网络错误
      - 提供重试机制
```

### TC-ERROR-002: 服务不可用处理

```yaml
测试ID: TC-ERROR-002
测试名称: 服务不可用处理测试
优先级: P1
测试类型: 异常测试

测试步骤:
  步骤1:
    操作: 模拟数据库不可用
    - 停止数据库服务
    API调用:
      method: GET
      url: /api/skills
    预期结果:
      - HTTP状态码: 503
      - 友好的错误提示
      - 不暴露内部错误详情
      
  步骤2:
    操作: 恢复数据库服务
    预期结果:
      - 服务自动恢复
      - 请求正常处理
```

---

## 八、测试执行计划

### 8.1 测试环境

```yaml
环境配置:
  服务器:
    - CPU: 4核
    - 内存: 8GB
    - 磁盘: 100GB
  数据库:
    - MySQL 8.0
    - 连接池: HikariCP
  缓存:
    - Redis 6.0
  网络:
    - 带宽: 100Mbps
    - 延迟: < 10ms
```

### 8.2 测试数据准备

```sql
-- 初始化测试数据
INSERT INTO skills (id, name, description, category, status, created_at) VALUES
('skill-001', '文本转大写', '将文本转换为大写', 'text', 'ACTIVE', NOW()),
('skill-002', '文本转小写', '将文本转换为小写', 'text', 'ACTIVE', NOW()),
('skill-003', '计算长度', '计算文本长度', 'text', 'PENDING', NOW());

INSERT INTO groups (id, name, description, member_count, created_at) VALUES
('group-001', '测试群组1', '描述1', 5, NOW()),
('group-002', '测试群组2', '描述2', 3, NOW());
```

### 8.3 测试执行顺序

```
第一阶段: 基础功能测试 (1-2天)
  - TC-SKILL-001 到 TC-SKILL-003
  - TC-GROUP-001
  
第二阶段: 存储与系统管理测试 (1天)
  - TC-STORAGE-001
  - TC-SYSTEM-001
  
第三阶段: 安全测试 (1天)
  - TC-SECURITY-001
  - TC-SECURITY-002
  
第四阶段: 性能与并发测试 (2天)
  - TC-CONCURRENT-001
  
第五阶段: 异常场景测试 (1天)
  - TC-ERROR-001
  - TC-ERROR-002
```

### 8.4 测试通过标准

```yaml
功能测试:
  - 所有P0用例100%通过
  - 所有P1用例90%以上通过
  - 无阻塞性缺陷

性能测试:
  - 平均响应时间 < 3秒
  - 95%响应时间 < 5秒
  - 并发成功率 > 99%
  - 系统资源使用率 < 80%

安全测试:
  - 无高危安全漏洞
  - SQL注入防护100%有效
  - 越权访问防护100%有效
```

---

## 九、自动化测试脚本示例

### 9.1 JMeter测试计划

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testname="SkillCenter API Test">
      <elementProp name="TestPlan.user_defined_variables">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.value">localhost</stringProp>
          </elementProp>
          <elementProp name="PORT" elementType="Argument">
            <stringProp name="Argument.value">8081</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testname="并发用户">
        <intProp name="ThreadGroup.num_threads">100</intProp>
        <intProp name="ThreadGroup.ramp_time">10</intProp>
        <longProp name="ThreadGroup.duration">60</longProp>
        <elementProp name="ThreadGroup.arguments">
          <collectionProp name="Arguments.arguments"/>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testname="执行技能">
          <elementProp name="HTTPsampler.Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{"input":"test"}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port">${PORT}</stringProp>
          <stringProp name="HTTPSampler.path">/skillcenter/api/execution/execute/skill-001</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
        </HTTPSamplerProxy>
        <hashTree>
          <HeaderManager guiclass="HeaderPanel" testname="HTTP Header">
            <collectionProp name="HeaderManager.headers">
              <elementProp name="" elementType="Header">
                <stringProp name="Header.name">Content-Type</stringProp>
                <stringProp name="Header.value">application/json</stringProp>
              </elementProp>
            </collectionProp>
          </HeaderManager>
        </hashTree>
      </hashTree>
    </hashTree>
  </jmeterTestPlan>
</jmeterTestPlan>
```

### 9.2 Postman测试集合

```json
{
  "info": {
    "name": "SkillCenter API Tests",
    "description": "SkillCenter后端API测试集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/"
  },
  "item": [
    {
      "name": "技能管理",
      "item": [
        {
          "name": "发布技能",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/skills",
              "host": ["{{baseUrl}}"],
              "path": ["api", "skills"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"测试技能\",\n  \"description\": \"这是一个测试技能\",\n  \"category\": \"text\",\n  \"code\": \"console.log('hello')\"\n}"
            }
          },
          "response": [],
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('状态码为200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "",
                  "pm.test('响应包含成功标志', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.success).to.eql(true);",
                  "});",
                  "",
                  "pm.test('响应包含技能ID', function () {",
                  "    var jsonData = pm.response.json();",
                  "    pm.expect(jsonData.data).to.have.property('id');",
                  "});"
                ]
              }
            }
          ]
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081/skillcenter"
    }
  ]
}
```

---

## 十、测试报告模板

```markdown
# SkillCenter测试报告

## 测试概览
- 测试日期: 2026-02-07
- 测试版本: v0.6.6
- 测试人员: [测试人员姓名]
- 测试环境: [环境信息]

## 测试统计
| 测试类型 | 用例总数 | 通过 | 失败 | 跳过 | 通过率 |
|---------|---------|------|------|------|--------|
| 功能测试 | 50 | 48 | 2 | 0 | 96% |
| 性能测试 | 10 | 10 | 0 | 0 | 100% |
| 安全测试 | 15 | 15 | 0 | 0 | 100% |
| 异常测试 | 10 | 9 | 1 | 0 | 90% |
| **总计** | **85** | **82** | **3** | **0** | **96.5%** |

## 缺陷列表
| 缺陷ID | 严重程度 | 描述 | 状态 |
|--------|---------|------|------|
| BUG-001 | 高 | 并发执行时偶现死锁 | 已修复 |
| BUG-002 | 中 | 大数据量查询响应慢 | 待优化 |
| BUG-003 | 低 | 页面样式在IE浏览器下错乱 | 已修复 |

## 测试结论
[测试结论和建议]

## 附录
- 详细测试日志
- 性能测试报告
- 安全扫描报告
```
