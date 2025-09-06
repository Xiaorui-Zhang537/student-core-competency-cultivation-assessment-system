## 帮助入口

- 学生端与教师端：在右上角头像菜单新增“帮助”，指向 `/help`（无需登录可访问）。
- 登录页：在登录表单下新增“帮助中心”按钮，点击进入 `/help`。

实现要点：
- 路由新增：`/help` 映射到 `features/shared/views/HelpView.vue`。
- i18n：使用 `layout.common.help`，已在中英文 `layout.json` 中补充。

