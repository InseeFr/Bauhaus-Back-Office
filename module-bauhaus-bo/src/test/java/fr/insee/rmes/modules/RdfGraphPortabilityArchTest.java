package fr.insee.rmes.modules;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Handle;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;

/**
 * Règles 2 et 3 de portabilité SPARQL (voir le {@code CLAUDE.md} du dépôt) : un appel RDF4J
 * natif nomme son graphe, et le graphe ne se relit jamais depuis un {@link Statement}. Les deux
 * ne fonctionnent que sur GraphDB, dont le graphe par défaut est l'union des graphes nommés.
 * <p>
 * Les deux règles sont gelées : les appels existants sont enregistrés dans {@code archunit_store}
 * et disparaissent au fil des corrections de {@code RepositoryGestion}, {@code RepositoryUtils},
 * {@code RepositoryPublication} et des classes {@code *Publication}. Aucun nouvel appel ne peut
 * s'ajouter.
 * <p>
 * L'import couvre aussi {@code module-utility}, présent sur le classpath du module.
 *
 * @see RdfGraphPortabilityRulesTest
 * @see ArchUnitStoreTest
 */
@AnalyzeClasses(packages = "fr.insee.rmes", importOptions = ImportOption.DoNotIncludeTests.class)
public class RdfGraphPortabilityArchTest {

    private static final String RESOURCE = Type.getInternalName(Resource.class);
    private static final String REPOSITORY_PACKAGE = "org/eclipse/rdf4j/repository/";
    private static final Set<String> TRIPLE_PATTERN_METHODS = Set.of("getStatements", "remove");
    private static final String TRIPLE_PATTERN_PARAMETERS =
            "(Lorg/eclipse/rdf4j/model/Resource;Lorg/eclipse/rdf4j/model/IRI;Lorg/eclipse/rdf4j/model/Value;";

    static final ArchRule NO_STATEMENT_GET_CONTEXT = noClasses()
            .should()
            .callMethodWhere(target(name("getContext")).and(target(owner(assignableTo(Statement.class)))))
            .as("no classes should read the graph of a Statement with getContext()")
            .because("getContext() returns null under SPARQLRepository: the graph must come from the caller");

    static final ArchRule NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH = classes()
            .should(nameTheirGraphWhenCallingRepositoryConnection())
            .because("without a context, RDF4J targets the default graph, which is the union of named graphs "
                    + "only on GraphDB: elsewhere a read returns nothing and a remove does nothing");

    @ArchTest
    public static final ArchRule statementGetContextIsForbidden = FreezingArchRule.freeze(NO_STATEMENT_GET_CONTEXT);

    @ArchTest
    public static final ArchRule nativeAccessWithoutNamedGraphIsForbidden =
            FreezingArchRule.freeze(NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH);

    private static ArchCondition<JavaClass> nameTheirGraphWhenCallingRepositoryConnection() {
        return new ArchCondition<>("name their graph when calling RepositoryConnection.getStatements or remove") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass
                        .getSource()
                        .ifPresent(source -> callsWithoutNamedGraph(source.getUri())
                                .forEach(call -> events.add(SimpleConditionEvent.violated(javaClass, call))));
            }
        };
    }

    /**
     * ArchUnit ne voit pas les arguments d'un appel : on lit le bytecode. Un varargs de contextes
     * vide est compilé en {@code iconst_0; anewarray Resource} juste avant l'appel.
     */
    private static List<String> callsWithoutNamedGraph(URI classFile) {
        try (InputStream bytecode = classFile.toURL().openStream()) {
            CallCollector collector = new CallCollector();
            new ClassReader(bytecode).accept(collector, ClassReader.SKIP_FRAMES);
            return collector.calls;
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire " + classFile, e);
        }
    }

    private static final class CallCollector extends ClassVisitor {

        private final List<String> calls = new ArrayList<>();
        private String className;
        private String sourceFile;

        private CallCollector() {
            super(SpringAsmInfo.ASM_VERSION);
        }

        @Override
        public void visit(
                int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = Type.getObjectType(name).getClassName();
        }

        @Override
        public void visitSource(String source, String debug) {
            sourceFile = source;
        }

        @Override
        public MethodVisitor visitMethod(
                int access, String name, String descriptor, String signature, String[] exceptions) {
            String caller = className + "." + name + "("
                    + Arrays.stream(Type.getArgumentTypes(descriptor))
                            .map(Type::getClassName)
                            .collect(Collectors.joining(", "))
                    + ")";
            return new MethodVisitor(SpringAsmInfo.ASM_VERSION) {

                private boolean zeroPushed;
                private boolean noContext;
                private int line;

                @Override
                public void visitLineNumber(int lineNumber, Label start) {
                    line = lineNumber;
                }

                @Override
                public void visitInsn(int opcode) {
                    reset();
                    zeroPushed = opcode == Opcodes.ICONST_0;
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    boolean emptyContexts = zeroPushed && opcode == Opcodes.ANEWARRAY && RESOURCE.equals(type);
                    reset();
                    noContext = emptyContexts;
                }

                @Override
                public void visitMethodInsn(
                        int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (noContext
                            && owner.startsWith(REPOSITORY_PACKAGE)
                            && TRIPLE_PATTERN_METHODS.contains(name)
                            && descriptor.startsWith(TRIPLE_PATTERN_PARAMETERS)) {
                        calls.add("Method <%s> calls %s.%s without a named graph in (%s:%d)"
                                .formatted(
                                        caller, owner.substring(owner.lastIndexOf('/') + 1), name, sourceFile, line));
                    }
                    reset();
                }

                @Override
                public void visitVarInsn(int opcode, int varIndex) {
                    reset();
                }

                @Override
                public void visitIntInsn(int opcode, int operand) {
                    reset();
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    reset();
                }

                @Override
                public void visitLdcInsn(Object value) {
                    reset();
                }

                @Override
                public void visitInvokeDynamicInsn(
                        String name,
                        String descriptor,
                        Handle bootstrapMethodHandle,
                        Object... bootstrapMethodArguments) {
                    reset();
                }

                private void reset() {
                    zeroPushed = false;
                    noContext = false;
                }
            };
        }
    }
}
