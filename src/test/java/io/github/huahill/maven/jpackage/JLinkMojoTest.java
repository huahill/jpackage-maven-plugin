package io.github.huahill.maven.jpackage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JLinkMojoTest {

    @Test
    void declaresJlinkGoalInPackagePhase() throws Exception {
        var descriptor = Files.readString(Path.of("target/classes/META-INF/maven/plugin.xml"));

        assertTrue(descriptor.contains("<goal>jlink</goal>"));
        assertTrue(descriptor.contains("<implementation>io.github.huahill.maven.jpackage.JLinkMojo</implementation>"));
        assertTrue(descriptor.contains("<phase>package</phase>"));
    }
}
