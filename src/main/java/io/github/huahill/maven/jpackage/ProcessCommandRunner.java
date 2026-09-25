package io.github.huahill.maven.jpackage;

import java.util.List;
import org.apache.maven.api.plugin.Log;
import org.apache.maven.api.plugin.MojoException;

final class ProcessCommandRunner implements CommandRunner {

    private final Log log;

    ProcessCommandRunner(Log log) {
        this.log = log;
    }

    @Override
    public void run(List<String> command) throws Exception {
        log.info(String.join(" ", command));
        var process = new ProcessBuilder(command).inheritIO().start();
        try {
            var exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new MojoException("Command failed with exit code " + exitCode + ": "
                        + String.join(" ", command));
            }
        } finally {
            process.destroy();
        }
    }
}
