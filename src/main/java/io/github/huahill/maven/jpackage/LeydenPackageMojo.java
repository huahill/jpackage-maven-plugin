package io.github.huahill.maven.jpackage;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.api.PathScope;
import org.apache.maven.api.Project;
import org.apache.maven.api.Session;
import org.apache.maven.api.di.Inject;
import org.apache.maven.api.plugin.Log;
import org.apache.maven.api.plugin.MojoException;
import org.apache.maven.api.plugin.annotations.Mojo;
import org.apache.maven.api.plugin.annotations.Parameter;

@Mojo(name = "leyden", defaultPhase = "package")
public class LeydenPackageMojo implements org.apache.maven.api.plugin.Mojo {

    private static final int MIN_LEYDEN_JAVA_FEATURE_VERSION = 25;
    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("JAVA_VERSION=\"([^\"]+)\"");

    @Inject
    private Project project;

    @Inject
    private Session session;

    @Inject
    private Log log;

    @Parameter
    private List<Path> optionFiles = List.of();

    @Parameter(required = true)
    private String name;

    @Parameter
    private String mainClass;

    @Parameter(defaultValue = "${project.build.finalName}.jar", required = true)
    private String mainJar;

    @Parameter
    private String module;

    @Parameter(property = "jpackage.appVersion", defaultValue = "1.0.0")
    private String packageVersion;

    @Parameter
    private String copyright;

    @Parameter
    private String description;

    @Parameter
    private String vendor;

    @Parameter
    private Path icon;

    @Parameter
    private Path temp;

    @Parameter(defaultValue = "true")
    private boolean verbose;

    @Parameter(defaultValue = "false")
    private boolean help;

    @Parameter(defaultValue = "false")
    private boolean toolVersion;

    @Parameter(required = true)
    private List<String> runtimeModules = List.of();

    @Parameter
    private List<Path> modulePath = List.of();

    @Parameter(defaultValue = "true")
    private boolean includeInputDirectoryOnModulePath;

    @Parameter(defaultValue = "true")
    private boolean noHeaderFiles;

    @Parameter(defaultValue = "true")
    private boolean noManPages;

    @Parameter(defaultValue = "true")
    private boolean stripDebug;

    @Parameter
    private String compress;

    @Parameter(defaultValue = "false")
    private boolean bindServices;

    @Parameter
    private List<String> jlinkOptions = List.of();

    @Parameter
    private List<String> javaOptions = List.of();

    @Parameter
    private List<String> addLaunchers = List.of();

    @Parameter
    private List<String> arguments = List.of();

    @Parameter
    private List<Path> appContent = List.of();

    @Parameter
    private String aboutUrl;

    @Parameter
    private List<Path> fileAssociations = List.of();

    @Parameter
    private String installDir;

    @Parameter(defaultValue = "false")
    private boolean launcherAsService;

    @Parameter
    private Path licenseFile;

    @Parameter
    private Path resourceDir;

    @Parameter(defaultValue = "leyden.training")
    private String trainingProperty;

    @Parameter(defaultValue = "startup.aot")
    private String aotCacheName;

    @Parameter
    private String macPackageIdentifier;

    @Parameter
    private String macPackageName;

    @Parameter
    private String macAppCategory;

    @Parameter
    private String macAppImageSignIdentity;

    @Parameter(defaultValue = "false")
    private boolean macAppStore;

    @Parameter
    private List<Path> macDmgContent = List.of();

    @Parameter
    private Path macEntitlements;

    @Parameter
    private String macInstallerSignIdentity;

    @Parameter
    private String macPackageSigningPrefix;

    @Parameter(defaultValue = "false")
    private boolean macSign;

    @Parameter
    private String macSigningKeyUserName;

    @Parameter
    private String macSigningKeychain;

    @Parameter
    private String linuxPackageName;

    @Parameter
    private String linuxAppCategory;

    @Parameter
    private String linuxAppRelease;

    @Parameter
    private String linuxDebMaintainer;

    @Parameter
    private String linuxMenuGroup;

    @Parameter
    private List<String> linuxPackageDeps = List.of();

    @Parameter
    private String linuxRpmLicenseType;

    @Parameter(defaultValue = "false")
    private boolean linuxShortcut;

