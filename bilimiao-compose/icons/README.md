### svg图标文件夹

图标 SVG 源文件位于 `./BilimiaoIcons` 文件夹。

## 生成 Kotlin 代码

使用 [svg-to-compose](https://github.com/DevSrSouza/svg-to-compose) 通过 Gradle 任务自动将 SVG 转换为 Compose ImageVector Kotlin 代码：

```bash
./gradlew :bilimiao-compose:generateBilimiaoIcons
```

生成的代码输出到 `bilimiao-compose/build/generated/bilimiaoIcons/src/commonMain/kotlin`，并自动注册到 `commonMain` 源集中，Kotlin 编译时会自动运行此任务。

## 添加/修改图标

1. 将 `.svg` 文件放入 `./BilimiaoIcons/Common/`（或新建子文件夹作为图标分组）
2. 运行 `./gradlew :bilimiao-compose:generateBilimiaoIcons` 重新生成
3. 代码中使用：`BilimiaoIcons.Common.图标名`
