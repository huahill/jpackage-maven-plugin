package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PackagingPathsTest {

    @Test
    void derivesMacOsJpackageLayout() {
        var paths = samplePaths("Sample App", "startup.aot",
                OperatingSystem.MACOS);

        assertEquals(Path.of("target", "jpackage-input"), paths.inputDirectory());
        assertEquals(Path.of("target", "jlink-runtime"), paths.runtimeDirectory());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App.app"), paths.appImage());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App.app", "Contents", "MacOS", "Sample App"),
                paths.appExecutable());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App.app", "Contents", "app", "Sample App.cfg"),
                paths.appConfig());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App.app", "Contents", "app",
                "startup.aot"), paths.aotCache());
        assertEquals(Path.of("target", "jpackage", "Sample App-1.0.0.dmg"), paths.installer());
    }

    @Test
    void derivesLinuxJpackageLayout() {
        var paths = samplePaths("Sample App", "startup.aot",
                OperatingSystem.LINUX);

        assertEquals(Path.of("target", "jpackage-app-image", "Sample App"), paths.appImage());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App", "bin", "Sample App"),
                paths.appExecutable());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App", "lib", "app", "Sample App.cfg"),
                paths.appConfig());
        assertEquals(Path.of("target", "jpackage", "Sample App-1.0.0.deb"), paths.installer());
    }

    @Test
    void derivesWindowsJpackageLayout() {
        var paths = samplePaths("Sample App", "startup.aot",
                OperatingSystem.WINDOWS);

        assertEquals(Path.of("target", "jpackage-app-image", "Sample App"), paths.appImage());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App", "Sample App.exe"),
                paths.appExecutable());
        assertEquals(Path.of("target", "jpackage-app-image", "Sample App", "app", "Sample App.cfg"),
                paths.appConfig());
        assertEquals(Path.of("target", "jpackage", "Sample App-1.0.0.msi"), paths.installer());
    }

    private PackagingPaths samplePaths(String appName, String aotCacheName, OperatingSystem operatingSystem) {
        return PackagingPaths.derive(Path.of("target", "jpackage-input"),
                Path.of("target", "jlink-runtime"),
                Path.of("target", "jpackage-app-image"),
                Path.of("target", "jpackage"),
                appName, "1.0.0", aotCacheName, operatingSystem);
    }
}
