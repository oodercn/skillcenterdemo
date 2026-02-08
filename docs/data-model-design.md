# SkillCenter 数据模型完善设计文档

## 一、个人中心模块数据模型

### 1.1 个人仪表盘统计 (PersonalDashboardStats)

```java
public class PersonalDashboardStats {
    // 技能统计
    private int totalSkills;                    // 总技能数
    private int publishedSkills;                // 已发布技能数
    private int pendingSkills;                  // 待审核技能数
    private int draftSkills;                    // 草稿技能数
    
    // 执行统计
    private int executionsToday;                // 今日执行次数
    private int executionsThisWeek;             // 本周执行次数
    private int executionsThisMonth;            // 本月执行次数
    private int totalExecutions;                // 总执行次数
    private double successRate;                 // 成功率
    private long averageExecutionTime;          // 平均执行时间(ms)
    
    // 分享统计
    private int sharedSkills;                   // 已分享技能数
    private int receivedSkills;                 // 收到技能数
    private int shareViews;                     // 分享被查看次数
    private int shareDownloads;                 // 分享被下载次数
    
    // 群组统计
    private int groupCount;                     // 加入的群组数
    private int ownedGroups;                    // 拥有的群组数
    private int groupSkills;                    // 群组技能总数
    
    // 时间戳
    private LocalDateTime lastUpdated;          // 最后更新时间
}
```

### 1.2 执行历史记录 (ExecutionHistory)

```java
@Entity
@Table(name = "execution_history")
public class ExecutionHistory {
    @Id
    private String executionId;                 // 执行ID (UUID)
    
    private String userId;                      // 用户ID
    private String skillId;                     // 技能ID
    private String skillName;                   // 技能名称
    private String skillVersion;                // 技能版本
    
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;             // 执行状态
    
    private LocalDateTime startTime;            // 开始时间
    private LocalDateTime endTime;              // 结束时间
    private Long duration;                      // 执行时长(ms)
    
    @Column(columnDefinition = "TEXT")
    private String input;                       // 输入参数(JSON)
    
    @Column(columnDefinition = "TEXT")
    private String output;                      // 输出结果(JSON)
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;                // 错误信息
    
    private String executionType;               // 执行类型: SYNC/ASYNC
    private String triggerSource;               // 触发来源: WEB/API/SCHEDULED
    private String clientIp;                    // 客户端IP
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

public enum ExecutionStatus {
    PENDING,        // 等待中
    RUNNING,        // 运行中
    SUCCESS,        // 成功
    FAILED,         // 失败
    TIMEOUT,        // 超时
    CANCELLED       // 已取消
}
```

### 1.3 分享记录 (ShareRecord)

```java
@Entity
@Table(name = "share_records")
public class ShareRecord {
    @Id
    private String shareId;                     // 分享ID
    
    private String skillId;                     // 技能ID
    private String skillName;                   // 技能名称
    private String fromUserId;                  // 分享者ID
    private String fromUserName;                // 分享者名称
    private String toUserId;                    // 接收者ID
    private String toUserName;                  // 接收者名称
    
    @Enumerated(EnumType.STRING)
    private ShareType shareType;                // 分享类型
    
    private String shareMessage;                // 分享消息
    private LocalDateTime shareTime;            // 分享时间
    private LocalDateTime expireTime;           // 过期时间
    
    @Enumerated(EnumType.STRING)
    private ShareStatus status;                 // 分享状态
    
    private int viewCount;                      // 查看次数
    private int downloadCount;                  // 下载次数
    private LocalDateTime lastViewTime;         // 最后查看时间
    
    @CreatedDate
    private LocalDateTime createdAt;
}

public enum ShareType {
    PRIVATE,        // 私密分享
    PUBLIC,         // 公开分享
    GROUP           // 群组分享
}

public enum ShareStatus {
    ACTIVE,         // 有效
    EXPIRED,        // 已过期
    REVOKED,        // 已撤销
    ACCEPTED,       // 已接受
    REJECTED        // 已拒绝
}
```

### 1.4 个人身份信息 (PersonalIdentity)

```java
@Entity
@Table(name = "user_identity")
public class PersonalIdentity {
    @Id
    private String userId;                      // 用户ID
    
    private String nickname;                    // 昵称
    private String avatar;                      // 头像URL
    private String email;                       // 邮箱
    private String phone;                       // 电话
    
    @Enumerated(EnumType.STRING)
    private UserRole role;                      // 角色
    
    private String department;                  // 部门
    private String position;                    // 职位
    
    @Column(columnDefinition = "TEXT")
    private String bio;                         // 个人简介
    
    private String timezone;                    // 时区
    private String language;                    // 语言偏好
    
    private boolean emailNotification;          // 邮件通知开关
    private boolean smsNotification;            // 短信通知开关
    
    private LocalDateTime lastLoginTime;        // 最后登录时间
    private String lastLoginIp;                 // 最后登录IP
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

public enum UserRole {
    ADMIN,          // 管理员
    USER,           // 普通用户
    GUEST           // 访客
}
```

## 二、系统管理模块数据模型

### 2.1 系统配置 (SystemConfig)

