package org.spine3.tools.javadoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import org.junit.After;
import org.junit.Test;
import org.spine3.util.Exceptions;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spine3.tools.javadoc.RootDocProxyReceiver.rootDocFor;

public class ExcludeInternalDocletShould {

    private static final String TEST_SOURCES_PACKAGE = "testsources";
    private static final String INTERNAL_PACKAGE = TEST_SOURCES_PACKAGE + ".internal";
    private static final String INTERNAL_METHOD_CLASS_FILENAME = "InternalMethodClass.java";
    private static final String INTERNAL_CLASS_FILENAME = "InternalClass.java";
    private static final String DERIVED_FROM_INTERNAL_CLASS_FILENAME = "DerivedFromInternalClass.java";
    private static final String NOT_INTERNAL_CLASS_FILENAME = "/notinternal/NotInternalClass.java";

    @After
    public void tearDown() throws Exception {
        cleanUpGeneratedJavadocs();
    }

    @Test
    public void run_standard_doclet() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(NOT_INTERNAL_CLASS_FILENAME)
                .build();

        ExcludeInternalDoclet.main(args);

        assertTrue(Files.exists(Paths.get(JavadocArgsBuilder.getJavadocDir())));
    }

    @Test
    public void exclude_internal_annotated_annotations() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("InternalAnnotatedAnnotation.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses().length);
    }

    @Test
    public void exclude_internal_ctors() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("InternalCtorClass.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses()[0].constructors().length);
    }

    @Test
    public void exclude_internal_fields() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("InternalFieldClass.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses()[0].fields().length);
    }

    @Test
    public void exclude_internal_methods() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(INTERNAL_METHOD_CLASS_FILENAME)
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses()[0].methods().length);
    }

    @Test
    public void exclude_internal_package_content() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("/internal/InternalPackageClass.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses().length);
    }

    @Test
    public void exclude_only_from_internal_subpackages() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("/internal/subinternal/SubInternalPackageClass.java")
                .addSource(NOT_INTERNAL_CLASS_FILENAME)
                .addPackage(INTERNAL_PACKAGE)
                .addPackage(TEST_SOURCES_PACKAGE + ".notinternal")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(1, rootDoc.specifiedClasses().length);
    }

    @Test
    public void exclude_internal_classes() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(INTERNAL_CLASS_FILENAME)
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses().length);
    }

    @Test
    public void exclude_internal_interfaces() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("InternalAnnotatedInterface.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses().length);
    }

    @Test
    public void exclude_internal_enums() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("InternalEnum.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(0, rootDoc.specifiedClasses().length);
    }

    @Test
    public void not_exclude_elements_annotated_with_Internal_located_in_another_package() {
        final String[] args = new JavadocArgsBuilder()
                .addSource("GrpcInternalAnnotatedClass.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        assertEquals(1, rootDoc.specifiedClasses().length);
    }

    @Test
    public void correctly_work_when_compareTo_called() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(INTERNAL_CLASS_FILENAME)
                .addSource(DERIVED_FROM_INTERNAL_CLASS_FILENAME)
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        // invoke compareTo to be sure, that proxy unwrapping
        // doest not expose object passed to compareTo method
        final ClassDoc anotherClassDoc = rootDoc.specifiedClasses()[0].superclass();
        rootDoc.specifiedClasses()[0].compareTo(anotherClassDoc);

        assertEquals(1, rootDoc.specifiedClasses().length);
    }

    @Test
    public void correctly_work_when_overrides_called() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(INTERNAL_METHOD_CLASS_FILENAME)
                .addSource("OverridesInternalMethod.java")
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        // invoke overrides to be sure, that proxy unwrapping
        // doest not expose overridden method
        final ClassDoc overridesInternalMethod = rootDoc.classNamed(TEST_SOURCES_PACKAGE + ".OverridesInternalMethod");
        final MethodDoc overriddenMethod = overridesInternalMethod.methods()[0].overriddenMethod();
        overridesInternalMethod.methods()[0].overrides(overriddenMethod);

        assertEquals(0, rootDoc.classNamed(TEST_SOURCES_PACKAGE + ".InternalMethodClass")
                               .methods().length);
    }

    @Test
    public void correctly_work_when_subclassOf_invoked() {
        final String[] args = new JavadocArgsBuilder()
                .addSource(INTERNAL_CLASS_FILENAME)
                .addSource(DERIVED_FROM_INTERNAL_CLASS_FILENAME)
                .build();

        final RootDoc rootDoc = rootDocFor(args);

        // invoke subclassOf to be sure, that proxy unwrapping
        // doest not expose parent internal class
        final ClassDoc superclass = rootDoc.specifiedClasses()[0].superclass();
        rootDoc.specifiedClasses()[0].subclassOf(superclass);

        assertEquals(1, rootDoc.specifiedClasses().length);
    }

    @SuppressWarnings("ProhibitedExceptionCaught") // Need to catch NPE to fail test.
    @Test
    public void not_throw_NPE_processing_null_values() {
        final ExcludeInternalDoclet doclet = new NullProcessingTestDoclet();

        try {
            doclet.process(null, void.class);
        } catch (NullPointerException e) {
            fail("Should process null values without throwing NPE.");
        }
    }

    @SuppressWarnings("ConstantConditions") // Ok to not initialize ExcludePrinciple here.
    private static class NullProcessingTestDoclet extends ExcludeInternalDoclet {

        private NullProcessingTestDoclet() {
            super(null);
        }
    }

    private static void cleanUpGeneratedJavadocs() {
        Path rootPath = Paths.get(JavadocArgsBuilder.getJavadocDir());

        if (Files.exists(rootPath)) {
            try {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw Exceptions.wrappedCause(e);
            }
        }
    }
}
