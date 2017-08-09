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

package io.spine.tools.protodoc;

import org.gradle.api.Project;

import java.io.File;

import static io.spine.tools.protodoc.ProtoJavadocPlugin.PROTO_JAVADOC_EXTENSION_NAME;

/**
 * The extension for {@link ProtoJavadocPlugin}.
 *
 * @author Dmytro Grankin
 */
public class Extension {

    /**
     * The path to the main Java sources directory, generated basing on Protobuf definitions.
     *
     * <p>The path is relative to a Gradle project.
     */
    public String mainGenProtoDir;

    /**
     * The path to the test Java sources directory, generated basing on Protobuf definitions.
     *
     * <p>The path is relative to a Gradle project.
     */
    public String testGenProtoDir;

    /**
     * Obtains absolute path to the {@link #mainGenProtoDir}.
     *
     * @param project the project to get the {@code mainGenProtoDir}
     * @return the absolute path to the main directory
     */
    public static String getAbsoluteMainGenProtoDir(Project project) {
        return rootPath(project) + File.separator + getExtension(project).mainGenProtoDir;
    }

    /**
     * Obtains absolute path to the {@link #testGenProtoDir}.
     *
     * @param project the project to get the {@code testGenProtoDir}
     * @return the absolute path to the test directory
     */
    public static String getAbsoluteTestGenProtoDir(Project project) {
        return rootPath(project) + File.separator + getExtension(project).testGenProtoDir;
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
