package io.github.huahill.maven.jpackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

final class ProjectStager {

    void stage(Path mainArtifact, List<Path> runtimeArtifacts, Path inputDirectory) throws IOException {
        Files.createDirectories(inputDirectory);
        copy(mainArtifact, inputDirectory.resolve(mainArtifact.getFileName()));
        for (var artifact : runtimeArtifacts) {
            copy(artifact, inputDirectory.resolve(artifact.getFileName()));
        }
    }

    private void copy(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
