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

package io.spine.tools.mc.dart.gradle;

import com.google.common.collect.ImmutableMap;
import io.spine.tools.fs.ExternalModules;
import io.spine.tools.gradle.SourceScope;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.spine.tools.gradle.BaseTaskName.assemble;
import static io.spine.tools.gradle.Projects.getDefaultMainDescriptors;
import static io.spine.tools.gradle.Projects.getDefaultTestDescriptors;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.ProtocPluginName.dart;
import static io.spine.tools.gradle.SourceScope.main;
import static io.spine.tools.gradle.SourceScope.test;
import static io.spine.tools.mc.dart.gradle.McDartTaskName.copyGeneratedDart;
import static io.spine.tools.mc.dart.gradle.McDartTaskName.copyTestGeneratedDart;
import static org.gradle.api.Task.TASK_TYPE;

/**
 * DSL extension for configuring Protobuf-to-Dart compilation.
 */
public final class McDartExtension {

    private static final String NAME = "protoDart";

    private static final String LIB_DIRECTORY = "lib";
    private static final String TEST_DIRECTORY = "test";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String GENERATED_BASE_DIR = "generated";

    private final Property<Object> mainDescriptorSetFile;
    private final Property<Object> testDescriptorSetFile;
    private final DirectoryProperty generatedDir;
    private final DirectoryProperty libDir;
    private final DirectoryProperty testDir;
    private final DirectoryProperty mainGeneratedDir;
    private final DirectoryProperty testGeneratedDir;

    /**
     * Names of Dart modules and directories they provide.
     *
     * <p>Information about modules is used to resolve imports in generated Protobuf files.
     *
     * <p>Import resolution only applies to Dart files generated from Protobuf. Such files must
     * have one of extensions: {@code .pb.dart}, {@code .pbenum.dart}, {@code .pbserver.dart}, or
     * {@code .pbjson.dart}. All other files are ignored.
     *
     * <p>An example of the definition:
     * <pre>{@code
     * modules = [
     *      // The module provides `company/client` directory (not including subdirectories).
     *      // So, an import path like {@code ../company/client/file.pb.dart}
     *      // becomes {@code package:client/company/client/file.pb.dart}.
     *      'client' : ['company/client'],
     *
     *      // The module provides `company/server` directory (including subdirectories).
     *      // So, an import path like {@code ../company/server/nested/file.pb.dart}
     *      // becomes {@code package:server/company/server/nested/file.pb.dart}.
     *      'server' : ['company/server/*'],
     *
     *      // The module provides 'proto/company` directory.
     *      // So, an import pah like {@code ../company/file.pbenum.dart}
     *      // becomes {@code package:common_types/proto/company/file.pbenum.dart}.
     *      'common_types' : ['proto/company']
     * ]
     * }</pre>
     */
    @SuppressWarnings({
            "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
            "UnrecognisedJavadocTag" /* ... `{@code }` within the code block example above. */
    })
    public final Map<String, List<String>> modules = new HashMap<>();

    private final Project project;

    McDartExtension(Project project) {
        super();
        this.project = project;
        ObjectFactory objects = project.getObjects();
        this.libDir = objects.directoryProperty();
        this.testDir = objects.directoryProperty();
        this.mainGeneratedDir = objects.directoryProperty();
        this.testGeneratedDir = objects.directoryProperty();
        this.mainDescriptorSetFile = objects.property(Object.class);
        this.testDescriptorSetFile = objects.property(Object.class);
        this.generatedDir = objects.directoryProperty();
        initProperties();
    }

    void createMainCopyTaskIn(Project project) {
        createCopyTask(project, main);
    }

    void createTestCopyTaskIn(Project project) {
        createCopyTask(project, test);
    }

