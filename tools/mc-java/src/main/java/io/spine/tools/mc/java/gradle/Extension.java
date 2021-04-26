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
package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import groovy.lang.Closure;
import io.spine.tools.code.Indent;
import io.spine.tools.code.fs.TempArtifacts;
import io.spine.tools.gradle.GradleExtension;
import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.java.fs.Generated;
import io.spine.tools.java.protoc.NestedClasses;
import io.spine.tools.mc.java.gradle.annotate.Annotations;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A configuration for the {@link ModelCompilerPlugin}.
 *
 * @apiNote Even though this class is not planned for inheritance it cannot be made final because of
 * the <a href="https://docs.gradle.org/current/userguide/custom_plugins.html#sec:getting_input_from_the_build">
 * Gradle conventions</a> for project extensions.
 */
@SuppressWarnings({
        "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
        "ClassWithTooManyMethods" /* The methods are needed for handing default values. */,
        "ClassWithTooManyFields", "PMD.TooManyFields" /* Gradle extensions are flat like this. */})
public class Extension extends GradleExtension {

    /**
     * The absolute path to the Protobuf source code under the {@code main} directory.
     */
    public String mainProtoDir;

    /**
     * The absolute path to the test Protobuf source directory.
     */
    public String testProtoDir;

    /**
     * The absolute path to the main Protobuf descriptor set file.
     *
     * <p>The file must have the {@code .desc} extension.
     */
    public String mainDescriptorSetFile;

    /**
     * The absolute path to the test Protobuf descriptor set file.
     *
     * <p>The file must have the {@code .desc} extension.
     */
    public String testDescriptorSetFile;

    /**
     * The absolute path to the main Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedMainJavaDir;

    /**
     * The absolute path to the main {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedMainGrpcJavaDir;

    /**
     * The absolute path to the main target generated resources directory.
     */
    public String generatedMainResourcesDir;

    /**
     * The absolute path to the test target generated resources directory.
     */
    public String generatedTestResourcesDir;

    /**
     * The absolute path to the test Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestJavaDir;

    /**
     * The absolute path to the test {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestGrpcJavaDir;

    /**
     * The absolute path to the main target generated rejections root directory.
     */
    public String generatedMainRejectionsDir;

    /**
     * The absolute path to the test target generated rejections root directory.
     */
    public String generatedTestRejectionsDir;

    /**
     * The absolute path to directory to delete.
     *
     * <p>Either this property OR the {@link #dirsToClean} property is used.
     */
    public String dirToClean;

    /**
     * The indent for the generated code in the validating builders.
     */
    public Indent indent = Indent.of4();

    /**
     * The absolute paths to directories to delete.
     *
     * <p>Either this property OR the {@link #dirToClean} property is used.
     */
    public List<String> dirsToClean = new ArrayList<>();

    public final Annotations generateAnnotations = new Annotations();

    public final Interfaces interfaces = new Interfaces();

    public final Methods methods = new Methods();

    public final NestedClasses nestedClasses = new NestedClasses();

    public final Fields fields = new Fields();

    public final EntityQueries entityQueries = new EntityQueries();

    public boolean generateValidatingBuilders = true;

    public boolean generateValidation = true;

    public List<String> internalClassPatterns = new ArrayList<>();

    public List<String> internalMethodNames = new ArrayList<>();

    Extension(Project project, String name) {
        super(project, name);
    }

    /**
     * Obtains the instance of the {@linkplain ModelCompilerPlugin#extensionName() extension} in
     * the passed project.
     *
     * @throws org.gradle.api.UnknownDomainObjectException
     *         if the project does not have this extension
     */
    public static Extension of(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        String extensionName = ModelCompilerPlugin.extensionName();
        Object found = extensions.getByName(extensionName);
        Extension result = (Extension) found;
        return result;
    }

    @Override
    protected final DefaultJavaPaths defaultPathsIn(Project project) {
        return DefaultJavaPaths.at(project.getProjectDir());
    }

    @Override
    protected final DefaultJavaPaths defaultPaths() {
        return (DefaultJavaPaths) super.defaultPaths();
    }

    @SuppressWarnings("FloggerSplitLogStatement")
    public String mainProtoSrcDir() {
        _debug().log("Extension is `%s`.", this);
        String protoDir = mainProtoDir;
        _debug().log("`modelCompiler.mainProtoDir` is `%s`.", protoDir);
        return pathOrDefault(protoDir, defaultPaths().src().mainProto());
    }

    public String generatedMainResourcesDir() {
        return pathOrDefault(generatedMainResourcesDir, generated().mainResources());
    }

    public String generatedMainGrpcJavaDir() {
        return pathOrDefault(generatedMainGrpcJavaDir, generated().mainGrpc());
    }

    public String generatedMainJavaDir() {
        return pathOrDefault(generatedMainJavaDir, generated().mainJava());
    }

    public String generatedTestResourcesDir() {
        return pathOrDefault(generatedTestResourcesDir, generated().testResources());
    }

    public String testProtoDir() {
        return pathOrDefault(testProtoDir, defaultPaths().src().testProto());
    }

