package io.github.huahill.maven.jpackage;

enum OperatingSystem {
    MACOS("dmg", "dmg"),
    LINUX("deb", "deb"),
    WINDOWS("msi", "msi");

    private final String jpackageType;
    private final String installerExtension;

    OperatingSystem(String jpackageType, String installerExtension) {
        this.jpackageType = jpackageType;
        this.installerExtension = installerExtension;
    }

    String jpackageType() {
        return jpackageType;
    }

    String installerExtension() {
        return installerExtension;
    }

    static OperatingSystem current() {
        var osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("mac")) {
            return MACOS;
        }
        if (osName.contains("win")) {
            return WINDOWS;
        }
        return LINUX;
    }
}
