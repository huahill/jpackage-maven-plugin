# JPackage Maven Plugin

中文 | [English](README.md)

---

这是一个用于打包桌面 Java 应用的 Maven 插件。它封装 JDK 自带的
`jlink` 和 `jpackage` 工具，并提供可选的 Project Leyden AOT 缓存训练流程。

## 坐标

Maven Central：
https://central.sonatype.com/artifact/io.github.huahill/jpackage-maven-plugin

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
</plugin>
```

当前插件构建基线是 Java 25 和 Maven 4。

## Java 兼容性

这里需要区分两个 Java 版本：

- 用来构建和运行该 Maven 插件的 Java 版本。
- `javaHome` 选择的 JDK 版本，插件会调用这个 JDK 下的 `jlink` 和 `jpackage` 工具。

| 范围 | 要求 | 说明 |
|---|---|---|
| 插件构建/运行 | Java 25、Maven 4 | 插件当前使用 `maven.compiler.release=25` 编译。 |
| `jlink` goal | 带有 `jlink` 的 JDK | 具体支持哪些选项取决于所选 JDK。当前结构化选项按 JDK 25 对齐。 |
| `jpackage` goal | 带有 `jpackage` 的 JDK | `jpackage` 不是 JDK 8 时代的工具，请使用现代 JDK。当前结构化选项按 JDK 25 对齐。 |
| `leyden` goal | JDK 25 或更新版本 | 该 goal 使用 `-XX:AOTCacheOutput`，也就是 JDK 25+ 的 Leyden AOT 缓存流程。若 `javaHome` 低于 JDK 25，会提前失败。 |

为了得到可预测的结果，建议 Maven 和 `javaHome` 使用同一个现代 JDK。如果给
`jlink` 或 `jpackage` 选择较老的 JDK，该 JDK 不支持的选项会在底层 JDK 工具中失败。

## Goals

| Goal | 用途 |
|---|---|
| `jlink` | 直接调用 JDK `jlink` 工具。 |
| `jpackage` | 直接调用 JDK `jpackage` 工具。 |
| `leyden` | 暂存项目产物，构建 `jlink` 运行时，创建 `jpackage` app image，训练 Leyden AOT 缓存，重写 launcher 配置，并创建最终安装包。 |

## 快速开始

直接从 Maven Central 使用已发布插件，不需要配置额外 Maven 仓库：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.huahill</groupId>
            <artifactId>jpackage-maven-plugin</artifactId>
            <version>0.1.0</version>
        </plugin>
    </plugins>
</build>
```

完整配置可以参考本仓库中的示例项目。如果是在开发插件本身，需要先把当前源码安装到
本地 Maven 仓库，再运行示例：

```bash
sdk env
./mvnw install
./mvnw -f samples/classpath-swing/pom.xml package
```

示例项目会把打包产物写入自己的 `target/` 目录。

## 支持的应用形态

| 形态 | 状态 |
|---|---|
| 使用 `mainJar` 和 `mainClass` 的 classpath 桌面应用 | 已实现，并在命令生成层面有单元测试。 |
| 使用 `module` 的 JPMS 桌面应用 | 已实现，并通过 `samples/jpms-swing` 的 macOS app image 验证。 |
| Swing/AWT | 由 `samples/classpath-swing` 和 `samples/jpms-swing` 覆盖；macOS app image 已验证。 |
| JavaFX | 由 `samples/classpath-javafx` 覆盖；macOS app image 已验证。 |
| 多 launcher | 通过 `addLaunchers` properties 文件支持。 |
| Linux/Windows 安装包 | 命令生成和路径推导已有单元测试；真实主机上的 `.deb`、`.rpm`、`.msi`、`.exe` 仍待验证。 |

## Classpath

### Swing

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>package-app</id>
            <phase>package</phase>
            <goals>
                <goal>jpackage</goal>
            </goals>
            <configuration>
                <name>Swing Sample</name>
                <packageType>app-image</packageType>
                <destination>${project.build.directory}/jpackage</destination>
                <input>${project.build.directory}/jpackage-input</input>
                <mainJar>${project.build.finalName}.jar</mainJar>
                <mainClass>com.acme.sample.SwingSampleApp</mainClass>
                <modules>
                    <module>java.desktop</module>
                </modules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

消费方项目需要确保主 jar 和运行时依赖已经放到 `input` 目录中。完整示例见
`samples/classpath-swing/pom.xml`，该示例会把 jar 直接写入
`target/jpackage-input`。

### JavaFX

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>package-app</id>
            <phase>package</phase>
            <goals>
                <goal>jpackage</goal>
            </goals>
            <configuration>
                <name>JavaFX Sample</name>
                <packageType>app-image</packageType>
                <destination>${project.build.directory}/jpackage</destination>
                <input>${project.build.directory}/jpackage-input</input>
                <mainJar>${project.build.finalName}.jar</mainJar>
                <mainClass>com.acme.sample.JavaFxSampleApp</mainClass>
                <modulePath>
                    <path>${project.build.directory}/jpackage-input</path>
                </modulePath>
                <modules>
                    <module>javafx.controls</module>
                </modules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

