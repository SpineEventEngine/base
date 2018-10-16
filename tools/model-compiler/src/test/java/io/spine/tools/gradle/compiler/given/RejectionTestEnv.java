/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler.given;

import com.sun.javadoc.RootDoc;
import io.spine.code.java.DefaultJavaProject;
import io.spine.code.java.FileName;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FieldName;
import io.spine.tools.gradle.GradleProject;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.String.format;

public class RejectionTestEnv {

    /** Javadocs received from {@link RootDoc} contain "\n" line separator. */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String JAVADOC_LINE_SEPARATOR = "\n";
    private static final String CLASS_COMMENT =
            "The rejection definition to test Javadoc generation.";
    private static final String REJECTION_NAME = "Rejection";
    private static final String FIRST_FIELD_COMMENT = "The rejection ID.";
    private static final FieldName FIRST_FIELD = FieldName.of("id");
    private static final String SECOND_FIELD_COMMENT = "The rejection message.";
    private static final FieldName SECOND_FIELD = FieldName.of("rejection_message");

    private static final PackageName JAVA_PACKAGE = PackageName.of("io.spine.sample.rejections");
    private static final FileName REJECTION_FILE_NAME = FileName.forType(REJECTION_NAME);

    /** Prevents instantiation of this utility class. */
    private RejectionTestEnv() {
    }

    public static GradleProject newProjectWithRejectionsJavadoc(TemporaryFolder projectFolder) {
        return GradleProject.newBuilder()
                            .setProjectName("rejections-javadoc")
                            .setProjectFolder(projectFolder.getRoot())
                            .createProto("javadoc_rejections.proto", rejectionWithJavadoc())
                            .enableDebug()
                            .build();
    }

    public static String rejectionsJavadocThrowableSource() {
        Path fileName = DefaultJavaProject.at(Paths.get("/"))
                                          .generated()
                                          .mainSpine()
                                          .resolve(JAVA_PACKAGE.toDirectory())
                                          .resolve(REJECTION_FILE_NAME.value());
        return fileName.toString();
    }

    public static String rejectionsJavadocProtoSource() {
        FileName fileName = FileName.forType("JavadocRejections");
        Path filePath = DefaultJavaProject.at(Paths.get("/"))
                                          .generated()
                                          .mainJava()
                                          .resolve(JAVA_PACKAGE.toDirectory())
                                          .resolve(fileName.value());
        return filePath.toString();
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
                    "int32 " + FIRST_FIELD + " = 1; // Is not a part of Javadoc.",

                    "//" + SECOND_FIELD_COMMENT,
                    "string " + SECOND_FIELD + " = 2;",

                    "bool hasNoComment = 3;",
                "}"
        );
    }

    public static String getExpectedClassComment() {
        return expectedWrappedInPreTag(CLASS_COMMENT) + JAVADOC_LINE_SEPARATOR
                + " Rejection based on proto type {@code " + JAVA_PACKAGE + '.' + REJECTION_NAME
                + '}' + JAVADOC_LINE_SEPARATOR;
    }

    public static String getExpectedBuilderClassComment() {
        return format(" The builder for the {@code %s} rejection. ", REJECTION_NAME);
    }

    public static String getExpectedFirstFieldComment() {
        return expectedWrappedInPreTag(FIRST_FIELD_COMMENT);

    }

    public static String getExpectedSecondFieldComment() {
        return expectedWrappedInPreTag(SECOND_FIELD_COMMENT);
    }

    private static String expectedWrappedInPreTag(String commentText) {
        return " <pre>" + JAVADOC_LINE_SEPARATOR
                + ' ' + commentText + JAVADOC_LINE_SEPARATOR
                + " </pre>" + JAVADOC_LINE_SEPARATOR;
    }
}
