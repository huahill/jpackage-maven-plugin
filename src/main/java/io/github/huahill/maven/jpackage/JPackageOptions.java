package io.github.huahill.maven.jpackage;

import java.nio.file.Path;
import java.util.List;

record JPackageOptions(
        List<Path> optionFiles,
        String name,
        String packageType,
        String packageVersion,
        String copyright,
        String description,
        Path destination,
        Path icon,
        Path temp,
        String vendor,
        boolean verbose,
        boolean help,
        boolean toolVersion,
        List<String> addModules,
        List<String> jlinkOptions,
        List<Path> modulePath,
        Path runtimeImage,
        List<Path> appContent,
        Path input,
        List<String> addLaunchers,
        List<String> arguments,
        List<String> javaOptions,
        String mainClass,
        String mainJar,
        String module,
        String aboutUrl,
        Path appImage,
        List<Path> fileAssociations,
        String installDir,
        boolean launcherAsService,
        Path licenseFile,
        Path resourceDir,
        String macAppCategory,
        String macAppImageSignIdentity,
        boolean macAppStore,
        List<Path> macDmgContent,
        Path macEntitlements,
        String macInstallerSignIdentity,
        String macPackageIdentifier,
        String macPackageName,
        String macPackageSigningPrefix,
        boolean macSign,
        String macSigningKeyUserName,
        String macSigningKeychain,
        String linuxAppCategory,
        String linuxAppRelease,
        String linuxDebMaintainer,
        String linuxMenuGroup,
        List<String> linuxPackageDeps,
        String linuxPackageName,
        String linuxRpmLicenseType,
        boolean linuxShortcut,
        boolean winConsole,
        boolean winDirChooser,
        String winHelpUrl,
        boolean winMenu,
        String winMenuGroup,
        boolean winPerUserInstall,
        boolean winShortcut,
        boolean winShortcutPrompt,
        String winUpdateUrl,
        String winUpgradeUuid,
        List<String> extraOptions) {

    JPackageOptions {
        optionFiles = safe(optionFiles);
        addModules = safe(addModules);
        jlinkOptions = safe(jlinkOptions);
        modulePath = safe(modulePath);
        appContent = safe(appContent);
        addLaunchers = safe(addLaunchers);
        arguments = safe(arguments);
        javaOptions = safe(javaOptions);
        fileAssociations = safe(fileAssociations);
        macDmgContent = safe(macDmgContent);
        linuxPackageDeps = safe(linuxPackageDeps);
        extraOptions = safe(extraOptions);
    }

    boolean usesModuleLaunch() {
        return module != null && !module.isBlank();
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
