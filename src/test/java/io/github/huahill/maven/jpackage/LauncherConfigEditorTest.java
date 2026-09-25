package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LauncherConfigEditorTest {

    @TempDir
    Path tempDir;

    @Test
    void rewritesTrainingOptionsToRuntimeAotCache() throws Exception {
        var config = tempDir.resolve("Sample App.cfg");
        Files.writeString(config, """
                [JavaOptions]
                java-options=-Dsample.option=true
                java-options=-Dsample.training=true
                java-options=-XX:AOTCacheOutput=$APPDIR/startup.aot
                """);

        new LauncherConfigEditor().rewriteForRuntime(config, "sample.training", "startup.aot");

        assertEquals("""
                [JavaOptions]
                java-options=-Dsample.option=true
                java-options=-XX:AOTCache=$APPDIR/startup.aot
                """, Files.readString(config));
    }

    @Test
    void failsWhenAotOutputOptionIsMissing() throws Exception {
        var config = tempDir.resolve("Sample App.cfg");
        Files.writeString(config, """
                [JavaOptions]
                java-options=-Dsample.training=true
                """);

        var error = assertThrows(IllegalStateException.class,
                () -> new LauncherConfigEditor().rewriteForRuntime(config, "sample.training",
                        "startup.aot"));

        assertEquals("Missing Leyden AOT cache output option in " + config, error.getMessage());
    }
}
