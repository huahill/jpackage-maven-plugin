package io.github.huahill.maven.jpackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class ToolExecutor {

    private final Path javaHome;
    private final CommandRunner commandRunner;

    ToolExecutor(Path javaHome, CommandRunner commandRunner) {
        this.javaHome = javaHome;
        this.commandRunner = commandRunner;
    }

    List<String> jlinkCommand(List<Path> modulePath, Path output, List<String> addModules, boolean noHeaderFiles,
            boolean noManPages, boolean stripDebug, String compress, boolean bindServices, List<String> extraOptions) {
        var command = new ArrayList<String>();
        command.add(javaHome.resolve("bin").resolve("jlink").toString());
        if (noHeaderFiles) {
            command.add("--no-header-files");
        }
        if (noManPages) {
            command.add("--no-man-pages");
        }
        if (stripDebug) {
            command.add("--strip-debug");
        }
        command.add("--output");
        command.add(output.toString());
        command.add("--module-path");
        command.add(joinPaths(modulePath));
        command.add("--add-modules");
        command.add(String.join(",", addModules));
        if (compress != null && !compress.isBlank()) {
            command.add("--compress");
            command.add(compress);
        }
        if (bindServices) {
            command.add("--bind-services");
        }
        command.addAll(extraOptions);
        return command;
    }

    List<String> jpackageCommand(OperatingSystem operatingSystem, JPackageOptions options) {
        var command = new ArrayList<String>();
        command.add(javaHome.resolve("bin").resolve("jpackage").toString());
        addOptionFiles(command, options.optionFiles());
        addCommonOptions(command, options);
        addRuntimeOptions(command, options);
        addAppImageOptions(command, options);
        addLauncherOptions(command, options);
        addPackageOptions(command, options);
        addPlatformOptions(command, operatingSystem, options);
        command.addAll(options.extraOptions());
        return command;
    }

    List<String> appImageCommand(PackagingPaths paths, OperatingSystem operatingSystem, JPackageOptions options,
            String trainingProperty) {
        var command = jpackageCommand(operatingSystem, options);
        command.add("--java-options");
        command.add("-D" + trainingProperty + "=true");
        command.add("--java-options");
        command.add("-XX:AOTCacheOutput=$APPDIR/" + paths.aotCache().getFileName());
        return command;
    }

    List<String> installerCommand(OperatingSystem operatingSystem, JPackageOptions options) {
        return jpackageCommand(operatingSystem, options);
    }

    List<String> codesignCommand(Path appImage) {
        return List.of("/usr/bin/codesign", "-s", "-", "--force", "--deep", appImage.toString());
    }

    void run(List<String> command) throws Exception {
        commandRunner.run(command);
    }

    private String joinPaths(List<Path> paths) {
        var separator = System.getProperty("path.separator");
        return paths.stream()
                .map(Path::toString)
                .collect(java.util.stream.Collectors.joining(separator));
    }

    private void addOptionFiles(List<String> command, List<Path> optionFiles) {
        for (var optionFile : optionFiles) {
            command.add("@" + optionFile);
        }
    }

    private void addCommonOptions(List<String> command, JPackageOptions options) {
        addValue(command, "--name", options.name());
        addValue(command, "--dest", options.destination());
        if (options.verbose()) {
            command.add("--verbose");
        }
        if (options.help()) {
            command.add("--help");
        }
        if (options.toolVersion()) {
            command.add("--version");
        }
        addValue(command, "--type", options.packageType());
        addValue(command, "--app-version", options.packageVersion());
        addValue(command, "--copyright", options.copyright());
        addValue(command, "--description", options.description());
        addValue(command, "--icon", options.icon());
        addValue(command, "--temp", options.temp());
        addValue(command, "--vendor", options.vendor());
    }

    private void addRuntimeOptions(List<String> command, JPackageOptions options) {
        addCsvValues(command, "--add-modules", options.addModules());
        addRepeatedValues(command, "--jlink-options", options.jlinkOptions());
        if (!options.modulePath().isEmpty()) {
            addValue(command, "--module-path", joinPaths(options.modulePath()));
        }
        addValue(command, "--runtime-image", options.runtimeImage());
    }

    private void addAppImageOptions(List<String> command, JPackageOptions options) {
        addCsvPaths(command, "--app-content", options.appContent());
        addValue(command, "--input", options.input());
    }

    private void addLauncherOptions(List<String> command, JPackageOptions options) {
        addRepeatedValues(command, "--add-launcher", options.addLaunchers());
        addRepeatedValues(command, "--arguments", options.arguments());
        addRepeatedValues(command, "--java-options", options.javaOptions());
        if (options.usesModuleLaunch()) {
            addValue(command, "--module", options.module());
        } else {
            addValue(command, "--main-class", options.mainClass());
            addValue(command, "--main-jar", options.mainJar());
        }
    }

    private void addPackageOptions(List<String> command, JPackageOptions options) {
        addValue(command, "--about-url", options.aboutUrl());
        addValue(command, "--app-image", options.appImage());
        addRepeatedPaths(command, "--file-associations", options.fileAssociations());
        addValue(command, "--install-dir", options.installDir());
        if (options.launcherAsService()) {
            command.add("--launcher-as-service");
        }
        addValue(command, "--license-file", options.licenseFile());
        addValue(command, "--resource-dir", options.resourceDir());
    }

    private void addPlatformOptions(List<String> command, OperatingSystem operatingSystem, JPackageOptions options) {
        switch (operatingSystem) {
            case MACOS -> {
                addValue(command, "--mac-app-category", options.macAppCategory());
                addValue(command, "--mac-app-image-sign-identity", options.macAppImageSignIdentity());
                if (options.macAppStore()) {
                    command.add("--mac-app-store");
                }
                addCsvPaths(command, "--mac-dmg-content", options.macDmgContent());
                addValue(command, "--mac-entitlements", options.macEntitlements());
                addValue(command, "--mac-installer-sign-identity", options.macInstallerSignIdentity());
                addValue(command, "--mac-package-identifier", options.macPackageIdentifier());
                addValue(command, "--mac-package-name", options.macPackageName());
                addValue(command, "--mac-package-signing-prefix", options.macPackageSigningPrefix());
                if (options.macSign()) {
                    command.add("--mac-sign");
                }
                addValue(command, "--mac-signing-key-user-name", options.macSigningKeyUserName());
                addValue(command, "--mac-signing-keychain", options.macSigningKeychain());
            }
            case LINUX -> {
                addValue(command, "--linux-app-category", options.linuxAppCategory());
                addValue(command, "--linux-app-release", options.linuxAppRelease());
                addValue(command, "--linux-deb-maintainer", options.linuxDebMaintainer());
                addValue(command, "--linux-menu-group", options.linuxMenuGroup());
                addCsvValues(command, "--linux-package-deps", options.linuxPackageDeps());
                addValue(command, "--linux-package-name", options.linuxPackageName());
                addValue(command, "--linux-rpm-license-type", options.linuxRpmLicenseType());
                if (options.linuxShortcut()) {
                    command.add("--linux-shortcut");
                }
            }
            case WINDOWS -> {
                if (options.winConsole()) {
                    command.add("--win-console");
                }
                if (options.winDirChooser()) {
                    command.add("--win-dir-chooser");
                }
                addValue(command, "--win-help-url", options.winHelpUrl());
                if (options.winMenu()) {
                    command.add("--win-menu");
                }
                addValue(command, "--win-menu-group", options.winMenuGroup());
                if (options.winPerUserInstall()) {
                    command.add("--win-per-user-install");
                }
                if (options.winShortcut()) {
                    command.add("--win-shortcut");
                }
                if (options.winShortcutPrompt()) {
                    command.add("--win-shortcut-prompt");
                }
                addValue(command, "--win-update-url", options.winUpdateUrl());
                addValue(command, "--win-upgrade-uuid", options.winUpgradeUuid());
            }
        }
    }

    private void addRepeatedValues(List<String> command, String option, List<String> values) {
        for (var value : values) {
            addValue(command, option, value);
        }
    }

    private void addRepeatedPaths(List<String> command, String option, List<Path> values) {
        for (var value : values) {
            addValue(command, option, value);
        }
    }

    private void addCsvValues(List<String> command, String option, List<String> values) {
        if (!values.isEmpty()) {
            addValue(command, option, String.join(",", values));
        }
    }

    private void addCsvPaths(List<String> command, String option, List<Path> values) {
        if (!values.isEmpty()) {
            var csv = values.stream()
                    .map(Path::toString)
                    .collect(java.util.stream.Collectors.joining(","));
            addValue(command, option, csv);
        }
    }

    private void addValue(List<String> command, String option, Path value) {
        if (value != null) {
            addValue(command, option, value.toString());
        }
    }

    private void addValue(List<String> command, String option, String value) {
        if (value != null && !value.isBlank()) {
            command.add(option);
            command.add(value);
        }
    }
}
