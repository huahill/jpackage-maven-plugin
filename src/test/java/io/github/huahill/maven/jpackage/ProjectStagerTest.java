package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectStagerTest {

    @TempDir
    Path tempDir;

    @Test
    void copiesMainArtifactAndRuntimeArtifactsToInputDirectory() throws Exception {
        var mainJar = Files.writeString(tempDir.resolve("app.jar"), "app");
        var runtimeJar = Files.writeString(tempDir.resolve("runtime.jar"), "runtime");
        var input = tempDir.resolve("input");

        new ProjectStager().stage(mainJar, List.of(runtimeJar), input);

        assertTrue(Files.exists(input.resolve("app.jar")));
        assertTrue(Files.exists(input.resolve("runtime.jar")));
    }
}
