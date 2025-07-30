# 学生非核心能力发展评估系统 - 环境变量配置

## 🔧 必需的环境变量

在运行应用程序之前，请设置以下环境变量：

### 数据库配置
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=student_assessment_system
export DB_USERNAME=root
export DB_PASSWORD=your_secure_database_password
```

### Redis配置
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_DATABASE=0
export REDIS_TIMEOUT=3000ms
export REDIS_PASSWORD=your_redis_password_if_any
```

### JWT安全配置
```bash
# 🔐 重要：JWT密钥必须至少32字符的强随机字符串
export JWT_SECRET=your_256_bit_secret_key_here_32_chars_minimum
export JWT_EXPIRATION=86400000       # 24小时
export JWT_REFRESH_EXPIRATION=604800000  # 7天
```

### 应用配置
```bash
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=8080
export UPLOAD_DIR=./uploads
export MAX_FILE_SIZE=50MB
export LOG_LEVEL=INFO
```

## 🔑 JWT密钥生成方法

### 方法1: 使用OpenSSL
```bash
openssl rand -base64 32
```

### 方法2: 使用Python
```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### 方法3: 使用Node.js
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

## 🚀 快速启动

1. 复制上述环境变量到你的 `.bashrc`, `.zshrc` 或 `.env` 文件
2. 生成强JWT密钥替换 `your_256_bit_secret_key_here_32_chars_minimum`
3. 设置正确的数据库密码
4. 重启终端或运行 `source ~/.bashrc`
5. 启动应用程序

## ⚠️ 安全注意事项

- **永远不要** 将真实的密钥提交到版本控制系统
- JWT密钥必须在生产环境中使用强随机生成的值
- 定期轮换密钥以提高安全性
- 使用不同的密钥用于不同的环境（开发/测试/生产）
