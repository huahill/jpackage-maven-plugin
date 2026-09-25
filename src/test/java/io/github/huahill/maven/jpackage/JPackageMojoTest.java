package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JPackageMojoTest {

    @Test
    void declaresBaseJpackageGoalInPackagePhase() throws Exception {
        var descriptor = Files.readString(Path.of("target/classes/META-INF/maven/plugin.xml"));

        assertTrue(descriptor.contains("<goal>jpackage</goal>"));
        assertTrue(descriptor.contains("<implementation>io.github.huahill.maven.jpackage.JPackageMojo</implementation>"));
        assertTrue(descriptor.contains("<phase>package</phase>"));
    }

    @Test
    void exposesCurrentJpackageOptions() throws Exception {
        var descriptor = Files.readString(Path.of("target/classes/META-INF/maven/plugin.xml"));

        assertParameters(descriptor,
                "optionFiles", "name", "packageType", "packageVersion", "copyright", "description",
                "destination", "icon", "temp", "vendor", "verbose", "modules", "jlinkOptions",
                "modulePath", "runtimeImage", "appContent", "input", "addLaunchers", "arguments",
                "javaOptions", "mainJar", "mainClass", "module", "aboutUrl", "appImage",
                "fileAssociations", "installDir", "launcherAsService", "licenseFile", "resourceDir",
                "macAppCategory", "macAppImageSignIdentity", "macAppStore", "macDmgContent",
                "macEntitlements", "macInstallerSignIdentity", "macPackageIdentifier", "macPackageName",
                "macPackageSigningPrefix", "macSign", "macSigningKeyUserName", "macSigningKeychain",
                "linuxAppCategory", "linuxAppRelease", "linuxDebMaintainer", "linuxMenuGroup",
                "linuxPackageDeps", "linuxPackageName", "linuxRpmLicenseType", "linuxShortcut",
                "winConsole", "winDirChooser", "winHelpUrl", "winMenu", "winMenuGroup",
                "winPerUserInstall", "winShortcut", "winShortcutPrompt", "winUpdateUrl",
                "winUpgradeUuid", "extraOptions");
    }

    private void assertParameters(String descriptor, String... names) {
        for (var name : names) {
            assertTrue(descriptor.contains("<name>" + name + "</name>"),
                    () -> "missing Maven parameter " + name);
        }
    }
}
