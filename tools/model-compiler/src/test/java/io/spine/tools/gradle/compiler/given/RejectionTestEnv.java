/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.code.fs.java.DefaultJavaProject;
import io.spine.code.fs.java.Directory;
import io.spine.code.fs.java.FileName;
import io.spine.code.java.PackageName;
import io.spine.code.proto.FieldName;
import io.spine.tools.gradle.testing.GradleProject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.String.format;

public class RejectionTestEnv {

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

    public static GradleProject newProjectWithRejectionsJavadoc(File projectFolder) {
        return GradleProject.newBuilder()
                            .setProjectName("rejections-javadoc")
                            .setProjectFolder(projectFolder)
                            .createProto("javadoc_rejections.proto", rejectionWithJavadoc())
                            .build();
    }

    public static String rejectionsJavadocThrowableSource() {
        Path fileName = DefaultJavaProject.at(Paths.get("/"))
                                          .generated()
                                          .mainSpine()
                                          .resolve(Directory.of(JAVA_PACKAGE))
                                          .resolve(REJECTION_FILE_NAME.value());
        return fileName.toString();
    }

    private static Iterable<String> rejectionWithJavadoc() {
        return Arrays.asList(
                "syntax = \"proto3\";",
                "package spine.sample.rejections;",
                "option java_package = \"" + JAVA_PACKAGE + "\";",
                "option java_multiple_files = false;",

                "// " + CLASS_COMMENT,
                "message " + REJECTION_NAME + " {",

                    "// " + FIRST_FIELD_COMMENT,
                    "int32 " + FIRST_FIELD + " = 1; // Is not a part of Javadoc.",

                    "// " + SECOND_FIELD_COMMENT,
                    "string " + SECOND_FIELD + " = 2;",

                    "bool hasNoComment = 3;",
                "}"
        );
    }

    public static String getExpectedClassComment() {
        return wrappedInPreTag(CLASS_COMMENT)
                + " Rejection based on proto type  " +
                "{@code " + JAVA_PACKAGE + '.' + REJECTION_NAME+ '}';
    }

    public static String getExpectedBuilderClassComment() {
        return format("The builder for the  {@code %s}  rejection.", REJECTION_NAME);
    }

    public static String getExpectedFirstFieldComment() {
        return wrappedInPreTag(FIRST_FIELD_COMMENT);

    }

    public static String getExpectedSecondFieldComment() {
        return wrappedInPreTag(SECOND_FIELD_COMMENT);
    }

    private static String wrappedInPreTag(String commentText) {
        return "<pre> " + commentText + " </pre>";
    }
}
