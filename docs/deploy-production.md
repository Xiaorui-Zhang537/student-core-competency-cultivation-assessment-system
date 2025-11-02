## 生产部署指南（宝塔面板 + 前后端分离）

本文档指导将本项目在宝塔面板环境进行前后端分离部署，并完全禁用 Redis 依赖。域名以 `stucoreai.space` 为例，支持 `www` 与裸域。

### 一、端口与域名规划

- 后端服务端口：8080（Spring Boot，内部监听，不直接暴露公网）
- 前端站点端口：80/443（Nginx/网站服务器，HTTPS 强烈推荐）
- 域名：
  - 裸域：`stucoreai.space`
  - 可选 `www`：`www.stucoreai.space`
  - 推荐做法：两个域名（裸域与 www）都签发 SSL 并做 301 统一跳转到裸域，或反之。

### 二、后端（Java）部署

1. 打包
   - 服务器或本地执行：`cd backend && mvn -DskipTests clean package`
   - 产物：`target/student-assessment-system-1.0.4.jar`

2. 服务器目录建议
   - 应用目录：`/opt/student-assessment/backend`
   - 上传目录：`/opt/student-assessment/uploads`（application-prod.yml 已默认 `/app/uploads`，可用运行参数覆盖）

3. 运行环境变量（宝塔-计划任务/守护进程或 Supervisor/PM2 管理）
   - 必需：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`
   - 可选：`APP_BASE_URL`（默认已设为 `https://stucoreai.space`）
   - 禁用 Redis：已在配置中排除自动装配，无需设置任何 Redis 变量。

4. 启动命令示例
```bash
nohup java -Xms512m -Xmx1024m \
  -Duser.timezone=Asia/Shanghai \
  -Dspring.profiles.active=prod \
  -Dfile.upload-dir=/opt/student-assessment/uploads \
-jar /opt/student-assessment/backend/student-assessment-system-1.0.4.jar \
  > /opt/student-assessment/backend/app.out 2>&1 &
```

5. 健康检查
- 反代后健康检查路径：`/api/actuator/health`

### 三、前端（Vite 构建）部署

1. 构建
```bash
cd frontend
npm ci
npm run build
```
生成目录：`frontend/dist`。

2. 部署
- 将 `dist` 同步到宝塔站点根目录（如：`/www/wwwroot/stucoreai.space`）。
- 前端请求统一走相对路径 `/api`（已修改 `src/api/config.ts`），无需再配置 `VITE_API_BASE*`。

### 四、宝塔 Nginx 站点与反向代理

1. 添加站点（推荐裸域为主站）
- 绑定域名：`stucoreai.space` 与 `www.stucoreai.space`
- 站点根目录指向前端 `dist`
- 申请并开启 SSL（勾选强制 HTTPS）

2. Nginx 反代配置（/api → 127.0.0.1:8080）

在站点配置中添加（或通过“反向代理”功能设置）：
```nginx
location /api/ {
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
  proxy_pass http://127.0.0.1:8080/api/;
}

# 单页应用路由回退
location / {
  try_files $uri $uri/ /index.html;
}
```

3. 域名跳转（可选）
- 若统一到裸域：在 `www.stucoreai.space` 站点配置 301 到 `https://stucoreai.space$request_uri`。

4. 文档站点（docs）部署（两种方式二选一，推荐独立子域）

- 方式 A：同站点子路径 `/docs`
  - 在前端站点新增 `location /docs/ { try_files $uri $uri/ /docs/index.html; }`
  - 若单独构建 docs 到 `docs/dist`，将其放到站点根的 `docs/` 目录。

- 方式 B（推荐）：独立子域 `docs.stucoreai.space`
  - 新增站点绑定 `docs.stucoreai.space`，站点根指向 docs 构建产物目录
  - docs 为纯静态站点，不访问后端，不需要也不要配置 `/api` 反代

### 五、常见问题

- 邮件链接跳转地址取自 `application.base-url`，已默认 `https://stucoreai.space`，若改用 `www`，请同步设置 `APP_BASE_URL=https://www.stucoreai.space`。
- 上传目录：后端会自动创建（见 `FileStorageServiceImpl`），但请确保运行用户对该目录有写权限。
- Swagger：生产默认关闭（springdoc disabled）。