    public String generatedTestGrpcJavaDir() {
        return pathOrDefault(generatedTestGrpcJavaDir, generated().testGrpc());
    }

    public String generatedTestJavaDir() {
        return pathOrDefault(generatedTestJavaDir, generated().testJava());
    }

    public File mainDescriptorSetFile() {
        String path = pathOrDefault(mainDescriptorSetFile, defaultMainDescriptor());
        return new File(path);
    }

    public File testDescriptorSetFile() {
        String path = pathOrDefault(testDescriptorSetFile, defaultTestDescriptor());
        return new File(path);
    }

    public String generatedMainRejectionsDir() {
        return pathOrDefault(generatedMainRejectionsDir, generated().mainSpine());
    }

    public String generatedTestRejectionsDir() {
        return pathOrDefault(generatedTestRejectionsDir, generated().testSpine());
    }

    private Generated generated() {
        return defaultPaths().generated();
    }

    private static String pathOrDefault(String path, Object defaultValue) {
        return isNullOrEmpty(path)
               ? defaultValue.toString()
               : path;
    }

    public Indent indent() {
        Indent result = indent;
        _debug().log("The current indent is %d.", result.getSize());
        return result;
    }

    public static List<String> dirsToCleanIn(Project project) {
        List<String> dirsToClean = newLinkedList(spineDirs(project));
        _debug().log("Finding the directories to clean.");
        Extension extension = of(project);
        List<String> dirs = extension.dirsToClean;
        String singleDir = extension.dirToClean;
        if (dirs.size() > 0) {
            _info().log("Found %d directories to clean: `%s`.", dirs.size(), dirs);
            dirsToClean.addAll(dirs);
        } else if (singleDir != null && !singleDir.isEmpty()) {
            _debug().log("Found directory to clean: `%s`.", singleDir);
            dirsToClean.add(singleDir);
        } else {
            String defaultValue = extension.generated().toString();
            _debug().log("Default directory to clean: `%s`.", defaultValue);
            dirsToClean.add(defaultValue);
        }
        return ImmutableList.copyOf(dirsToClean);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Closure<?> closure) {
        ConfigureUtil.configure(closure, generateAnnotations);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Action<? super Annotations> action) {
        action.execute(generateAnnotations);
    }

    @SuppressWarnings("unused") // Configures `interfaces` closure.
    public void interfaces(Closure<?> closure) {
        ConfigureUtil.configure(closure, interfaces);
    }

    @SuppressWarnings("unused") // Configures `interfaces` closure.
    public void interfaces(Action<? super Interfaces> action) {
        action.execute(interfaces);
    }

    @SuppressWarnings("unused") // Configures `methods` closure.
    public void methods(Closure<?> closure) {
        ConfigureUtil.configure(closure, methods);
    }

    @SuppressWarnings("unused") // Configures `methods` closure.
    public void methods(Action<? super Methods> action) {
        action.execute(methods);
    }

    @SuppressWarnings("unused") // Configures `nestedClasses` closure.
    public void nestedClasses(Closure<?> closure) {
        ConfigureUtil.configure(closure, nestedClasses);
    }

    @SuppressWarnings("unused") // Configures `nestedClasses` closure.
    public void nestedClasses(Action<? super NestedClasses> action) {
        action.execute(nestedClasses);
    }

    @SuppressWarnings("unused") // Configures `fields` closure.
    public void fields(Closure<?> closure) {
        ConfigureUtil.configure(closure, fields);
    }

    @SuppressWarnings("unused") // Configures `fields` closure.
    public void fields(Action<? super Fields> action) {
        action.execute(fields);
    }

    @SuppressWarnings("unused") // Configures `entityQueries` closure.
    public void entityQueries(Closure<?> closure) {
        ConfigureUtil.configure(closure, entityQueries);
    }

    @SuppressWarnings("unused") // Configures `entityQueries` closure.
    public void entityQueries(Action<? super EntityQueries> action) {
        action.execute(entityQueries);
    }

    public ImmutableSet<String> internalClassPatterns() {
        return ImmutableSet.copyOf(internalClassPatterns);
    }

    public ImmutableSet<String> internalMethodNames() {
        return ImmutableSet.copyOf(internalMethodNames);
    }

    private static Iterable<String> spineDirs(Project project) {
        List<String> spineDirs = newLinkedList();
        Optional<String> spineDir = spineDir(project);
        Optional<String> rootSpineDir = spineDir(project.getRootProject());
        if (spineDir.isPresent()) {
            spineDirs.add(spineDir.get());
            if (rootSpineDir.isPresent() && !spineDir.equals(rootSpineDir)) {
                spineDirs.add(rootSpineDir.get());
            }
        }
        return spineDirs;
    }

    public static Optional<String> spineDir(Project project) {
        File projectDir;
        try {
            projectDir = project.getProjectDir()
                                .getCanonicalFile();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to obtain project directory `%s`.", project.getProjectDir()
            );
        }
        TempArtifacts artifacts =
                DefaultJavaPaths.at(projectDir)
                                .tempArtifacts();
        if (artifacts.exists()) {
            return Optional.of(artifacts.toString());
        } else {
            return Optional.empty();
        }
    }
}
