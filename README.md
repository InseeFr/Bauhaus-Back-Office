# Bauhaus-Back-Office
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3795/badge)](https://bestpractices.coreinfrastructure.org/projects/3795)

Rest Endpoints and services Integration used by [Bauhaus](https://github.com/InseeFr/Bauhaus)

The documentation can be found in the [docs](https://github.com/InseeFr/Bauhaus/tree/master/documentation) folder and [browsed online](https://inseefr.github.io/Bauhaus).

## Running locally

The local development settings live in
`module-bauhaus-bo/config/bauhaus-local-dev.properties`. They are deliberately
kept **out of the packaged jar**: they allow every CORS origin, disable RDF
authentication and use MinIO's default credentials. `application.properties`
imports the file as `optional:file:./config/bauhaus-local-dev.properties`, i.e.
relative to the **working directory**, which must therefore be
`module-bauhaus-bo`.

Start the dependencies (GraphDB on 7200, MinIO on 9000), then the application.
The Docker stack is described once, in the front-end repository
([Bauhaus](https://github.com/InseeFr/Bauhaus), `docker-compose.yml`), cloned
next to this one:

```bash
./mvnw install -DskipTests       # sibling modules, once
docker compose -f ../Bauhaus/docker-compose.yml up -d graphdb minio minio-init
cd module-bauhaus-bo
../mvnw spring-boot:run          # working directory = module-bauhaus-bo
```

`minio-init` creates the `bauhaus` bucket and uploads a few test files, so that
documents 66, 593 and 1070 can be downloaded. The RDF repositories and their
test data are created by `e2e/playwright/db/init.sh` in the Bauhaus repository.

- **IntelliJ**: in the Spring Boot run configuration, set *Working directory* to
  `$MODULE_WORKING_DIR$` (or the `module-bauhaus-bo` folder).
- **From another directory**, or with a packaged jar, point Spring at the file
  explicitly:
  `java -jar module-bauhaus-bo-*.jar --spring.config.additional-location=file:/path/to/bauhaus-local-dev.properties`
- **Whole stack in Docker**: `pnpm e2e:stack` in the Bauhaus repository builds
  this Back-Office from the neighbouring clone and mounts this file into the
  container; it is not baked into the image.

Without this file (or an equivalent configuration) the application does not
start: the JWT issuer URI references `fr.insee.rmes.bauhaus.keycloak.server.url`.

## Code formatting

Java sources are formatted with [Spotless](https://github.com/diffplug/spotless)
and [palantir-java-format](https://github.com/palantir/palantir-java-format), an
opinionated formatter with no style options: 4-space indent, 120-column lines,
sorted imports.

```bash
./mvnw spotless:apply   # reformat every Java source
./mvnw spotless:check   # check only
```

`spotless:check` is bound to the `validate` phase, so any build — including the
`Test and Sonar Analysis` workflow, which runs `verify` — fails on an unformatted
file before compiling anything. Skip it with `-Dspotless.check.skip=true`.

Only Java sources are covered: `pom.xml`, the FreeMarker `.ftlh` templates and the
YAML workflows are left untouched. IntelliJ users can install the
[palantir-java-format plugin](https://plugins.jetbrains.com/plugin/13180) to have
the IDE reformat the same way.
