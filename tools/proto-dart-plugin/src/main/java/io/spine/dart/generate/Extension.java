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

package io.spine.dart.generate;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.io.File;
import java.nio.file.Path;

/**
 * DSL extension for configuring Protobuf-to-Dart compilation.
 */
@SuppressWarnings("UnstableApiUsage") // Gradle `Property` API.
public final class Extension {

    private static final String NAME = "protoDart";

    private static final String MAIN_DESCRIPTOR = "descriptors/main.desc";
    private static final String TEST_DESCRIPTOR = "descriptors/test.desc";
    private static final String TYPE_REGISTRY = "lib/types.dart";
    private static final String TEST_TYPE_REGISTRY = "test/types.dart";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String GENERATED_BASE_DIR = "proto";

    private final RegularFileProperty mainDescriptorSet;
    private final RegularFileProperty testDescriptorSet;
    private final RegularFileProperty mainTypeRegistry;
    private final RegularFileProperty testTypeRegistry;
    private final DirectoryProperty generatedDir;
    private final Property<String> packageName;

    Extension(Project project) {
        ObjectFactory objects = project.getObjects();
        this.mainDescriptorSet = objects.fileProperty();
        mainDescriptorSet.convention(project.getLayout()
                                            .getBuildDirectory()
                                            .file(MAIN_DESCRIPTOR));
        this.testDescriptorSet = objects.fileProperty();
        testDescriptorSet.convention(project.getLayout()
                                            .getBuildDirectory()
                                            .file(TEST_DESCRIPTOR));
        this.mainTypeRegistry = objects.fileProperty();
        mainTypeRegistry.convention(project.getLayout()
                                           .getProjectDirectory()
                                           .file(TYPE_REGISTRY));
        this.testTypeRegistry = objects.fileProperty();
        testTypeRegistry.convention(project.getLayout()
                                           .getProjectDirectory()
                                           .file(TEST_TYPE_REGISTRY));
        this.generatedDir = objects.directoryProperty();
        generatedDir.convention(project.getLayout()
                                       .getProjectDirectory()
                                       .dir(GENERATED_BASE_DIR));
        this.packageName = objects.property(String.class);
        packageName.convention(project.getName());
    }

    /**
     * Finds an extension of this type in the given project.
     */
    static Extension findIn(Project project) {
        Extension extension = project.getExtensions()
                                     .getByType(Extension.class);
        return extension;
    }

    /**
     * Registers this extension in the given project.
     */
    void registerIn(Project project) {
        project.getExtensions()
               .add(Extension.class, NAME, this);
    }

    /**
     * The descriptor set file for production Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/main.desc}.
     */
    public RegularFileProperty getMainDescriptorSet() {
        return mainDescriptorSet;
    }

    /**
     * Resolves the descriptor set file for production Protobuf types.
     */
    File mainDescriptorSetFile() {
        return getMainDescriptorSet().get()
                                     .getAsFile();
    }

    /**
     * The descriptor set file for test Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/test.desc}.
     */
    public RegularFileProperty getTestDescriptorSet() {
        return testDescriptorSet;
    }

    /**
     * Resolves the descriptor set file for test Protobuf types.
     */
    File testDescriptorSetFile() {
        return getTestDescriptorSet().get()
                                     .getAsFile();
    }

    /**
     * The generated type registry file for production types.
     *
     * <p>Defaults to {@code $projectDir/lib/types.dart}.
     */
    public RegularFileProperty getMainTypeRegistry() {
        return mainTypeRegistry;
    }

    /**
     * Resolves the generated type registry file for production types.
     */
    File mainTypeRegistryFile() {
        return getMainTypeRegistry().get()
                                    .getAsFile();
    }

    /**
     * The generated type registry file for test types.
     *
     * <p>Defaults to {@code $projectDir/test/types.dart}.
     */
    public RegularFileProperty getTestTypeRegistry() {
        return testTypeRegistry;
    }

    /**
     * Resolves the generated type registry file for test types.
     */
    File testTypeRegistryFile() {
        return getTestTypeRegistry().get()
                                    .getAsFile();
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
     * The name of this Dart package.
     *
     * <p>By default, equal to the name of the project.
     */
    public Property<String> getPackageName() {
        return packageName;
    }

    /**
     * Resolves the name of this Dart package.
     */
    String packageName() {
        return getPackageName().get();
    }
}
