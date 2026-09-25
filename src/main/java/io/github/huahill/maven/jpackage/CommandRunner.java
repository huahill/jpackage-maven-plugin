package io.github.huahill.maven.jpackage;

import java.util.List;

interface CommandRunner {
    void run(List<String> command) throws Exception;
}
