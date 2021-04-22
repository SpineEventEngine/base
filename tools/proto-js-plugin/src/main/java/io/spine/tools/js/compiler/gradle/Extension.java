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

package io.spine.tools.js.compiler.gradle;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.code.fs.DefaultPaths;
import io.spine.tools.js.fs.DefaultJsPaths;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.code.structure.DirectoryPattern;
import io.spine.tools.code.structure.ExternalModule;
import io.spine.tools.gradle.GradleExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.tools.js.compiler.gradle.ProtoJsPlugin.extensionName;
import static io.spine.tools.code.structure.ExternalModule.predefinedModules;
import static java.util.stream.Collectors.toList;

/**
 * An extension for the {@link ProtoJsPlugin} which allows to obtain
 * the {@link #generateParsersTask()} task to configure when it will be executed
 * during the build lifecycle.
 */
@SuppressWarnings("PublicField" /* Expose fields as a Gradle extension */)
public class Extension extends GradleExtension {

    /**
     * The absolute path to the main Protobuf descriptor set file.
     */
    public String mainDescriptorSetPath;

    /**
     * The absolute path to the test Protobuf descriptor set file.
     */
    public String testDescriptorSetPath;

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
    public Map<String, List<String>> modules = new HashMap<>();

    private Task generateParsersTask;

    protected Extension(Project project, String name) {
        super(project, name);
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    protected DefaultJsPaths defaultPaths() {
        return (DefaultJsPaths) super.defaultPaths();
    }

    public Directory getMainGenProto() {
        String specifiedValue = mainGenProtoDir;
        Path path = pathOrDefault(specifiedValue, defaultPaths().generated() .mainJs());
        return Directory.at(path);
    }

    public Directory getTestGenProtoDir() {
        Path path = pathOrDefault(testGenProtoDir, defaultPaths().generated().testJs());
        return Directory.at(path);
    }

    public File mainDescriptorSet() {
        Path path = pathOrDefault(mainDescriptorSetPath, defaultMainDescriptor());
        return path.toFile();
    }

    public File getTestDescriptorSet() {
        Path path = pathOrDefault(testDescriptorSetPath, defaultTestDescriptor());
        return path.toFile();
    }

    public static List<ExternalModule> modules(Project project) {
        Extension extension = of(project);
        Map<String, List<String>> rawModules = extension.modules;
        List<ExternalModule> modules = new ArrayList<>();
        for (String moduleName : rawModules.keySet()) {
            List<DirectoryPattern> patterns = patterns(rawModules.get(moduleName));
            ExternalModule module = new ExternalModule(moduleName, patterns);
            modules.add(module);
        }
        modules.addAll(predefinedModules());
        return modules;
    }

    /**
     * Returns the {@code generateJsonParsers} task configured by the {@link ProtoJsPlugin}.
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

    @VisibleForTesting
    static Extension of(Project project) {
        Extension extension = (Extension)
                project.getExtensions()
                       .getByName(extensionName());
        return extension;
    }

    private static List<DirectoryPattern> patterns(Collection<String> rawPatterns) {
        return rawPatterns.stream()
                          .map(DirectoryPattern::of)
                          .collect(toList());
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

    @Override
    protected DefaultPaths defaultPathsIn(Project project) {
        return def(project);
    }
}
