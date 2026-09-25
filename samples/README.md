# Samples

These projects show how a consuming desktop application can use the plugin.

Build and install the plugin first:

```bash
sdk env
./mvnw install
```

Then run a sample:

```bash
./mvnw -f samples/classpath-swing/pom.xml package
```

## Projects

| Project | Shape |
|---|---|
| `classpath-swing` | Non-modular Swing application using `mainJar` and `mainClass`. |
| `classpath-javafx` | Non-modular JavaFX application using `mainJar`, `mainClass`, JavaFX runtime dependencies, and `modules`. |
| `jpms-swing` | Modular Swing application using `module`. |

The three samples have been manually verified on macOS for `app-image` output.
