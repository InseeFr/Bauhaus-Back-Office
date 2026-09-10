# Bauhaus-Back-Office
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/3795/badge)](https://bestpractices.coreinfrastructure.org/projects/3795)

Rest Endpoints and services Integration used by [Bauhaus](https://github.com/InseeFr/Bauhaus)

The documentation can be found in the [docs](https://github.com/InseeFr/Bauhaus/tree/master/documentation) folder and [browsed online](https://inseefr.github.io/Bauhaus).

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