    @Parameter(defaultValue = "false")
    private boolean winConsole;

    @Parameter(defaultValue = "false")
    private boolean winDirChooser;

    @Parameter
    private String winHelpUrl;

    @Parameter(defaultValue = "false")
    private boolean winMenu;

    @Parameter(defaultValue = "false")
    private boolean winShortcut;

    @Parameter
    private String winMenuGroup;

    @Parameter(defaultValue = "false")
    private boolean winPerUserInstall;

    @Parameter(defaultValue = "false")
    private boolean winShortcutPrompt;

    @Parameter
    private String winUpdateUrl;

    @Parameter
    private String winUpgradeUuid;

    @Parameter
    private List<String> extraOptions = List.of();

    @Parameter(defaultValue = "${project.build.directory}/jpackage-input")
    private Path inputDirectory;

    @Parameter(defaultValue = "${project.build.directory}/jlink-runtime")
    private Path runtimeDirectory;

    @Parameter(defaultValue = "${project.build.directory}/jpackage-app-image")
    private Path appImageDirectory;

    @Parameter(defaultValue = "${project.build.directory}/jpackage")
    private Path packageDirectory;

    @Parameter(defaultValue = "${java.home}")
    private Path javaHome;

    static String defaultAotCacheName() {
        return "startup.aot";
    }

    static Path defaultJavaHome() {
        return Path.of(System.getProperty("java.home"));
    }

    static int javaFeatureVersion(Path javaHome) throws IOException {
        var releaseFile = javaHome.resolve("release");
        var release = Files.readString(releaseFile);
        var matcher = JAVA_VERSION_PATTERN.matcher(release);
        if (!matcher.find()) {
            throw new IOException("Missing JAVA_VERSION in " + releaseFile);
        }
        return parseFeatureVersion(matcher.group(1));
    }

    static int parseFeatureVersion(String javaVersion) {
        var normalized = javaVersion.startsWith("1.") ? javaVersion.substring(2) : javaVersion;
        var end = 0;
        while (end < normalized.length() && Character.isDigit(normalized.charAt(end))) {
            end++;
        }
        if (end == 0) {
            throw new IllegalArgumentException("Unsupported Java version: " + javaVersion);
        }
        return Integer.parseInt(normalized.substring(0, end));
    }

    @Override
    public void execute() throws Exception {
        var os = OperatingSystem.current();
        var paths = PackagingPaths.derive(inputDirectory, runtimeDirectory, appImageDirectory,
                packageDirectory, name, packageVersion, aotCacheName, os);
        var executor = new ToolExecutor(javaHome, new ProcessCommandRunner(log));

        try {
            requireLeydenJava(javaHome);
            if (runtimeModules.isEmpty()) {
                throw new MojoException("Missing required Leyden runtimeModules");
            }
            if ((module == null || module.isBlank()) && (mainClass == null || mainClass.isBlank())) {
                throw new MojoException("Missing required Leyden mainClass or module");
            }
            deleteIfExists(paths.inputDirectory());
            deleteIfExists(paths.runtimeDirectory());
            deleteIfExists(paths.appImageDirectory());
            deleteIfExists(paths.packageDirectory());

            var mainArtifact = project.getMainArtifact()
                    .flatMap(session::getArtifactPath)
                    .orElseThrow(() -> new MojoException("Missing main project artifact path"));
            var runtimeArtifacts = session.resolveDependencies(project, PathScope.MAIN_RUNTIME).stream()
                    .filter(path -> !path.equals(mainArtifact))
                    .toList();

            new ProjectStager().stage(mainArtifact, runtimeArtifacts, paths.inputDirectory());
            var effectiveModulePath = new java.util.ArrayList<Path>();
            if (includeInputDirectoryOnModulePath) {
                effectiveModulePath.add(paths.inputDirectory());
            }
            effectiveModulePath.addAll(modulePath);
            if (effectiveModulePath.isEmpty()) {
                throw new MojoException("Missing required Leyden jlink modulePath");
            }
            executor.run(executor.jlinkCommand(effectiveModulePath, paths.runtimeDirectory(),
                    runtimeModules, noHeaderFiles, noManPages, stripDebug, compress, bindServices, jlinkOptions));
            executor.run(executor.appImageCommand(paths, os, appImageOptions(paths), trainingProperty));
            executor.run(List.of(paths.appExecutable().toString()));
            if (!Files.exists(paths.aotCache())) {
                throw new MojoException("Leyden AOT cache was not generated: " + paths.aotCache());
            }
            new LauncherConfigEditor().rewriteForRuntime(paths.appConfig(), trainingProperty, aotCacheName);
            if (os == OperatingSystem.MACOS) {
                executor.run(executor.codesignCommand(paths.appImage()));
            }
            executor.run(executor.installerCommand(os, installerOptions(paths, os)));
        } catch (MojoException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoException("Leyden packaging failed", e);
        }
    }