### 六、验证清单

- 前端首页可正常打开（HTTPS）。
- 访问 `/api/actuator/health` 返回 `UP`。
- 登录/注册/邮件验证链接可用。
- 文件上传与下载正常。

完成。

# 生产环境部署指南（Linux + Nginx + systemd）

> 适用域名：`www.stucoreai.space`
> 服务器公网IP：`8.141.124.239`（示例）

## 一、准备
- 操作系统：Ubuntu 20.04+/22.04+
- 需要安装：OpenJDK 17、Nginx、Certbot、Node.js（仅用于本地构建前端）

```bash
sudo apt update && sudo apt install -y openjdk-17-jre-headless nginx certbot python3-certbot-nginx
```

## 二、目录与用户
```bash
sudo adduser --system --group app
sudo mkdir -p /app/{uploads,logs}
sudo mkdir -p /var/www/stucoreai
sudo chown -R app:app /app /var/www/stucoreai
```

## 三、后端部署
1) 上传后端可执行包
- 将 `backend/target/student-assessment-system-*.jar` 上传为 `/app/app.jar`

2) 配置环境变量 `/etc/default/student-assessment`
```bash
SERVER_PORT=8080
APP_BASE_URL=https://www.stucoreai.space

DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=student_assessment_system
DB_USERNAME=student_assessment_system
DB_PASSWORD=Ab*83327000

REDIS_HOST=127.0.0.1
REDIS_PORT=6379

JWT_SECRET=请替换为足够复杂的随机字符串
UPLOAD_DIR=/app/uploads
MAX_FILE_SIZE=50MB
```

3) 创建 systemd 服务 `/etc/systemd/system/student-assessment.service`
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

4) 启动服务
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now student-assessment
sudo systemctl status student-assessment -n 100 --no-pager
```

## 四、前端部署
1) 本地构建
```bash
cd frontend
npm ci
npm run build
```
2) 上传构建产物
```bash
# 将 frontend/dist/ 上传至服务器 /var/www/stucoreai
sudo rsync -av --delete dist/ /var/www/stucoreai/
sudo chown -R app:app /var/www/stucoreai
```

## 五、Nginx 配置
创建站点文件 `/etc/nginx/sites-available/stucoreai`
```nginx
server {
  listen 80;
  server_name www.stucoreai.space stucoreai.space;
  return 301 https://www.stucoreai.space$request_uri;
}

server {
  listen 443 ssl http2;
  server_name www.stucoreai.space;

  ssl_certificate     /etc/letsencrypt/live/www.stucoreai.space/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/www.stucoreai.space/privkey.pem;

  client_max_body_size 50m;

  root /var/www/stucoreai;
  index index.html;

  location /api/ {
    proxy_pass         http://127.0.0.1:8080;
    proxy_set_header   Host              $host;
    proxy_set_header   X-Real-IP         $remote_addr;
    proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header   X-Forwarded-Proto $scheme;
  }

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```
启用并重载：
```bash
sudo ln -sf /etc/nginx/sites-available/stucoreai /etc/nginx/sites-enabled/stucoreai
sudo nginx -t && sudo systemctl reload nginx
```

## 六、HTTPS 证书申请
使用 Certbot 自动签发并配置 Nginx：
```bash
sudo certbot --nginx -d www.stucoreai.space --redirect --agree-tos -m 你的邮箱 -n
```
证书续期由 Certbot 自动 cron/systemd 定时处理，可手动测试：
```bash
sudo certbot renew --dry-run
```

## 七、验证
- 健康检查：
```bash
curl -I https://www.stucoreai.space/api/actuator/health
```
- 前端访问：打开浏览器访问 `https://www.stucoreai.space`

## 八、防火墙与安全组
- 开放 80/443（22 可选）
- 关闭 8080 公网，仅本机访问

## 九、常见问题
- 502/超时：确认 `student-assessment` 已运行、Nginx 反代地址与端口正确
- 跨域：后端已允许 `https://www.stucoreai.space` 与根域，若还需其它域请在 `SecurityConfig` 中添加
- 上传失败：同步调整 Nginx `client_max_body_size` 与后端 `MAX_FILE_SIZE`