```java
@Entity
@Table(name = "system_config")
public class SystemConfig {
    @Id
    private String configKey;                   // 配置键
    
    @Column(columnDefinition = "TEXT")
    private String configValue;                 // 配置值(JSON格式)
    
    private String description;                 // 配置描述
    private String category;                    // 配置分类
    
    private boolean editable;                   // 是否可编辑
    private boolean visible;                    // 是否可见
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private String updatedBy;                   // 更新人
}

// 配置分类示例
public class SystemConfigCategories {
    public static final String GENERAL = "general";           // 通用配置
    public static final String SECURITY = "security";         // 安全配置
    public static final String PERFORMANCE = "performance";   // 性能配置
    public static final String FEATURES = "features";         // 功能开关
    public static final String LIMITS = "limits";             // 限制配置
    public static final String NOTIFICATION = "notification"; // 通知配置
}
```

### 2.2 系统健康状态 (SystemHealth)

```java
public class SystemHealth {
    private HealthStatus status;                // 整体健康状态
    private LocalDateTime checkTime;            // 检查时间
    private long responseTime;                  // 响应时间(ms)
    
    private Map<String, HealthCheck> checks;    // 各组件检查详情
    
    public static class HealthCheck {
        private HealthStatus status;            // 组件状态
        private String component;               // 组件名称
        private String message;                 // 状态消息
        private long responseTime;              // 响应时间
        private Map<String, Object> details;    // 详细信息
    }
}

public enum HealthStatus {
    UP,             // 正常
    DOWN,           // 宕机
    DEGRADED,       // 降级
    UNKNOWN         // 未知
}
```

### 2.3 系统日志 (SystemLog)

```java
@Entity
@Table(name = "system_logs")
public class SystemLog {
    @Id
    private String logId;                       // 日志ID
    
    @Enumerated(EnumType.STRING)
    private LogLevel level;                     // 日志级别
    
    private String logger;                      // 日志记录器
    
    @Column(columnDefinition = "TEXT")
    private String message;                     // 日志消息
    
    @Column(columnDefinition = "TEXT")
    private String exception;                   // 异常堆栈
    
    private String thread;                      // 线程名
    private String className;                   // 类名
    private String methodName;                  // 方法名
    private Integer lineNumber;                 // 行号
    
    private String userId;                      // 用户ID
    private String clientIp;                    // 客户端IP
    private String requestId;                   // 请求ID
    
    @Column(columnDefinition = "TEXT")
    private String context;                     // 上下文信息(JSON)
    
    private LocalDateTime logTime;              // 日志时间
    
    @CreatedDate
    private LocalDateTime createdAt;
}

public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
}
```

### 2.4 系统操作日志 (OperationLog)

```java
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    private String operationId;                 // 操作ID
    
    @Enumerated(EnumType.STRING)
    private OperationType type;                 // 操作类型
    
    private String userId;                      // 用户ID
    private String userName;                    // 用户名
    
    private String targetType;                  // 目标类型
    private String targetId;                    // 目标ID
    private String targetName;                  // 目标名称
    
    private String description;                 // 操作描述
    
    @Enumerated(EnumType.STRING)
    private OperationStatus status;             // 操作状态
    
    @Column(columnDefinition = "TEXT")
    private String requestData;                 // 请求数据
    
    @Column(columnDefinition = "TEXT")
    private String responseData;                // 响应数据
    
    private String errorMessage;                // 错误信息
    
    private String clientIp;                    // 客户端IP
    private String userAgent;                   // 用户代理
    private String requestUri;                  // 请求URI
    private String requestMethod;               // 请求方法
    
    private Long executionTime;                 // 执行时间(ms)
    
    private LocalDateTime operationTime;        // 操作时间
    
    @CreatedDate
    private LocalDateTime createdAt;
}

public enum OperationType {
    CREATE,         // 创建
    UPDATE,         // 更新
    DELETE,         // 删除
    QUERY,          // 查询
    EXECUTE,        // 执行
    PUBLISH,        // 发布
    APPROVE,        // 审核
    REJECT,         // 拒绝
    SHARE,          // 分享
    LOGIN,          // 登录
    LOGOUT,         // 登出
    CONFIG_UPDATE,  // 配置更新
    SYSTEM_RESTART, // 系统重启
    BACKUP,         // 备份
    RESTORE         // 恢复
}

public enum OperationStatus {
    SUCCESS,        // 成功
    FAILED,         // 失败
    PENDING,        // 进行中
    CANCELLED       // 已取消
}
```

## 三、存储管理模块数据模型

### 3.1 存储配置 (StorageSettings)

```java
@Entity
@Table(name = "storage_settings")
public class StorageSettings {
    @Id
    private String id;                          // 配置ID
    
    private String storagePath;                 // 存储路径
    private String backupPath;                  // 备份路径
    
    private boolean autoBackup;                 // 自动备份开关
    private Integer backupInterval;             // 备份间隔(小时)
    private Integer maxBackupCount;             // 最大备份数量
    private Integer backupRetentionDays;        // 备份保留天数
    
    private String maxStorageSize;              // 最大存储容量
    private String maxFileSize;                 // 最大文件大小
    
    @ElementCollection
    private List<String> allowedFileTypes;      // 允许的文件类型
    
    private boolean compressionEnabled;         // 压缩开关
    private String compressionLevel;            // 压缩级别
    
    private boolean encryptionEnabled;          // 加密开关
    private String encryptionAlgorithm;         // 加密算法
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private String updatedBy;                   // 更新人
}
```

