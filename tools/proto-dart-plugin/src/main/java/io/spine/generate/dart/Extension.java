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

package io.spine.generate.dart;

import io.spine.code.fs.dart.DefaultDartProject;
import io.spine.tools.gradle.GradleExtension;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.io.File;
import java.nio.file.Path;

/**
 * DSL extension for configuring Protobuf-to-Dart compilation.
 */
@SuppressWarnings("UnstableApiUsage") // Gradle `Property` API.
public final class Extension extends GradleExtension {

    private static final String NAME = "protoDart";

    private static final String LIB_DIRECTORY = "lib";
    private static final String TEST_DIRECTORY = "test";
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String GENERATED_BASE_DIR = "proto";

    private final Property<Object> mainDescriptorSet;
    private final Property<Object> testDescriptorSet;
    private final DirectoryProperty generatedDir;
    private final DirectoryProperty libDir;
    private final DirectoryProperty testDir;
    private final DirectoryProperty mainGeneratedDir;
    private final DirectoryProperty testGeneratedDir;
    private final Project project;

    Extension(Project project) {
        super();
        this.project = project;
        ObjectFactory objects = project.getObjects();
        this.libDir = objects.directoryProperty();
        this.testDir = objects.directoryProperty();
        this.mainGeneratedDir = objects.directoryProperty();
        this.testGeneratedDir = objects.directoryProperty();
        this.mainDescriptorSet = objects.property(Object.class);
        this.testDescriptorSet = objects.property(Object.class);
        this.generatedDir = objects.directoryProperty();
        initProperties();
    }

    private void initProperties() {
        libDir.convention(project.getLayout()
                                 .getProjectDirectory()
                                 .dir(LIB_DIRECTORY));
        testDir.convention(project.getLayout()
                                  .getProjectDirectory()
                                  .dir(TEST_DIRECTORY));
        mainGeneratedDir.convention(libDir);
        testGeneratedDir.convention(testDir);
        mainDescriptorSet.convention(defaultMainDescriptor(project));
        testDescriptorSet.convention(defaultTestDescriptor(project));
        generatedDir.convention(project.getLayout()
                                       .getProjectDirectory()
                                       .dir(GENERATED_BASE_DIR));
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
    void register() {
        project.getExtensions()
               .add(Extension.class, NAME, this);
    }

    /**
     * The descriptor set file for production Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/main.desc}.
     */
    public Property<Object> getMainDescriptorSet() {
        return mainDescriptorSet;
    }

    /**
     * Resolves the descriptor set file for production Protobuf types.
     */
    File mainDescriptorSetFile() {
        return file(getMainDescriptorSet());
    }

    /**
     * The descriptor set file for test Protobuf types.
     *
     * <p>Defaults to {@code $projectDir/build/descriptors/test.desc}.
     */
    public Property<Object> getTestDescriptorSet() {
        return testDescriptorSet;
    }

    /**
     * Resolves the descriptor set file for test Protobuf types.
     */
    File testDescriptorSetFile() {
        return file(getTestDescriptorSet());
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
     * Resolves the directory which contains production Dart code.
     */
    Path libDir() {
        return getLibDir().get()
                          .getAsFile()
                          .toPath();
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
     * Resolves the directory which contains test Dart code.
     */
    Path testDir() {
        return getTestDir().get()
                           .getAsFile()
                           .toPath();
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
     * Resolves the directory which contains the generated production Dart files.
     */
    Path mainGeneratedDir() {
        return getMainGeneratedDir().get()
                                    .getAsFile()
                                    .toPath();
    }

    /**
     * The directory which contains the generated test Dart files.
     *
     * <p>Must be a subdirectory of {@link #getTestDir() testDir}.
     *
     * <p>Defaults to the {@code testDir}.
     */
    public DirectoryProperty getTestGeneratedDir() {
        return testGeneratedDir;
    }

    /**
     * Resolves the directory which contains the generated test Dart files.
     */
    Path testGeneratedDir() {
        return getTestGeneratedDir().get()
                                    .getAsFile()
                                    .toPath();
    }

    /**
     * Finalizes all configurable values.
     *
     * <p>If a user tries to configure the extension after this method is called, the Gradle build
     * will fail with an error.
     */
    void finalizeAll() {
        this.libDir.finalizeValue();
        this.testDir.finalizeValue();
        this.mainGeneratedDir.finalizeValue();
        this.testGeneratedDir.finalizeValue();
        this.mainDescriptorSet.finalizeValue();
        this.testDescriptorSet.finalizeValue();
        this.generatedDir.finalizeValue();
    }

    private File file(Property<Object> property) {
        return project.file(property.get());
    }

    @Override
    protected DefaultDartProject defaultProject(Project project) {
        return DefaultDartProject.at(project.getProjectDir().toPath());
    }
}
