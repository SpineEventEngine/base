/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.js.gradle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.spine.code.js.DefaultJsProject;
import io.spine.code.js.Directory;
import io.spine.code.js.Module;
import io.spine.js.generate.resolve.ExternalModule;
import io.spine.js.generate.resolve.PackagePattern;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

/**
 * An extension for the {@link ProtoJsPlugin} which allows to obtain the {@code generateJsonParsers}
 * task to configure when it will be executed during the build lifecycle.
 */
@SuppressWarnings({"PublicField", "WeakerAccess"} /* Expose fields as a Gradle extension */)
public class Extension {

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
     * Names of JavaScript modules and Protobuf packages they provide.
     *
     * <p>Information about modules is used to resolve imports in generated Protobuf files.
     *
     * <p>Additionally to modules specified via the property,
     * the predefined Spine modules can be {@linkplain #resolveSpineModules used}.
     *
     * <p>An example of the definition:
     * <pre>{@code
     * modules = [
     *      // The `client` module provides Protobuf types from `company.client` package
     *      // (excluding nested packages).
     *      'client' : ['company.client'],
     *
     *      // The `server` module provides Protobuf types from `company.server` package
     *      // (including nested packages).
     *      'server' : ['company.server.*'],
     *
     *      // The `common/proto` module provides Protobuf types from `company` package.
     *      'common/proto' : ['company']
     * ]
     * }</pre>
     */
    public Map<String, List<String>> modules = newHashMap();
    /**
     * Determines whether to resolve {@linkplain #predefinedModules() predefined} Spine modules.
     *
     * <p>The option is enabled by default.
     */
    public boolean resolveSpineModules = true;
    private Task generateParsersTask;

    public static Directory getMainGenProto(Project project) {
        Extension extension = extension(project);
        String specifiedValue = extension.mainGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).proto()
                                              .mainJs());
        return Directory.at(path);
    }

    public static Directory getTestGenProtoDir(Project project) {
        Extension extension = extension(project);
        String specifiedValue = extension.testGenProtoDir;
        Path path = pathOrDefault(specifiedValue,
                                  def(project).proto()
                                              .testJs());
        return Directory.at(path);
    }

    public static File getMainDescriptorSet(Project project) {
        Path path = pathOrDefault(extension(project).mainDescriptorSetPath,
                                  def(project).mainDescriptors());
        return path.toFile();
    }

    public static File getTestDescriptorSet(Project project) {
        Path path = pathOrDefault(extension(project).testDescriptorSetPath,
                                  def(project).testDescriptors());
        return path.toFile();
    }

    public static List<ExternalModule> modules(Project project) {
        Extension extension = extension(project);
        Map<String, List<String>> rawModules = extension.modules;
        List<ExternalModule> modules = newArrayList();
        for (String moduleName : rawModules.keySet()) {
            List<PackagePattern> patterns = patterns(rawModules.get(moduleName));
            ExternalModule module = new ExternalModule(moduleName, patterns);
            modules.add(module);
        }
        if (extension.resolveSpineModules) {
            modules.addAll(predefinedModules());
        }
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
    static Extension extension(Project project) {
        return (Extension)
                project.getExtensions()
                       .getByName(ProtoJsPlugin.extensionName());
    }

    @VisibleForTesting
    static List<ExternalModule> predefinedModules() {
        return ImmutableList.of(
                spineWeb()
        );
    }

    @SuppressWarnings("DuplicateStringLiteralInspection" /* Used in a different context. */)
    private static ExternalModule spineWeb() {
        String name = Module.spineWeb.artifactName();
        List<PackagePattern> packages = ImmutableList.of(
                PackagePattern.of("client.parser"),
                PackagePattern.of("spine.base.*"),
                PackagePattern.of("spine.change.*"),
                PackagePattern.of("spine.client.*"),
                PackagePattern.of("spine.core.*"),
                PackagePattern.of("spine.net.*"),
                PackagePattern.of("spine.people.*"),
                PackagePattern.of("spine.time.*"),
                PackagePattern.of("spine.ui.*"),
                PackagePattern.of("spine.validate.*"),
                PackagePattern.of("spine.web.*"),
                PackagePattern.of("spine")
        );
        return new ExternalModule(name, packages);
    }

    private static List<PackagePattern> patterns(Collection<String> rawPatterns) {
        return rawPatterns.stream()
                          .map(PackagePattern::of)
                          .collect(toList());
    }

    private static Path pathOrDefault(String path, Object defaultValue) {
        String pathValue = isNullOrEmpty(path)
                           ? defaultValue.toString()
                           : path;
        return Paths.get(pathValue);
    }

    private static DefaultJsProject def(Project project) {
        return DefaultJsProject.at(project.getProjectDir());
    }
}
