# Releasing

This project publishes release tags to Maven Central through GitHub Actions and
the Sonatype Central Publisher Portal.

Artifact page:
https://central.sonatype.com/artifact/io.github.huahill/jpackage-maven-plugin

## Versioning

- Use semantic versioning after the first public release.
- Use `0.x` while the configuration model can still change.
- Keep snapshots on the main development branch.
- Tag releases as `v<version>`, for example `v0.1.0`.

## Pre-release Checks

Run the plugin build:

```bash
sdk env
./mvnw verify
```

Run at least one host packaging smoke test before publishing:

```bash
./mvnw install
./mvnw -f samples/classpath-swing/pom.xml package
```

Before a stable release, verify the sample projects on the target operating
systems:

| Host | Expected package types |
|---|---|
| macOS | `app-image`, `dmg`, `pkg` |
| Linux | `app-image`, `deb`, `rpm` |
| Windows | `app-image`, `msi`, `exe` |

## GitHub Secrets

Configure these secrets on the `jpackage-maven-plugin` GitHub Environment
before pushing a release tag:

| Secret | Purpose |
|---|---|
| `CENTRAL_USERNAME` | Sonatype Central Portal user token username. |
| `CENTRAL_TOKEN` | Sonatype Central Portal user token password/token. |
| `GPG_PRIVATE_KEY` | ASCII-armored private key used to sign release artifacts. |
| `GPG_PASSPHRASE` | Passphrase for `GPG_PRIVATE_KEY`. |

## Publishing

Release publishing is triggered by tags matching `v*`. The workflow validates
that the tag and `project.version` match and refuses to publish `-SNAPSHOT`
versions.

Example release:

```bash
./mvnw versions:set -DnewVersion=0.1.0
./mvnw verify
git commit -am "release: prepare 0.1.0"
git tag v0.1.0
git push origin main v0.1.0
```

The publish job runs:

```bash
./mvnw -B -P release -DskipTests install \
  org.sonatype.central:central-publishing-maven-plugin:0.10.0:publish \
  -DautoPublish=true \
  -Dmaven.consumer.pom=false
```

The `release` Maven profile attaches sources, attaches Javadocs, signs
artifacts with GPG, uploads the deployment to the Central Portal, and auto
publishes after validation.

Maven 4 otherwise attaches a `consumer` classifier POM that Central cannot
map to Maven coordinates. The release profile and publish command disable
that companion artifact with `-Dmaven.consumer.pom=false`.

Before running the Maven publish command, the GitHub workflow temporarily
rewrites `pom.xml` to Maven model `4.0.0` so Sonatype Central can associate the
uploaded files with Maven coordinates. The committed source POM remains Maven
`4.1.0`.

After the publish job succeeds, two follow-up jobs run:

- Create a GitHub Release for the tag, or update it if it already exists.
- Check out `main`, bump the root project version to the next patch snapshot,
  commit it, and push it back to `main`. For example, publishing `v0.1.0`
  bumps `main` to `0.1.1-SNAPSHOT`.

GitHub Packages can be added later as a secondary distribution target for
snapshots or early access builds.

## Release Notes

GitHub Release notes are generated from commit subjects since the previous
`v*` tag, plus the Maven Central coordinates. Edit the GitHub Release after
it is created if you need a curated summary.

Release notes should include:

- Breaking configuration changes.
- New `jpackage`, `jlink`, or Leyden options.
- Host operating systems that were verified.
- Known limitations.