对于 classpath JavaFX 应用，需要把 JavaFX 运行时依赖复制到 `input` 目录，
并把该目录加入 `modulePath`，这样 `jpackage` 才能把 JavaFX 模块链接进运行时镜像。

## JPMS Module

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>package-app</id>
            <phase>package</phase>
            <goals>
                <goal>jpackage</goal>
            </goals>
            <configuration>
                <name>JPMS Swing Sample</name>
                <packageType>app-image</packageType>
                <destination>${project.build.directory}/jpackage</destination>
                <modulePath>
                    <path>${project.build.directory}/jpackage-input</path>
                </modulePath>
                <module>com.acme.modulesample/com.acme.modulesample.ModuleSwingSampleApp</module>
            </configuration>
        </execution>
    </executions>
</plugin>
```

对于 JPMS 应用，需要把模块化应用 jar 和模块化依赖放到 `modulePath` 上，
并使用 `module`，而不是 `mainJar`/`mainClass`。

## Leyden

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>leyden-package</id>
            <phase>package</phase>
            <goals>
                <goal>leyden</goal>
            </goals>
            <configuration>
                <name>Swing Sample</name>
                <mainJar>${project.build.finalName}.jar</mainJar>
                <mainClass>com.acme.sample.SwingSampleApp</mainClass>
                <runtimeModules>
                    <runtimeModule>java.desktop</runtimeModule>
                    <runtimeModule>jdk.jfr</runtimeModule>
                </runtimeModules>
                <trainingProperty>sample.leyden.training</trainingProperty>
            </configuration>
        </execution>
    </executions>
</plugin>
```

应用必须在 `-D<trainingProperty>=true` 存在时自行退出。插件会在 Maven 构建期间
直接启动生成出来的 app image，并等待该进程结束。

JPMS Leyden 启动通过 `module` 配置：

```xml
<configuration>
    <name>JPMS Swing Sample</name>
    <module>com.acme.modulesample/com.acme.modulesample.ModuleSwingSampleApp</module>
    <runtimeModules>
        <runtimeModule>com.acme.modulesample</runtimeModule>
        <runtimeModule>java.desktop</runtimeModule>
    </runtimeModules>
</configuration>
```

## `jlink` 参数

| 参数 | 工具选项 | 说明 |
|---|---|---|
| `modulePath` | `--module-path` | 该 goal 必填。 |
| `modules` | `--add-modules` | 该 goal 必填。 |
| `output` | `--output` | 该 goal 必填。 |
| `noHeaderFiles` | `--no-header-files` | 布尔值。 |
| `noManPages` | `--no-man-pages` | 布尔值。 |
| `stripDebug` | `--strip-debug` | 布尔值。 |
| `compress` | `--compress` | 字符串值，例如 `zip-6`。 |
| `bindServices` | `--bind-services` | 布尔值。 |
| `extraOptions` | 透传 | 追加到已建模选项之后。 |
| `javaHome` | 工具位置 | 默认 `${java.home}`。 |

## `jpackage` 参数

通用选项：

| 参数 | 工具选项 |
|---|---|
| `optionFiles` | `@file` |
| `name` | `--name` |
| `packageType` | `--type` |
| `packageVersion` | `--app-version` |
| `copyright` | `--copyright` |
| `description` | `--description` |
| `destination` | `--dest` |
| `help` | `--help` |
| `icon` | `--icon` |
| `temp` | `--temp` |
| `toolVersion` | `--version` |
| `vendor` | `--vendor` |
| `verbose` | `--verbose` |

运行时镜像选项：

| 参数 | 工具选项 |
|---|---|
| `modules` | `--add-modules` |
| `jlinkOptions` | `--jlink-options` |
| `modulePath` | `--module-path` |
| `runtimeImage` | `--runtime-image` |

应用镜像选项：

| 参数 | 工具选项 |
|---|---|
| `appContent` | `--app-content` |
| `input` | `--input` |

Launcher 选项：

| 参数 | 工具选项 |
|---|---|
| `addLaunchers` | `--add-launcher` |
| `arguments` | `--arguments` |
| `javaOptions` | `--java-options` |
| `mainClass` | `--main-class` |
| `mainJar` | `--main-jar` |
| `module` | `--module` |

安装包选项：

| 参数 | 工具选项 |
|---|---|
| `aboutUrl` | `--about-url` |
| `appImage` | `--app-image` |
| `fileAssociations` | `--file-associations` |
| `installDir` | `--install-dir` |
| `launcherAsService` | `--launcher-as-service` |
| `licenseFile` | `--license-file` |
| `resourceDir` | `--resource-dir` |

macOS 选项：

