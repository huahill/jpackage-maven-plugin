package io.github.huahill.maven.jpackage;

import java.nio.file.Path;
import java.util.List;
import org.apache.maven.api.di.Inject;
import org.apache.maven.api.plugin.Log;
import org.apache.maven.api.plugin.MojoException;
import org.apache.maven.api.plugin.annotations.Mojo;
import org.apache.maven.api.plugin.annotations.Parameter;

@Mojo(name = "jpackage", defaultPhase = "package")
public class JPackageMojo implements org.apache.maven.api.plugin.Mojo {

    @Inject
    private Log log;

    @Parameter
    private List<Path> optionFiles = List.of();

    @Parameter(required = true)
    private String name;

    @Parameter(defaultValue = "app-image", required = true)
    private String packageType;

    @Parameter(property = "jpackage.appVersion", defaultValue = "1.0.0")
    private String packageVersion;

    @Parameter
    private String copyright;

    @Parameter
    private String description;

    @Parameter
    private String vendor;

    @Parameter
    private Path destination;

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

    @Parameter
    private List<String> modules = List.of();

    @Parameter
    private List<String> jlinkOptions = List.of();

    @Parameter
    private List<Path> modulePath = List.of();

    @Parameter
    private Path runtimeImage;

    @Parameter
    private List<Path> appContent = List.of();

    @Parameter
    private Path input;

    @Parameter
    private List<String> addLaunchers = List.of();

    @Parameter
    private List<String> arguments = List.of();

    @Parameter
    private List<String> javaOptions = List.of();

    @Parameter
    private String mainJar;

    @Parameter
    private String mainClass;

    @Parameter
    private String module;

    @Parameter
    private Path appImage;

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
    private String macPackageIdentifier;

    @Parameter
    private String macPackageName;

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

    @Parameter
    private String winMenuGroup;

    @Parameter(defaultValue = "false")
    private boolean winPerUserInstall;

    @Parameter(defaultValue = "false")
    private boolean winShortcut;

    @Parameter(defaultValue = "false")
    private boolean winShortcutPrompt;

    @Parameter
    private String winUpdateUrl;

    @Parameter
    private String winUpgradeUuid;

    @Parameter
    private List<String> extraOptions = List.of();

    @Parameter(defaultValue = "${java.home}")
    private Path javaHome;

    @Override
    public void execute() throws Exception {
        if (destination == null) {
            throw new MojoException("Missing required jpackage destination");
        }
        var os = OperatingSystem.current();
        var executor = new ToolExecutor(javaHome, new ProcessCommandRunner(log));
        executor.run(executor.jpackageCommand(os, options()));
    }

    private JPackageOptions options() {
        return new JPackageOptions(optionFiles, name, packageType, packageVersion, copyright, description,
                destination, icon, temp, vendor, verbose, help, toolVersion, modules, jlinkOptions,
                modulePath, runtimeImage, appContent, input, addLaunchers, arguments, javaOptions,
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
}
