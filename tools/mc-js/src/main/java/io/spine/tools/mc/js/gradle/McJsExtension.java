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

package io.spine.tools.mc.js.gradle;

import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.js.fs.DefaultJsPaths;
import io.spine.tools.js.fs.Directory;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static io.spine.tools.fs.ExternalModule.predefinedModules;
import static io.spine.tools.gradle.Projects.getDefaultMainDescriptors;
import static io.spine.tools.gradle.Projects.getDefaultTestDescriptors;
import static io.spine.tools.mc.js.gradle.McJsPlugin.extensionName;

/**
 * An extension for the {@link McJsPlugin} which allows to obtain the {@code generateJsonParsers}
 * task to configure when it will be executed during the build lifecycle.
 */
@SuppressWarnings("PublicField" /* Expose fields as a Gradle extension */)
public class McJsExtension {

    /**
     * The name of the extension as it appears in a Gradle script.
     */
    static final String NAME = "protoJs";

    /**
     * The absolute path to the main Protobuf descriptor set file.
     */
    public String mainDescriptorSetFile;

    /**
     * The absolute path to the test Protobuf descriptor set file.
     */
    public String testDescriptorSetFile;

    /**
     * The absolute path to the main Protobufs compiled to JavaScript.
     */
    public String mainGenProtoDir;

    /**
     * The absolute path to the test Protobufs compiled to JavaScript.
     */
    public String testGenProtoDir;

    /**
     * Names of JavaScript modules and directories they provide.
     *
     * <p>Information about modules is used to resolve imports in generated Protobuf files.
     *
     * <p>Additionally to modules specified via the property,
     * the {@linkplain ExternalModule#predefinedModules() predefined Spine} modules are used.
     *
     * <p>An example of the definition:
     * <pre>{@code
     * modules = [
     *      // The module provides `company/client` directory (not including subdirectories).
     *      // So, an import path like {@code ../company/client/file.js}
     *      // becomes {@code client/company/client/file.js}.
     *      'client' : ['company/client'],
     *
     *      // The module provides `company/server` directory (including subdirectories).
     *      // So, an import path like {@code ../company/server/nested/file.js}
     *      // becomes {@code server/company/server/nested/file.js}.
     *      'server' : ['company/server/*'],
     *
     *      // The module provides 'proto/company` directory.
     *      // So, an import pah like {@code ../company/file.js}
     *      // becomes {@code common-types/proto/company/file.js}.
     *      'common-types' : ['proto/company']
     * ]
     * }</pre>
     */
    @SuppressWarnings(
            "UnrecognisedJavadocTag" /* ... `{@code }` within the code block example above. */
    )
    public Map<String, List<String>> modules = newHashMap();

    private Task generateParsersTask;

    /**
     * Creates the extension in the given project.
     */
    static McJsExtension createIn(Project project) {
        McJsExtension extension =
                project.getExtensions()
                        .create(NAME, McJsExtension.class);
        return extension;
    }

    public static Directory getMainGenProto(Project project) {
        McJsExtension extension = extension(project);
        String specifiedValue = extension.mainGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).generated()
                                              .mainJs());
        return Directory.at(path);
    }

    public static Directory getTestGenProtoDir(Project project) {
        McJsExtension extension = extension(project);
        String specifiedValue = extension.testGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).generated()
                                              .testJs());
        return Directory.at(path);
    }

    public static File getMainDescriptorSet(Project project) {
        McJsExtension extension = extension(project);
        File result = getDefaultMainDescriptors(project);
        Path path = pathOrDefault(extension.mainDescriptorSetFile,
                                  result);
        return path.toFile();
    }

    public static File getTestDescriptorSet(Project project) {
        McJsExtension extension = extension(project);
        File result = getDefaultTestDescriptors(project);
        Path path = pathOrDefault(extension.testDescriptorSetFile,
                                  result);
        return path.toFile();
    }

    ExternalModules modules() {
        ExternalModules combined =
                new ExternalModules(modules)
                        .with(predefinedModules());
        return combined;
    }

    /**
     * Returns the {@code generateJsonParsers} task configured by the {@link McJsPlugin}.
     */
    @SuppressWarnings("unused") // Used in project applying the plugin.
    public Task generateParsersTask() {
        checkState(generateParsersTask != null,
                   "The 'generateJsonParsers' task was not configured by the ProtoJS plugin");
        return generateParsersTask;
    }

    /**
     * Makes the extension read-only for all plugin users.
     */
    void setGenerateParsersTask(Task generateParsersTask) {
        this.generateParsersTask = generateParsersTask;
    }

    static McJsExtension extension(Project project) {
        return (McJsExtension)
                project.getExtensions()
                       .getByName(extensionName());
    }

    private static Path pathOrDefault(String path, Object defaultValue) {
        String pathValue = isNullOrEmpty(path)
                           ? defaultValue.toString()
                           : path;
        return Paths.get(pathValue);
    }

    private static DefaultJsPaths def(Project project) {
        return DefaultJsPaths.at(project.getProjectDir());
    }

}