    private void requireLeydenJava(Path javaHome) throws MojoException {
        try {
            var featureVersion = javaFeatureVersion(javaHome);
            if (featureVersion < MIN_LEYDEN_JAVA_FEATURE_VERSION) {
                throw new MojoException("Leyden packaging requires JDK "
                        + MIN_LEYDEN_JAVA_FEATURE_VERSION + " or newer for -XX:AOTCacheOutput, but javaHome is JDK "
                        + featureVersion + ": " + javaHome);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new MojoException("Cannot determine Java version for Leyden javaHome: " + javaHome, e);
        }
    }

    private JPackageOptions appImageOptions(PackagingPaths paths) {
        return baseOptions("app-image", paths.appImageDirectory(), paths.runtimeDirectory(), paths.inputDirectory(), null);
    }

    private JPackageOptions installerOptions(PackagingPaths paths, OperatingSystem operatingSystem) {
        return installerOnlyOptions(operatingSystem.jpackageType(), paths.packageDirectory(), paths.appImage());
    }

    private JPackageOptions baseOptions(String packageType, Path destination, Path runtimeImage, Path input, Path appImage) {
        return new JPackageOptions(optionFiles, name, packageType, packageVersion, copyright, description,
                destination, icon, temp, vendor, verbose, help, toolVersion, List.of(), List.of(),
                List.of(), runtimeImage, appContent, input, addLaunchers, arguments, javaOptions,
                mainClass, mainJar, module, aboutUrl, appImage, fileAssociations, installDir,
                launcherAsService, licenseFile, resourceDir, macAppCategory, macAppImageSignIdentity,
                macAppStore, macDmgContent, macEntitlements, macInstallerSignIdentity,
                macPackageIdentifier, macPackageName, macPackageSigningPrefix, macSign,
                macSigningKeyUserName, macSigningKeychain, linuxAppCategory, linuxAppRelease,
                linuxDebMaintainer, linuxMenuGroup, linuxPackageDeps, linuxPackageName,
                linuxRpmLicenseType, linuxShortcut, winConsole, winDirChooser, winHelpUrl, winMenu,
                winMenuGroup, winPerUserInstall, winShortcut, winShortcutPrompt, winUpdateUrl,
                winUpgradeUuid, extraOptions);
    }

    private JPackageOptions installerOnlyOptions(String packageType, Path destination, Path appImage) {
        return new JPackageOptions(optionFiles, name, packageType, packageVersion, copyright, description,
                destination, icon, temp, vendor, verbose, help, toolVersion, List.of(), List.of(),
                List.of(), null, List.of(), null, List.of(), List.of(), List.of(), null, null, null,
                aboutUrl, appImage, fileAssociations, installDir, launcherAsService, licenseFile, resourceDir,
                macAppCategory, macAppImageSignIdentity, macAppStore, macDmgContent, macEntitlements,
                macInstallerSignIdentity, macPackageIdentifier, macPackageName, macPackageSigningPrefix,
                macSign, macSigningKeyUserName, macSigningKeychain, linuxAppCategory, linuxAppRelease,
                linuxDebMaintainer, linuxMenuGroup, linuxPackageDeps, linuxPackageName,
                linuxRpmLicenseType, linuxShortcut, winConsole, winDirChooser, winHelpUrl, winMenu,
                winMenuGroup, winPerUserInstall, winShortcut, winShortcutPrompt, winUpdateUrl,
                winUpgradeUuid, extraOptions);
    }

    private void deleteIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
