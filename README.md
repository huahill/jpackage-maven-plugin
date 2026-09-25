# JPackage Maven Plugin

English | [中文](README_ZH.md)

---

Maven plugin for packaging desktop Java applications with the JDK `jlink` and
`jpackage` tools, with an optional Project Leyden AOT cache training workflow.

## Coordinates

Maven Central:
https://central.sonatype.com/artifact/io.github.huahill/jpackage-maven-plugin

```xml
<plugin>
    <groupId>io.github.huahill</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>0.1.0</version>
</plugin>
```

Java 25 and Maven 4 are the current plugin build baseline.

## Java Compatibility

There are two Java versions to think about:

- The Java version used to build and run this Maven plugin.
- The JDK selected by `javaHome`, whose `jlink` and `jpackage` tools are used
  for packaging.

| Area | Requirement | Notes |
|---|---|---|
| Plugin build/runtime | Java 25, Maven 4 | The plugin is currently compiled with `maven.compiler.release=25`. |
| `jlink` goal | JDK with `jlink` | The exact supported options depend on the selected JDK. Current structured options are aligned with JDK 25. |
| `jpackage` goal | JDK with `jpackage` | `jpackage` was introduced after JDK 8; use a modern JDK. Current structured options are aligned with JDK 25. |
| `leyden` goal | JDK 25 or newer | This goal uses `-XX:AOTCacheOutput`, which is available in the JDK 25+ Leyden AOT cache workflow. The goal fails early when `javaHome` is older than JDK 25. |

For predictable results, use the same modern JDK for Maven and `javaHome`.
When using an older JDK for `jlink` or `jpackage`, any option not supported by
that JDK will fail in the underlying JDK tool.

## Goals

| Goal | Purpose |
|---|---|
| `jlink` | Invokes the JDK `jlink` tool directly. |
| `jpackage` | Invokes the JDK `jpackage` tool directly. |
| `leyden` | Stages project artifacts, builds a `jlink` runtime, creates a `jpackage` app image, trains a Leyden AOT cache, rewrites the launcher config, and creates the final installer. |

## Quick Start

Use the released plugin directly from Maven Central. No custom Maven repository
is required:

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

For a complete working example, copy one of the sample project configurations
from this repository. When developing this plugin from source, install the
current checkout locally before running the samples:

```bash
sdk env
./mvnw install
./mvnw -f samples/classpath-swing/pom.xml package
```

The sample writes packaging output under its own `target/` directory.

## Supported Application Shapes

| Shape | Status |
|---|---|
| Classpath desktop app with `mainJar` and `mainClass` | Implemented and unit-tested at command generation level. |
| JPMS desktop app with `module` | Implemented and verified with the `samples/jpms-swing` macOS app image. |
| Swing/AWT | Covered by `samples/classpath-swing` and `samples/jpms-swing`; macOS app images are verified. |
| JavaFX | Covered by `samples/classpath-javafx`; macOS app image is verified. |
| Multiple launchers | Supported through `addLaunchers` properties files. |
| Linux/Windows installers | Command generation and path layout are unit-tested. Real host `.deb`, `.rpm`, `.msi`, and `.exe` verification is still pending. |

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
                <addModules>
                    <module>java.desktop</module>
                </addModules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The consuming project must make sure the main jar and runtime dependencies are
present in `input`. See `samples/classpath-swing/pom.xml` for a complete
project that writes the jar directly into `target/jpackage-input`.

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
                <addModules>
                    <module>javafx.controls</module>
                </addModules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

For JavaFX classpath apps, copy JavaFX runtime dependencies into the `input`
directory and include that directory on `modulePath` so `jpackage` can build a
runtime image with the JavaFX modules.

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

For JPMS apps, put the modular application jar and modular dependencies on
`modulePath` and use `module` instead of `mainJar`/`mainClass`.

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

The application must exit on its own when `-D<trainingProperty>=true` is
present. The plugin starts the generated app image directly during the Maven
build and waits for that process to finish.

JPMS Leyden launch is configured with `module`:

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

## `jlink` Parameters

| Parameter | Tool option | Notes |
|---|---|---|
| `modulePath` | `--module-path` | Required by this goal. |
| `addModules` | `--add-modules` | Required by this goal. |
| `output` | `--output` | Required by this goal. |
| `noHeaderFiles` | `--no-header-files` | Boolean. |
| `noManPages` | `--no-man-pages` | Boolean. |
| `stripDebug` | `--strip-debug` | Boolean. |
| `compress` | `--compress` | String value such as `zip-6`. |
| `bindServices` | `--bind-services` | Boolean. |
| `extraOptions` | passthrough | Appended after modeled options. |
| `javaHome` | tool location | Defaults to `${java.home}`. |

## `jpackage` Parameters

