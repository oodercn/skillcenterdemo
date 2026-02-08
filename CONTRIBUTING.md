# 贡献指南

感谢您对 Agent SkillCenter 项目的关注！我们欢迎各种形式的贡献，包括但不限于：

- 提交 Bug 报告
- 提交功能建议
- 改进文档
- 提交代码修复
- 提交新功能

## 如何贡献

### 提交 Issue

如果您发现了 Bug 或有功能建议，请通过以下步骤提交 Issue：

1. 检查是否已有相似的 Issue 存在
2. 使用清晰的标题描述问题
3. 详细描述问题或建议：
   - 对于 Bug：描述复现步骤、期望行为和实际行为
   - 对于功能建议：描述功能用途和预期行为

### 提交代码

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建一个 Pull Request

### 代码规范

- 所有代码必须兼容 Java 8
- 遵循 Java 代码规范
- 添加必要的注释
- 编写单元测试

### 提交信息规范

提交信息应该清晰描述修改内容：

```
类型: 简短描述

详细描述（可选）
```

类型包括：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

## 开发环境

### 环境要求

- Java 8+
- Maven 3.6+

### 构建项目

```bash
mvn clean package
```

### 运行测试

```bash
mvn test
```

## 许可证

通过提交代码，您同意您的贡献将在 MIT 许可证下发布。