| 参数 | 工具选项 |
|---|---|
| `macAppCategory` | `--mac-app-category` |
| `macAppImageSignIdentity` | `--mac-app-image-sign-identity` |
| `macAppStore` | `--mac-app-store` |
| `macDmgContent` | `--mac-dmg-content` |
| `macEntitlements` | `--mac-entitlements` |
| `macInstallerSignIdentity` | `--mac-installer-sign-identity` |
| `macPackageIdentifier` | `--mac-package-identifier` |
| `macPackageName` | `--mac-package-name` |
| `macPackageSigningPrefix` | `--mac-package-signing-prefix` |
| `macSign` | `--mac-sign` |
| `macSigningKeyUserName` | `--mac-signing-key-user-name` |
| `macSigningKeychain` | `--mac-signing-keychain` |

Linux 选项：

| 参数 | 工具选项 |
|---|---|
| `linuxAppCategory` | `--linux-app-category` |
| `linuxAppRelease` | `--linux-app-release` |
| `linuxDebMaintainer` | `--linux-deb-maintainer` |
| `linuxMenuGroup` | `--linux-menu-group` |
| `linuxPackageDeps` | `--linux-package-deps` |
| `linuxPackageName` | `--linux-package-name` |
| `linuxRpmLicenseType` | `--linux-rpm-license-type` |
| `linuxShortcut` | `--linux-shortcut` |

Windows 选项：

| 参数 | 工具选项 |
|---|---|
| `winConsole` | `--win-console` |
| `winDirChooser` | `--win-dir-chooser` |
| `winHelpUrl` | `--win-help-url` |
| `winMenu` | `--win-menu` |
| `winMenuGroup` | `--win-menu-group` |
| `winPerUserInstall` | `--win-per-user-install` |
| `winShortcut` | `--win-shortcut` |
| `winShortcutPrompt` | `--win-shortcut-prompt` |
| `winUpdateUrl` | `--win-update-url` |
| `winUpgradeUuid` | `--win-upgrade-uuid` |

未来 JDK 新增但尚未结构化建模的选项，可以先通过 `extraOptions` 追加。

## `leyden` 参数

`leyden` 接受与 `jpackage` 相同的应用、launcher、安装包和平台选项，并额外支持：

| 参数 | 默认值 | 说明 |
|---|---|---|
| `trainingProperty` | `leyden.training` | 用于让应用在训练后退出的系统属性。 |
| `aotCacheName` | `startup.aot` | app image 内部的 AOT 缓存文件名。 |
| `inputDirectory` | `${project.build.directory}/jpackage-input` | 暂存项目产物和运行时依赖。 |
| `runtimeDirectory` | `${project.build.directory}/jlink-runtime` | 生成的运行时镜像。 |
| `appImageDirectory` | `${project.build.directory}/jpackage-app-image` | 训练用 app image 输出目录。 |
| `packageDirectory` | `${project.build.directory}/jpackage` | 最终安装包输出目录。 |
| `runtimeModules` | 无 | 必填。传给内部 `jlink --add-modules`。 |
| `modulePath` | 空 | 提供给内部 `jlink` 的额外模块或 JMOD 位置。 |
| `includeInputDirectoryOnModulePath` | `true` | 把暂存 jar 加入内部 `jlink` 的模块路径。 |
| `noHeaderFiles` | `true` | 内部 `jlink` 选项。 |
| `noManPages` | `true` | 内部 `jlink` 选项。 |
| `stripDebug` | `true` | 内部 `jlink` 选项。 |
| `compress` | 无 | 内部 `jlink --compress` 值。 |
| `bindServices` | `false` | 内部 `jlink --bind-services`。 |
| `jlinkOptions` | 空 | 额外内部 `jlink` 选项。 |

`leyden` goal 会在打包前检查所选 `javaHome`，要求 JDK 25 或更新版本。

## 当前限制

- 插件目前处于 alpha 阶段。
- 当前源码树包含命令生成、descriptor 生成、路径推导、暂存和 launcher 配置重写的单元测试。
- 示例项目已经在 macOS 上手工验证过 `app-image` 输出，但还没有接入根项目的
  `verify` 生命周期。
- Linux 和 Windows 安装包创建尚未在真实 Linux/Windows 主机上验证。
- JPMS module launch 已支持，并已验证直接 `jpackage` app-image 打包；JPMS Leyden
  打包仍需要专门的运行时验证。
- GraalVM native-image 打包不在该插件范围内。
- Apple notarization 不在范围内。Notarization 是 `jpackage` 之后的发布流程步骤，
  不是 JDK `jpackage` 选项。插件支持 `jpackage` 暴露的 JDK macOS 签名选项。

## 开发

```bash
sdk env
./mvnw verify
```

当前构建验证的是 Maven 插件本身。示例项目在加入集成测试前仍需要手工运行。

## 发布流程

发布流程见 `docs/releasing.md`。

匹配 `v*` 的 tag 会触发 GitHub Actions 发布到 Maven Central。workflow 会校验
tag 与 `project.version` 一致，拒绝发布 `-SNAPSHOT` 版本，使用 GPG 签名构件，
并通过 Sonatype Central Publisher Portal 发布。发布成功后，workflow 会把 `main`
自动提升到下一个 patch `-SNAPSHOT` 版本。

## 许可证

本项目使用 Universal Permissive License, Version 1.0。