### 3.2 备份记录 (BackupRecord)

```java
@Entity
@Table(name = "backup_records")
public class BackupRecord {
    @Id
    private String backupId;                    // 备份ID
    
    private String backupName;                  // 备份名称
    private String backupPath;                  // 备份路径
    
    @Enumerated(EnumType.STRING)
    private BackupType type;                    // 备份类型
    
    @Enumerated(EnumType.STRING)
    private BackupStatus status;                // 备份状态
    
    private Long fileSize;                      // 文件大小(字节)
    private Integer fileCount;                  // 文件数量
    
    private LocalDateTime startTime;            // 开始时间
    private LocalDateTime endTime;              // 结束时间
    private Long duration;                      // 耗时(ms)
    
    private String description;                 // 备份描述
    private String createdBy;                   // 创建人
    
    private LocalDateTime expireTime;           // 过期时间
    
    @Column(columnDefinition = "TEXT")
    private String details;                     // 备份详情(JSON)
    
    @CreatedDate
    private LocalDateTime createdAt;
}

public enum BackupType {
    FULL,           // 全量备份
    INCREMENTAL,    // 增量备份
    DIFFERENTIAL    // 差异备份
}

public enum BackupStatus {
    PENDING,        // 等待中
    RUNNING,        // 进行中
    SUCCESS,        // 成功
    FAILED,         // 失败
    CANCELLED       // 已取消
}
```

### 3.3 存储统计 (StorageStats)

```java
public class StorageStats {
    // 容量统计
    private Long totalCapacity;                 // 总容量(字节)
    private Long usedCapacity;                  // 已用容量
    private Long freeCapacity;                  // 可用容量
    private Double usagePercent;                // 使用率
    
    // 文件统计
    private Integer totalFiles;                 // 总文件数
    private Integer skillFiles;                 // 技能文件数
    private Integer backupFiles;                // 备份文件数
    private Integer logFiles;                   // 日志文件数
    
    // 增长趋势
    private Long dailyGrowth;                   // 日增长量
    private Long weeklyGrowth;                  // 周增长量
    private Long monthlyGrowth;                 // 月增长量
    
    // 备份统计
    private Integer backupCount;                // 备份数量
    private Long backupSize;                    // 备份总大小
    private LocalDateTime lastBackupTime;       // 最后备份时间
    
    // 时间戳
    private LocalDateTime statsTime;            // 统计时间
}
```

## 四、数据校验规则

### 4.1 通用校验规则

```java
public class ValidationRules {
    // 字符串长度
    public static final int SKILL_NAME_MIN = 2;
    public static final int SKILL_NAME_MAX = 50;
    public static final int SKILL_DESC_MAX = 500;
    public static final int GROUP_NAME_MAX = 30;
    public static final int GROUP_DESC_MAX = 200;
    
    // 数值范围
    public static final int MAX_SKILLS_PER_USER = 100;
    public static final int MAX_EXECUTION_TIME = 30000;  // 30秒
    public static final int MAX_BACKUP_COUNT = 50;
    public static final int MAX_FILE_SIZE = 100 * 1024 * 1024;  // 100MB
    
    // 正则表达式
    public static final String SKILL_ID_PATTERN = "^[a-z0-9-]+$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
}
```

### 4.2 后端校验注解

```java
public class SkillDTO {
    @NotBlank(message = "技能名称不能为空")
    @Size(min = 2, max = 50, message = "技能名称长度必须在2-50之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_-]+$", message = "技能名称包含非法字符")
    private String name;
    
    @NotBlank(message = "技能描述不能为空")
    @Size(max = 500, message = "技能描述不能超过500字符")
    private String description;
    
    @NotBlank(message = "技能分类不能为空")
    private String category;
    
    @NotBlank(message = "技能代码不能为空")
    @Size(max = 10000, message = "技能代码不能超过10000字符")
    private String code;
}
```

### 4.3 前端校验规则

```javascript
const validationRules = {
    skill: {
        name: {
            required: true,
            minLength: 2,
            maxLength: 50,
            pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/,
            message: '技能名称2-50字符，支持中英文、数字、下划线和横线'
        },
        description: {
            required: true,
            maxLength: 500,
            message: '技能描述不能为空，最多500字符'
        },
        category: {
            required: true,
            message: '请选择技能分类'
        },
        code: {
            required: true,
            maxLength: 10000,
            message: '技能代码不能为空，最多10000字符'
        }
    },
    group: {
        name: {
            required: true,
            minLength: 2,
            maxLength: 30,
            message: '群组名称2-30字符'
        },
        description: {
            required: true,
            maxLength: 200,
            message: '群组描述最多200字符'
        }
    }
};
```
