---
title: 生产部署指南
description: 前后端分离部署（Nginx 反代 /api 到后端，支持 Baota 或 systemd）
outline: [2, 3]
---

# 生产部署指南

本文档提供两种常见部署方式，二选一即可：

- 方式 A：宝塔面板（更“运维 UI 化”）
- 方式 B：Linux + systemd（更标准化、可控）

共同点：

- 后端为 Spring Boot，`context-path=/api`（即后端根路径为 `http://<host>:8080/api`）。
- 前端为静态站点（Vite build），生产默认 API baseURL 为 `/api`（同源）。
- 本项目已排除 Redis 自动装配（无 Redis 依赖），缓存为本地内存（`spring.cache.type=simple`）。

:::danger 安全提示
不要把真实的 `DB_PASSWORD`、`MAIL_PASSWORD`、`JWT_SECRET` 写进文档或提交到 Git。下面所有示例均用占位符表示。
:::

## 1. 端口与域名规划

- 后端：`127.0.0.1:8080`（建议仅内网监听或用防火墙阻断公网访问）
- 前端：Nginx 提供 `80/443`
- 建议：
  - `stucoreai.space` 提供主站
  - `docs.stucoreai.space`（可选）提供文档站

## 2. 后端部署（通用）

### 2.1 打包

```bash
cd backend
mvn -DskipTests clean package
```

产物形如：`backend/target/student-assessment-system-<version>.jar`

### 2.2 生产配置与环境变量（建议）

生产 Profile：`prod`（对应 `backend/src/main/resources/application-prod.yml`）。

最小建议环境变量：

- `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- `JWT_SECRET`
- `APP_BASE_URL`（用于邮件链接跳转域名等场景）
- `UPLOAD_DIR`（上传目录，默认 `/app/uploads`）
- AI（可选）：`AI_DEFAULT_PROVIDER`、`GOOGLE_API_KEY`/`GLM_API_KEY` 等

:::tip Swagger
生产配置默认关闭 Swagger（`application-prod.yml` 中 `springdoc.*.enabled=false`）。
:::

## 3. 前端部署（通用）

### 3.1 构建

```bash
cd frontend
npm ci
npm run build
```

构建产物为：`frontend/dist/`

### 3.2 配置要点

- 生产环境建议保持 `VITE_API_BASE=/api`（同源）
- 文档站跳转可设置 `VITE_DOCS_URL=https://docs.example.com`（见前端 `/docs` 路由）

## 4. 方式 A：宝塔面板（Nginx 反代）

### 4.1 站点根目录

将 `frontend/dist/` 部署到宝塔站点根目录（例如 `/www/wwwroot/stucoreai.space`）。

### 4.2 Nginx 反代（/api -> 127.0.0.1:8080）

```nginx
location /api/ {
  proxy_pass http://127.0.0.1:8080;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
  try_files $uri $uri/ /index.html;
}
```

### 4.3 文档站部署（可选）

- 推荐独立子域 `docs.stucoreai.space`，部署 `docs` 的静态产物即可（不需要反代 `/api`）。
- 若必须使用子路径 `/docs`：需要将 docs 静态产物放在站点目录的 `docs/` 下，并增加对应 `location` 与路由回退。

## 5. 方式 B：Linux + systemd（Ubuntu 示例）

### 5.1 安装依赖

```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless nginx
```

### 5.2 准备目录与用户

```bash
sudo adduser --system --group app
sudo mkdir -p /app/uploads /app/logs
sudo mkdir -p /var/www/stucoreai
sudo chown -R app:app /app /var/www/stucoreai
```

### 5.3 systemd 启动后端

1. 上传 jar 为 `/app/app.jar`
2. 写环境变量文件（示例）：`/etc/default/student-assessment`

```bash
SERVER_PORT=8080
APP_BASE_URL=https://stucoreai.space

DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=student_assessment_system
DB_USERNAME=student_assessment_system
DB_PASSWORD=__REPLACE_ME__

JWT_SECRET=__REPLACE_ME__
UPLOAD_DIR=/app/uploads
MAX_FILE_SIZE=50MB
```

3. 创建 systemd unit：`/etc/systemd/system/student-assessment.service`

```ini
[Unit]
Description=Student Assessment Backend
After=network.target

[Service]
User=app
Group=app
EnvironmentFile=/etc/default/student-assessment
WorkingDirectory=/app
ExecStart=/usr/bin/java -jar /app/app.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

4. 启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now student-assessment
sudo systemctl status student-assessment -n 100 --no-pager
```

### 5.4 Nginx 部署前端

将 `frontend/dist/` 上传到 `/var/www/stucoreai/`，并配置站点：

```nginx
server {
  listen 80;
  server_name stucoreai.space www.stucoreai.space;

  root /var/www/stucoreai;
  index index.html;

  client_max_body_size 50m;

  location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

## 6. 验证（Verify）

- 前端首页可打开（建议 HTTPS）
- 健康检查（如开启）：`curl -i http://127.0.0.1:8080/api/actuator/health`
- Nginx 反代后：`curl -i https://<domain>/api/actuator/health`
- 登录/注册/邮件验证链接可用
- 文件上传与下载正常（注意 Nginx `client_max_body_size` 与后端 `MAX_FILE_SIZE`）
