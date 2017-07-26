/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.gradle.compiler.rejection;

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

    static final String PROJECT_NAME = "rejections-gen-plugin-test";

    /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String JAVADOC_LINE_SEPARATOR = "\n";
    static final String JAVA_PACKAGE = "io.spine.sample.rejections";
    static final String CLASS_COMMENT =
            "The rejection definition to test Javadoc generation.";
    static final String REJECTION_NAME = "Rejection";
    static final String TEST_SOURCE = getDefaultMainGenSpineDir() + "/io/spine/sample/rejections/"
            + REJECTION_NAME + getJavaExtension();
    static final String REJECTIONS_FILE_NAME = "javadoc_rejections.proto";
    static final String FIRST_FIELD_COMMENT = "The rejection ID.";
    static final String FIRST_FIELD_NAME = "id";
    static final String SECOND_FIELD_COMMENT = "The rejection message.";
    static final String SECOND_FIELD_NAME = "message";

    private Given() {
        // Prevent instantiation of this utility class.
    }

    static GradleProject newProjectWithRejectionsJavadoc(TemporaryFolder projectFolder) {
        return GradleProject.newBuilder()
                            .setProjectName("rejections-javadoc")
                            .setProjectFolder(projectFolder)
                            .createProto(REJECTIONS_FILE_NAME, rejectionWithJavadoc())
                            .build();
    }

    private static Iterable<String> rejectionWithJavadoc() {
        return Arrays.asList(
                "syntax = \"proto3\";",
                "package spine.sample.rejections;",
                "option java_package = \"" + JAVA_PACKAGE + "\";",
                "option java_multiple_files = false;",

                "//" + CLASS_COMMENT,
                "message " + REJECTION_NAME + " {",

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
                + " Rejection based on proto type {@code " + JAVA_PACKAGE + '.' + REJECTION_NAME
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
