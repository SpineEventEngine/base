package io.spine.gradle.compiler.failure;

import com.sun.javadoc.RootDoc;
import io.spine.gradle.compiler.GradleProject;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;

import static io.spine.gradle.compiler.Extension.getDefaultMainGenSpineDir;
import static io.spine.gradle.compiler.util.JavaCode.toJavaFieldName;
import static io.spine.gradle.compiler.util.JavaSources.getJavaExtension;

/**
 * @author Dmytro Grankin
 */
class Given {

    static final String PROJECT_NAME = "failures-gen-plugin-test";

    /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String JAVADOC_LINE_SEPARATOR = "\n";
    static final String JAVA_PACKAGE = "io.spine.sample.failures";
    static final String CLASS_COMMENT =
            "The failure definition to test Javadoc generation.";
    static final String FAILURE_NAME = "Failure";
    static final String TEST_SOURCE = getDefaultMainGenSpineDir() + "/io/spine/sample/failures/"
            + FAILURE_NAME + getJavaExtension();
    static final String FAILURES_FILE_NAME = "javadoc_failures.proto";
    static final String FIRST_FIELD_COMMENT = "The failure ID.";
    static final String FIRST_FIELD_NAME = "id";
    static final String SECOND_FIELD_COMMENT = "The failure message.";
    static final String SECOND_FIELD_NAME = "message";

    private Given() {
        // Prevent instantiation of this utility class.
    }

    static GradleProject newProjectWithFailuresJavadoc(TemporaryFolder projectFolder) {
        return GradleProject.newBuilder()
                            .setProjectName(PROJECT_NAME)
                            .setProjectFolder(projectFolder)
                            .createProto(FAILURES_FILE_NAME, failureWithJavadoc())
                            .build();
    }

    private static Iterable<String> failureWithJavadoc() {
        return Arrays.asList(
                "syntax = \"proto3\";",
                "package spine.sample.failures;",
                "option java_package = \"" + JAVA_PACKAGE + "\";",
                "option java_multiple_files = false;",

                "//" + CLASS_COMMENT,
                "message " + FAILURE_NAME + " {",

                "//" + FIRST_FIELD_COMMENT,
                "int32 " + FIRST_FIELD_NAME + " = 1; // Is not a part of Javadoc.",

                "//" + SECOND_FIELD_COMMENT,
                "string " + SECOND_FIELD_NAME + " = 2;",

                "bool hasNoComment = 3;",
                "}"

        );
    }

    static String getExpectedClassComment() {
        return ' ' + "<pre>" + JAVADOC_LINE_SEPARATOR
                + ' ' + CLASS_COMMENT + JAVADOC_LINE_SEPARATOR
                + " </pre>" + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                + " Failure based on protobuf type {@code " + JAVA_PACKAGE + '.' + FAILURE_NAME
                + '}' + JAVADOC_LINE_SEPARATOR;
    }

    static String getExpectedCtorComment() {
        final String param = " @param ";
        final String firstFieldJavaName = toJavaFieldName(FIRST_FIELD_NAME, false);
        final String secondFieldJavaName = toJavaFieldName(SECOND_FIELD_NAME, false);
        return " Creates a new instance." + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                + param + firstFieldJavaName + "      " + FIRST_FIELD_COMMENT
                + JAVADOC_LINE_SEPARATOR
                + param + secondFieldJavaName + ' ' + SECOND_FIELD_COMMENT
                + JAVADOC_LINE_SEPARATOR;
    }
}
