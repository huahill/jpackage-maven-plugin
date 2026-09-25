package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ToolExecutorTest {

    @Test
    void buildsJlinkCommand() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.jlinkCommand(List.of(Path.of("input")), Path.of("runtime"),
                List.of("java.desktop", "jdk.jfr"), true, true, true, null, false, List.of());

        assertEquals(List.of("/jdk/bin/jlink", "--no-header-files", "--no-man-pages", "--strip-debug",
                "--output", "runtime", "--module-path", "input", "--add-modules", "java.desktop,jdk.jfr"),
                command);
    }

    @Test
    void buildsJlinkCommandWithGenericOptions() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.jlinkCommand(List.of(Path.of("mods"), Path.of("libs")), Path.of("runtime"),
                List.of("java.base", "java.logging"), false, false, false, "zip-6", true,
                List.of("--include-locales", "en,zh"));

        assertEquals(List.of("/jdk/bin/jlink", "--output", "runtime",
                "--module-path", "mods:libs", "--add-modules", "java.base,java.logging",
                "--compress", "zip-6", "--bind-services", "--include-locales", "en,zh"), command);
    }

    @Test
    void buildsTrainingAppImageJpackageCommand() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.MACOS);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.appImageCommand(paths, OperatingSystem.MACOS,
                sampleOptions("app-image", paths.appImageDirectory(), paths.runtimeDirectory(), paths.inputDirectory(), null),
                "sample.training");

        assertEquals("app-image", command.get(command.indexOf("--type") + 1));
        assertTrue(command.contains("-Dsample.training=true"));
        assertTrue(command.contains("-XX:AOTCacheOutput=$APPDIR/startup.aot"));
    }

    @Test
    void appImageCommandAddsOnlyMacOsPackageOptionsOnMacOs() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.MACOS);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.appImageCommand(paths, OperatingSystem.MACOS,
                sampleOptions("app-image", paths.appImageDirectory(), paths.runtimeDirectory(), paths.inputDirectory(), null),
                "sample.training");

        assertTrue(command.contains("--mac-package-identifier"));
        assertTrue(command.contains("--mac-package-name"));
        assertFalse(command.contains("--linux-package-name"));
        assertFalse(command.contains("--linux-app-category"));
        assertFalse(command.contains("--win-menu"));
        assertFalse(command.contains("--win-shortcut"));
        assertFalse(command.contains("--win-menu-group"));
    }

    @Test
    void appImageCommandAddsOnlyLinuxPackageOptionsOnLinux() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.LINUX);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.appImageCommand(paths, OperatingSystem.LINUX,
                sampleOptions("app-image", paths.appImageDirectory(), paths.runtimeDirectory(), paths.inputDirectory(), null),
                "sample.training");

        assertFalse(command.contains("--mac-package-identifier"));
        assertFalse(command.contains("--mac-package-name"));
        assertTrue(command.contains("--linux-package-name"));
        assertTrue(command.contains("--linux-app-category"));
        assertFalse(command.contains("--win-menu"));
        assertFalse(command.contains("--win-shortcut"));
        assertFalse(command.contains("--win-menu-group"));
    }

    @Test
    void appImageCommandAddsOnlyWindowsPackageOptionsOnWindows() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.WINDOWS);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.appImageCommand(paths, OperatingSystem.WINDOWS,
                sampleOptions("app-image", paths.appImageDirectory(), paths.runtimeDirectory(), paths.inputDirectory(), null),
                "sample.training");

        assertFalse(command.contains("--mac-package-identifier"));
        assertFalse(command.contains("--mac-package-name"));
        assertFalse(command.contains("--linux-package-name"));
        assertFalse(command.contains("--linux-app-category"));
        assertTrue(command.contains("--win-menu"));
        assertTrue(command.contains("--win-shortcut"));
        assertTrue(command.contains("--win-menu-group"));
    }

    @Test
    void buildsInstallerCommandFromAppImage() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.MACOS);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.installerCommand(OperatingSystem.MACOS,
                sampleOptions(OperatingSystem.MACOS.jpackageType(), paths.packageDirectory(), null, null, paths.appImage()));

        assertEquals("dmg", command.get(command.indexOf("--type") + 1));
        assertEquals(paths.appImage().toString(), command.get(command.indexOf("--app-image") + 1));
    }

    @Test
    void installerCommandFromAppImageDoesNotNeedLauncherOptions() {
        var paths = samplePaths("Sample App", "startup.aot", OperatingSystem.MACOS);
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.installerCommand(OperatingSystem.MACOS,
                minimalInstallerOptions(OperatingSystem.MACOS.jpackageType(), paths.packageDirectory(), paths.appImage()));

        assertTrue(command.contains("--app-image"));
        assertFalse(command.contains("--java-options"));
        assertFalse(command.contains("--main-class"));
        assertFalse(command.contains("--main-jar"));
        assertFalse(command.contains("--module"));
        assertFalse(command.contains("--runtime-image"));
        assertFalse(command.contains("--input"));
    }

    @Test
    void buildsBaseJpackageCommandWithoutLeydenTrainingOptions() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.jpackageCommand(OperatingSystem.MACOS,
                sampleOptions("app-image", Path.of("package-output"), Path.of("runtime"), Path.of("input"), null));

        assertEquals("app-image", command.get(command.indexOf("--type") + 1));
        assertEquals("input", command.get(command.indexOf("--input") + 1));
        assertEquals("runtime", command.get(command.indexOf("--runtime-image") + 1));
        assertTrue(command.contains("--mac-package-identifier"));
        assertFalse(command.contains("-Dsample.training=true"));
        assertFalse(command.stream().anyMatch(option -> option.contains("AOTCacheOutput")));
    }

    @Test
    void baseJpackageCommandOmitsUnsetOptionalBrandingOptions() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.jpackageCommand(OperatingSystem.MACOS,
                minimalOptions("Generic App", "app-image", Path.of("package-output"), Path.of("input"),
                        "app.jar", "com.acme.Main", null));

        assertFalse(command.contains("--vendor"));
        assertFalse(command.contains("--description"));
        assertFalse(command.contains("--mac-package-identifier"));
        assertFalse(command.contains("--mac-package-name"));
        assertFalse(command.contains("huahill"));
        assertFalse(command.contains("io.github.huahill"));
    }

    @Test
    void moduleLaunchUsesModuleInsteadOfMainJarAndMainClass() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var command = executor.jpackageCommand(OperatingSystem.MACOS,
                minimalOptions("Module App", "app-image", Path.of("package-output"), null,
                        "ignored.jar", "com.acme.Ignored", "com.acme.app/com.acme.Main"));

        assertTrue(command.contains("--module"));
        assertEquals("com.acme.app/com.acme.Main", command.get(command.indexOf("--module") + 1));
        assertFalse(command.contains("--main-jar"));
        assertFalse(command.contains("--main-class"));
    }

    @Test
    void supportsEveryCurrentJpackageOptionGroupThroughStructuredOptions() {
        var executor = new ToolExecutor(Path.of("/jdk"), new RecordingRunner());

        var macCommand = executor.jpackageCommand(OperatingSystem.MACOS, exhaustiveOptions("pkg", Path.of("out")));
        assertContainsOptions(macCommand, "@opts.txt", "--type", "--app-version", "--copyright",
                "--description", "--dest", "--icon", "--name", "--temp", "--vendor", "--verbose",
                "--add-modules", "--jlink-options", "--module-path", "--runtime-image", "--app-content",
                "--input", "--add-launcher", "--arguments", "--java-options", "--main-class", "--main-jar",
                "--about-url", "--app-image", "--file-associations", "--install-dir", "--launcher-as-service",
                "--license-file", "--resource-dir", "--mac-app-category", "--mac-app-image-sign-identity",
                "--mac-app-store", "--mac-dmg-content", "--mac-entitlements", "--mac-installer-sign-identity",
                "--mac-package-identifier", "--mac-package-name", "--mac-package-signing-prefix", "--mac-sign",
                "--mac-signing-key-user-name", "--mac-signing-keychain", "--future-option");

        var linuxCommand = executor.jpackageCommand(OperatingSystem.LINUX, exhaustiveOptions("deb", Path.of("out")));
        assertContainsOptions(linuxCommand, "--linux-app-category", "--linux-app-release",
                "--linux-deb-maintainer", "--linux-menu-group", "--linux-package-deps",
                "--linux-package-name", "--linux-rpm-license-type", "--linux-shortcut");

        var windowsCommand = executor.jpackageCommand(OperatingSystem.WINDOWS, exhaustiveOptions("msi", Path.of("out")));
        assertContainsOptions(windowsCommand, "--win-console", "--win-dir-chooser", "--win-help-url",
                "--win-menu", "--win-menu-group", "--win-per-user-install", "--win-shortcut",
                "--win-shortcut-prompt", "--win-update-url", "--win-upgrade-uuid");
    }

    private void assertContainsOptions(List<String> command, String... options) {
        for (var option : options) {
            assertTrue(command.contains(option), () -> "missing jpackage option " + option + " in " + command);
        }
    }

    private JPackageOptions sampleOptions(String packageType, Path destination, Path runtimeImage, Path input,
            Path appImage) {
        return new JPackageOptions(List.of(), "Sample App", packageType, "1.0.0", null, "Description",
                destination, null, null, "Acme", true, false, false, List.of(), List.of(), List.of(), runtimeImage,
                List.of(), input, List.of(), List.of(), List.of("-Dsample.option=true"), "com.acme.Main",
                "sample-app.jar", null, null, appImage, List.of(), null, false, null, null, null, null, false,
                List.of(), null, null, "com.acme.app", "Sample App", null, false, null, null, "Development", null,
                null, null, List.of(), "sample-app", null, false, false, false, null, true, "Sample App", false,
                true, false, null, null, List.of());
    }

    private JPackageOptions minimalOptions(String name, String packageType, Path destination, Path input,
            String mainJar, String mainClass, String module) {
        return new JPackageOptions(List.of(), name, packageType, "1.0.0", null, null, destination, null, null,
                null, true, false, false, List.of(), List.of(), List.of(), null, List.of(), input, List.of(),
                List.of(), List.of(), mainClass, mainJar, module, null, null, List.of(), null, false, null, null,
                null, null, false, List.of(), null, null, null, null, null, false, null, null, null, null, null,
                null, List.of(), null, null, false, false, false, null, false, null, false, false, false, null,
                null, List.of());
    }

    private JPackageOptions minimalInstallerOptions(String packageType, Path destination, Path appImage) {
        return new JPackageOptions(List.of(), "Sample App", packageType, "1.0.0", null, null, destination,
                null, null, null, true, false, false, List.of(), List.of(), List.of(), null, List.of(), null,
                List.of(), List.of(), List.of(), null, null, null, null, appImage, List.of(), null, false, null,
                null, null, null, false, List.of(), null, null, null, null, null, false, null, null, null, null,
                null, null, List.of(), null, null, false, false, false, null, false, null, false, false, false,
                null, null, List.of());
    }

    private JPackageOptions exhaustiveOptions(String packageType, Path destination) {
        return new JPackageOptions(List.of(Path.of("opts.txt")), "Sample App", packageType, "1.0.0", "Copyright",
                "Description", destination, Path.of("icon.icns"), Path.of("tmp"), "Acme", true, false, false,
                List.of("java.desktop"), List.of("--strip-debug"), List.of(Path.of("mods")), Path.of("runtime"),
                List.of(Path.of("content")), Path.of("input"), List.of("helper=helper.properties"), List.of("--open"),
                List.of("-Xmx256m"), "com.acme.Main", "sample-app.jar", null, "https://example.com",
                Path.of("app-image"), List.of(Path.of("assoc.properties")), "/Applications/Sample App", true,
                Path.of("LICENSE.txt"), Path.of("resources"), "public.app-category.developer-tools", "Developer ID App",
                true, List.of(Path.of("README.txt")), Path.of("entitlements.plist"), "Developer ID Installer",
                "com.acme.app", "Sample App", "com.acme", true, "ACME", "login.keychain-db", "Development",
                "1", "Acme Maintainer", "Development", List.of("libc6"), "sample-app", "MIT", true, true,
                true, "https://example.com/help", true, "Sample App", true, true, true,
                "https://example.com/update", "12345678-1234-1234-1234-123456789abc",
                List.of("--future-option", "future-value"));
    }

    private PackagingPaths samplePaths(String appName, String aotCacheName, OperatingSystem operatingSystem) {
        return PackagingPaths.derive(Path.of("target", "jpackage-input"),
                Path.of("target", "jlink-runtime"),
                Path.of("target", "jpackage-app-image"),
                Path.of("target", "jpackage"),
                appName, "1.0.0", aotCacheName, operatingSystem);
    }

    private static final class RecordingRunner implements CommandRunner {
        @Override
        public void run(List<String> command) {
        }
    }
}
