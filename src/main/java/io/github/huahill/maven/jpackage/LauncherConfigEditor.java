package io.github.huahill.maven.jpackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

final class LauncherConfigEditor {

    void rewriteForRuntime(Path configFile, String trainingProperty, String aotCacheName) throws IOException {
        var outputLine = "java-options=-XX:AOTCacheOutput=$APPDIR/" + aotCacheName;
        var runtimeLine = "java-options=-XX:AOTCache=$APPDIR/" + aotCacheName;
        var trainingLine = "java-options=-D" + trainingProperty + "=true";
        var foundOutput = false;
        var foundTraining = false;
        var rewritten = new ArrayList<String>();

        for (var line : Files.readAllLines(configFile)) {
            if (line.equals(outputLine)) {
                rewritten.add(runtimeLine);
                foundOutput = true;
            } else if (line.equals(trainingLine)) {
                foundTraining = true;
            } else {
                rewritten.add(line);
            }
        }

        if (!foundOutput) {
            throw new IllegalStateException("Missing Leyden AOT cache output option in " + configFile);
        }
        if (!foundTraining) {
            throw new IllegalStateException("Missing Leyden training property option in " + configFile);
        }

        Files.writeString(configFile, String.join(System.lineSeparator(), rewritten) + System.lineSeparator());
    }
}
