/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.protodoc;

import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;

import java.io.File;

import static io.spine.tools.protodoc.ProtoJavadocPlugin.PROTO_JAVADOC_EXTENSION_NAME;
import static java.lang.String.format;

/**
 * The extension for {@link ProtoJavadocPlugin}.
 */
@SuppressWarnings("unused") // Implicitly used during a Gradle build.
public class Extension implements Logging {

    /**
     * The path to the main Java sources directory, generated basing on Protobuf definitions.
     *
     * <p>The path is relative to a Gradle project.
     */
    private String mainGenProtoDir;

    /**
     * The path to the test Java sources directory, generated basing on Protobuf definitions.
     *
     * <p>The path is relative to a Gradle project.
     */
    private String testGenProtoDir;

    /**
     * Obtains absolute path to the {@link #mainGenProtoDir}.
     *
     * @param project the project to get the {@code mainGenProtoDir}
     * @return the absolute path to the main directory
     */
    static String getAbsoluteMainGenProtoDir(Project project) {
        String mainGenProtoDir = getExtension(project).mainGenProtoDir;
        checkExtensionField(mainGenProtoDir, "mainGenProtoDir");
        return rootPath(project) + File.separator + mainGenProtoDir;
    }

    /**
     * Obtains absolute path to the {@link #testGenProtoDir}.
     *
     * @param project the project to get the {@code testGenProtoDir}
     * @return the absolute path to the test directory
     */
    static String getAbsoluteTestGenProtoDir(Project project) {
        String testGenProtoDir = getExtension(project).testGenProtoDir;
        checkExtensionField(testGenProtoDir, "testGenProtoDir");
        return rootPath(project) + File.separator + testGenProtoDir;
    }

    void setMainGenProtoDir(String mainGenProtoDir) {
        this.mainGenProtoDir = mainGenProtoDir;
        _debug().log("Path to main generated Protobufs set up to `%s`.", mainGenProtoDir);
    }

    void setTestGenProtoDir(String testGenProtoDir) {
        this.testGenProtoDir = testGenProtoDir;
        _debug().log("Path to test generated Protobufs set up to `%s`.", testGenProtoDir);
    }

    private static void checkExtensionField(@Nullable String value, String name) {
        if (value == null) {
            String errMSg = format("%s.%s was not set.", PROTO_JAVADOC_EXTENSION_NAME, name);
            throw new IllegalStateException(errMSg);
        }
    }

    private static String rootPath(Project project) {
        return project.getProjectDir()
                      .getAbsolutePath();
    }

    private static Extension getExtension(Project project) {
        return (Extension) project.getExtensions()
                                  .getByName(PROTO_JAVADOC_EXTENSION_NAME);
    }
}
