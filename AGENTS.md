# AGENTS.md

## Cursor Cloud specific instructions

### Architecture

This is a monorepo with three main parts:

- **Backend** (`backend/`): Java 17 + Spring Boot 3.5.4 + MyBatis + MySQL — runs on port 8080 with context path `/api`
- **Frontend** (`frontend/`): Vue 3 + TypeScript + Vite 5 + Tailwind/DaisyUI — runs on port 5173
- **Docs** (`docs/`): VitePress documentation site — runs on port 4174 (optional)

See `README.md` for full details on scripts, config, and project structure.

### Starting services

1. **MySQL**: Must be running on port 3306 with database `student_assessment_system` and schema imported from `backend/src/main/resources/schema.sql`. Root user with empty password works for local dev.
2. **Backend**: `cd backend && JWT_SECRET="dev-jwt-secret-key-for-development-environment-should-be-secure" JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64 mvn spring-boot:run -DskipTests`
3. **Frontend**: `cd frontend && npm run dev:solo` (use `dev:solo` to skip the docs site; `npm run dev` starts both frontend + docs)

### Non-obvious gotchas

- **JWT_SECRET must be set**: The default profile leaves `jwt.secret` empty, which causes a 400 error on login. Either set `JWT_SECRET` env var (must be >= 256 bits / 32+ chars) or use `--spring.profiles.active=dev` (but dev profile also overrides DB password to `Ab*83327000`).
- **Email verification required**: Newly registered users must have `email_verified = 1` in the `users` table before they can log in. In dev, manually update: `mysql -u root student_assessment_system -e "UPDATE users SET email_verified = 1 WHERE username = 'xxx';"`
- **MySQL startup in container**: MySQL may need manual startup: `sudo mysqld --user=mysql --datadir=/var/lib/mysql --socket=/var/run/mysqld/mysqld.sock --port=3306 &`
- **Java version**: Must use Java 17 (not 21). Switch with: `sudo update-java-alternatives -s java-1.17.0-openjdk-amd64`
- **Redis is not required**: Redis autoconfiguration is explicitly excluded in `application.yml`.
- **No automated tests**: The backend has test dependencies and plugins removed from `pom.xml`. There are no unit/integration test files.
- **Frontend lint**: ESLint reports pre-existing errors (mostly `no-empty` block statements). This is expected and not a blocker.

### Common commands

| Task | Command |
|------|---------|
| Frontend lint | `cd frontend && npx eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --ignore-path .gitignore` |
| Frontend type-check | `cd frontend && npx vue-tsc --noEmit` |
| Frontend build | `cd frontend && npm run build` |
| Backend build | `cd backend && JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64 mvn package -DskipTests` |
| Backend run | See "Starting services" above |

### Test accounts

After importing `data.sql`, seeded users exist. You can also register via API:
```
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@12345","confirmPassword":"Test@12345","email":"test@example.com","role":"student"}'
```
Then verify email in DB before login.