    private void createCopyTask(Project project, SourceScope scope) {
        McDartTaskName taskName;
        DirectoryProperty targetDir;
        TaskName runAfter;
        if (scope == main) {
            taskName = copyGeneratedDart;
            targetDir = getLibDir();
            runAfter = generateProto;
        } else {
            taskName = copyTestGeneratedDart;
            targetDir = getTestDir();
            runAfter = generateTestProto;
        }
        Copy task = (Copy) project.task(ImmutableMap.of(TASK_TYPE, Copy.class), taskName.name());
        task.from(getGeneratedBaseDir().dir(scope.name() + File.separator + dart.name()));
        task.into(targetDir);
        task.dependsOn(runAfter.name());
        project.getTasks()
               .getByName(assemble.name())
               .dependsOn(taskName.name());
    }

    private void initProperties() {
        Directory projectDir = project.getLayout().getProjectDirectory();
        libDir.convention(projectDir.dir(LIB_DIRECTORY));
        testDir.convention(projectDir.dir(TEST_DIRECTORY));
        mainGeneratedDir.convention(libDir);
        testGeneratedDir.convention(testDir);
        mainDescriptorSetFile.convention(getDefaultMainDescriptors(project));
        testDescriptorSetFile.convention(getDefaultTestDescriptors(project));
        generatedDir.convention(projectDir.dir(GENERATED_BASE_DIR));
    }

    /**
     * Finds an extension of this type in the given project.
     */
    static McDartExtension findIn(Project project) {
        McDartExtension result =
                project.getExtensions()
                       .getByType(McDartExtension.class);
        return result;
    }

    /**
     * Registers this extension in the given project.
     */
    void register() {
        project.getExtensions()
               .add(McDartExtension.class, NAME, this);
    }

    /**
     * The descriptor set file for production Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/main.desc}.
     */
    public Property<Object> getMainDescriptorSetFile() {
        return mainDescriptorSetFile;
    }

    /**
     * Resolves the descriptor set file for production Protobuf types.
     */
    File mainDescriptorSetFile() {
        return file(getMainDescriptorSetFile());
    }

    /**
     * The descriptor set file for test Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/test.desc}.
     */
    public Property<Object> getTestDescriptorSetFile() {
        return testDescriptorSetFile;
    }

    /**
     * Resolves the descriptor set file for test Protobuf types.
     */
    File testDescriptorSetFile() {
        return file(getTestDescriptorSetFile());
    }

    /**
     * The base directory for code generated by {@code protoc}.
     *
     * <p>Defaults to {@code $projectDir/proto}.
     */
    public DirectoryProperty getGeneratedBaseDir() {
        return generatedDir;
    }

    /**
     * Resolves the base directory for code generated by {@code protoc}.
     */
    Path generatedDirPath() {
        return getGeneratedBaseDir().get()
                                    .getAsFile()
                                    .toPath();
    }

    /**
     * The directory which contains production Dart code.
     *
     * <p>Defaults to {@code $projectDir/lib}.
     */
    public DirectoryProperty getLibDir() {
        return libDir;
    }

    /**
     * The directory which contains test Dart code.
     *
     * <p>Defaults to {@code $projectDir/test}.
     */
    public DirectoryProperty getTestDir() {
        return testDir;
    }

    /**
     * The directory which contains the generated production Dart files.
     *
     * <p>Must be a subdirectory of {@link #getLibDir() libDir}.
     *
     * <p>Defaults to the {@code libDir}.
     */
    public DirectoryProperty getMainGeneratedDir() {
        return mainGeneratedDir;
    }

    /**
     * The directory which contains the generated test Dart files.
     *
     * <p>Must be a subdirectory of {@link #getTestDir() testDir}.
     *
     * <p>Defaults to the {@code testDir}.
     */
    @SuppressWarnings("unused") // For possible future use.
    public DirectoryProperty getTestGeneratedDir() {
        return testGeneratedDir;
    }

    ExternalModules modules() {
        return new ExternalModules(modules);
    }

    private File file(Property<Object> property) {
        return project.file(property.get());
    }
}
