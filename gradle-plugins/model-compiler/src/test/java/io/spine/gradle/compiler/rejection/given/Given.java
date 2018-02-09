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

package io.spine.gradle.compiler.rejection.given;

import com.sun.javadoc.RootDoc;
import io.spine.gradle.GradleProject;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;

import static io.spine.gradle.compiler.Extension.getDefaultMainGenSpineDir;
import static io.spine.tools.proto.FieldName.toCamelCase;

/**
 * @author Dmytro Grankin
 */
public class Given {

    /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String JAVADOC_LINE_SEPARATOR = "\n";
    private static final String JAVA_PACKAGE = "io.spine.sample.rejections";
    private static final String CLASS_COMMENT =
            "The rejection definition to test Javadoc generation.";
    private static final String REJECTION_NAME = "Rejection";
    private static final String FIRST_FIELD_COMMENT = "The rejection ID.";
    private static final String FIRST_FIELD_NAME = "id";
    private static final String SECOND_FIELD_COMMENT = "The rejection message.";
    private static final String SECOND_FIELD_NAME = "rejection_message";

    private Given() {
        // Prevent instantiation of this utility class.
    }

    public static GradleProject newProjectWithRejectionsJavadoc(TemporaryFolder projectFolder) {
        return GradleProject.newBuilder()
                            .setProjectName("rejections-javadoc")
                            .setProjectFolder(projectFolder)
                            .createProto("javadoc_rejections.proto", rejectionWithJavadoc())
                            .build();
    }

    public static String rejectionsJavadocSourceName() {
        final String packageAsDirectory = JAVA_PACKAGE.replace('.', '/');
        return getDefaultMainGenSpineDir() + '/' + packageAsDirectory + '/'
                + REJECTION_NAME + ".java";
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

    public static String getExpectedClassComment() {
        return ' ' + "<pre>" + JAVADOC_LINE_SEPARATOR
                + ' ' + CLASS_COMMENT + JAVADOC_LINE_SEPARATOR
                + " </pre>" + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                + " Rejection based on proto type {@code " + JAVA_PACKAGE + '.' + REJECTION_NAME
                + '}' + JAVADOC_LINE_SEPARATOR;
    }

    public static String getExpectedCtorComment() {
        final String param = " @param ";
        final String firstFieldJavaName = toCamelCase(FIRST_FIELD_NAME, false);
        final String secondFieldJavaName = toCamelCase(SECOND_FIELD_NAME, false);
        return " Creates a new instance." + JAVADOC_LINE_SEPARATOR + JAVADOC_LINE_SEPARATOR
                + param + firstFieldJavaName + "                " + FIRST_FIELD_COMMENT
                + JAVADOC_LINE_SEPARATOR
                + param + secondFieldJavaName + "  " + SECOND_FIELD_COMMENT
                + JAVADOC_LINE_SEPARATOR;
    }
}
