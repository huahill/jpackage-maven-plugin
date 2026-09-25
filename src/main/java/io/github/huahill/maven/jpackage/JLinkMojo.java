package io.github.huahill.maven.jpackage;

import java.nio.file.Path;
import java.util.List;
import org.apache.maven.api.di.Inject;
import org.apache.maven.api.plugin.Log;
import org.apache.maven.api.plugin.MojoException;
import org.apache.maven.api.plugin.annotations.Mojo;
import org.apache.maven.api.plugin.annotations.Parameter;

@Mojo(name = "jlink", defaultPhase = "package")
public class JLinkMojo implements org.apache.maven.api.plugin.Mojo {

    @Inject
    private Log log;

    @Parameter(required = true)
    private List<Path> modulePath = List.of();

    @Parameter(required = true)
    private List<String> modules = List.of();

    @Parameter(required = true)
    private Path output;

    @Parameter(defaultValue = "false")
    private boolean noHeaderFiles;

    @Parameter(defaultValue = "false")
    private boolean noManPages;

    @Parameter(defaultValue = "false")
    private boolean stripDebug;

    @Parameter
    private String compress;

    @Parameter(defaultValue = "false")
    private boolean bindServices;

    @Parameter
    private List<String> extraOptions = List.of();

    @Parameter(defaultValue = "${java.home}")
    private Path javaHome;

    @Override
    public void execute() throws Exception {
        if (modulePath.isEmpty()) {
            throw new MojoException("Missing required jlink modulePath");
        }
        if (modules.isEmpty()) {
            throw new MojoException("Missing required jlink modules");
        }
        if (output == null) {
            throw new MojoException("Missing required jlink output");
        }
        var executor = new ToolExecutor(javaHome, new ProcessCommandRunner(log));
        executor.run(executor.jlinkCommand(modulePath, output, modules, noHeaderFiles, noManPages, stripDebug,
                compress, bindServices, extraOptions));
    }
}