General options:

| Parameter | Tool option |
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

Runtime image options:

| Parameter | Tool option |
|---|---|
| `addModules` | `--add-modules` |
| `jlinkOptions` | `--jlink-options` |
| `modulePath` | `--module-path` |
| `runtimeImage` | `--runtime-image` |

Application image options:

| Parameter | Tool option |
|---|---|
| `appContent` | `--app-content` |
| `input` | `--input` |

Launcher options:

| Parameter | Tool option |
|---|---|
| `addLaunchers` | `--add-launcher` |
| `arguments` | `--arguments` |
| `javaOptions` | `--java-options` |
| `mainClass` | `--main-class` |
| `mainJar` | `--main-jar` |
| `module` | `--module` |

Package options:

| Parameter | Tool option |
|---|---|
| `aboutUrl` | `--about-url` |
| `appImage` | `--app-image` |
| `fileAssociations` | `--file-associations` |
| `installDir` | `--install-dir` |
| `launcherAsService` | `--launcher-as-service` |
| `licenseFile` | `--license-file` |
| `resourceDir` | `--resource-dir` |

macOS options:

| Parameter | Tool option |
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

Linux options:

| Parameter | Tool option |
|---|---|
| `linuxAppCategory` | `--linux-app-category` |
| `linuxAppRelease` | `--linux-app-release` |
| `linuxDebMaintainer` | `--linux-deb-maintainer` |
| `linuxMenuGroup` | `--linux-menu-group` |
| `linuxPackageDeps` | `--linux-package-deps` |
| `linuxPackageName` | `--linux-package-name` |
| `linuxRpmLicenseType` | `--linux-rpm-license-type` |
| `linuxShortcut` | `--linux-shortcut` |

Windows options:

| Parameter | Tool option |
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

Future JDK options can be appended with `extraOptions` while the structured
configuration catches up.

## `leyden` Parameters

`leyden` accepts the same application, launcher, package, and platform options
as `jpackage`, plus:

| Parameter | Default | Notes |
|---|---|---|
| `trainingProperty` | `leyden.training` | System property used to make the app exit after training. |
| `aotCacheName` | `startup.aot` | AOT cache file name inside the app image. |
| `inputDirectory` | `${project.build.directory}/jpackage-input` | Staged project artifact and runtime dependencies. |
| `runtimeDirectory` | `${project.build.directory}/jlink-runtime` | Generated runtime image. |
| `appImageDirectory` | `${project.build.directory}/jpackage-app-image` | Training app image output. |
| `packageDirectory` | `${project.build.directory}/jpackage` | Final installer output. |
| `runtimeModules` | none | Required. Passed to internal `jlink --add-modules`. |
| `modulePath` | empty | Additional module or JMOD locations for internal `jlink`. |
| `includeInputDirectoryOnModulePath` | `true` | Adds staged jars to the internal `jlink` module path. |
| `noHeaderFiles` | `true` | Internal `jlink` option. |
| `noManPages` | `true` | Internal `jlink` option. |
| `stripDebug` | `true` | Internal `jlink` option. |
| `compress` | none | Internal `jlink --compress` value. |
| `bindServices` | `false` | Internal `jlink --bind-services`. |
| `jlinkOptions` | empty | Extra internal `jlink` options. |

The `leyden` goal checks the selected `javaHome` before packaging and requires
JDK 25 or newer.

## Current Limitations

- The plugin is alpha quality.
- The current source tree has unit tests for command generation, descriptor
  generation, path derivation, staging, and launcher config rewriting.
- The sample projects have been manually verified on macOS for `app-image`
  output. They are not wired into the root `verify` lifecycle yet.
- Linux and Windows installer creation is not yet verified on real Linux and
  Windows hosts.
- JPMS module launch is supported and verified for direct `jpackage` app-image
  packaging. JPMS Leyden packaging still needs dedicated runtime verification.
- GraalVM native-image packaging is out of scope for this plugin.
- Apple notarization is out of scope. Notarization is a post-`jpackage` release
  step, not a JDK `jpackage` option. The plugin supports the JDK macOS signing
  options exposed by `jpackage`.

## Development

```bash
sdk env
./mvnw verify
```

The build currently verifies the Maven plugin itself. Sample projects are
manual until integration tests are added.

## Release Process

The release process is documented in `docs/releasing.md`.

Tags matching `v*` trigger GitHub Actions publishing to Maven Central. The
workflow validates that the tag matches `project.version`, refuses
`-SNAPSHOT` versions, signs artifacts with GPG, and publishes through the
Sonatype Central Publisher Portal. After a successful release, the workflow
bumps `main` to the next patch `-SNAPSHOT` version.

## License

This project is licensed under the Universal Permissive License, Version 1.0.
