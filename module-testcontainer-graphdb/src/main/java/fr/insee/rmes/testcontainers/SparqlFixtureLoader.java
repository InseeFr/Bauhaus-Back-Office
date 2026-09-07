package fr.insee.rmes.testcontainers;

/**
 * Backend-neutral seam for loading TriG fixtures into a SPARQL test container.
 *
 * <p>The fixtures themselves (standard RDF/TriG files) are already independent of the
 * triplestore; what is backend-specific is <em>how</em> they are pushed into the running
 * container. GraphDB uses the proprietary RDF4J REST endpoint
 * ({@code POST /repositories/{id}/statements}); Fuseki will use the standard Graph Store
 * Protocol ({@code POST /{ds}/data}). This interface hides that difference so the same
 * integration-test base and the same {@code *.trig} fixtures can drive either container.
 *
 * <p>Implemented today by {@link GraphDBContainer}; the future {@code FusekiContainer}
 * (lot D of the GraphDB→Fuseki migration) will implement it as well. When that module
 * lands, this interface is the natural thing to promote to a shared test-support module.
 */
public interface SparqlFixtureLoader {

    /**
     * Sets the classpath folder from which fixture files are resolved.
     *
     * @param folder classpath-relative folder holding the {@code .trig} fixtures
     * @return this loader, for chaining
     */
    SparqlFixtureLoader withInitFolder(String folder);

    /**
     * Loads a TriG fixture (resolved against the current init folder) into the container.
     *
     * @param file the fixture file name, relative to the init folder
     * @return this loader, for chaining
     */
    SparqlFixtureLoader withTrigFiles(String file);
}
