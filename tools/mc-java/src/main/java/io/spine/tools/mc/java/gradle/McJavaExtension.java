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
import com.google.common.flogger.FluentLogger;
import groovy.lang.Closure;
import io.spine.tools.java.fs.DefaultJavaPaths;
import io.spine.tools.code.Indent;
import io.spine.tools.gradle.GradleExtension;
import io.spine.tools.protoc.EntityQueries;
import io.spine.tools.protoc.Fields;
import io.spine.tools.protoc.Interfaces;
import io.spine.tools.protoc.Methods;
import io.spine.tools.protoc.NestedClasses;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
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
 * A configuration for the {@link McJavaPlugin}.
 */
@SuppressWarnings({
        "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
        "ClassWithTooManyMethods" /* The methods are needed for handing default values. */,
        "ClassWithTooManyFields", "PMD.TooManyFields" /* Gradle extensions are flat like this. */})
public class McJavaExtension extends GradleExtension {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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
     * The absolute path to the test Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestJavaDir;

    /**
     * The absolute path to the test target generated resources directory.
     */
    public String generatedTestResourcesDir;

    /**
     * The absolute path to the test {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String generatedTestGrpcDir;

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
     * <p>Either this property OR {@code dirsToClean} property is used.
     */
    public String dirToClean;

    /**
     * The indent for the generated code in the validating builders.
     */
    public Indent indent = Indent.of4();

    /**
     * The absolute paths to directories to delete.
     *
     * <p>Either this property OR {@code dirToClean} property is used.
     */
    public List<String> dirsToClean = new ArrayList<>();

    /**
     * The severity of the Spine-custom Error Prone checks.
     *
     * <p>If this value is not set, the default severities are used, which are specific for the
     * each check.
     *
     * <p>May be overridden by the values provided by the {@link ErrorProneChecksExtension}.
     */
    public Severity defaultCheckSeverity;

    public final CodeGenAnnotations generateAnnotations = new CodeGenAnnotations();

    public final Interfaces interfaces = new Interfaces();

    public final Methods methods = new Methods();

    public final NestedClasses nestedClasses = new NestedClasses();

    public final Fields fields = new Fields();

    public final EntityQueries entityQueries = new EntityQueries();

    public boolean generateValidatingBuilders = true;

    public boolean generateValidation = true;

    public List<String> internalClassPatterns = new ArrayList<>();

    public List<String> internalMethodNames = new ArrayList<>();

    @Override
    protected DefaultJavaPaths defaultPaths(Project project) {
        return def(project);
    }

    private static DefaultJavaPaths def(Project project) {
        return DefaultJavaPaths.at(project.getProjectDir());
    }

    @SuppressWarnings({
            "PMD.MethodNamingConventions",
            "FloggerSplitLogStatement" // See: https://github.com/SpineEventEngine/base/issues/612
    })
    private static FluentLogger.Api _debug() {
        return logger.atFine();
    }

    @SuppressWarnings("FloggerSplitLogStatement")

    public static String getMainProtoDir(Project project) {
        McJavaExtension extension = extension(project);
        _debug().log("Extension is `%s`.", extension);
        String protoDir = extension.mainProtoDir;
        _debug().log("`modelCompiler.mainProtoSrcDir` is `%s`.", protoDir);
        return pathOrDefault(protoDir,
                             def(project).src()
                                         .mainProto());
    }

    public static String getTestProtoDir(Project project) {
        return pathOrDefault(extension(project).testProtoDir,
                             def(project).src()
                                         .testProto());
    }

    public static File getMainDescriptorSetFile(Project project) {
        McJavaExtension extension = extension(project);
        String path = pathOrDefault(extension.mainDescriptorSetFile,
                                    extension.defaultMainDescriptor(project));
        return new File(path);
    }

    public static File getTestDescriptorSetFile(Project project) {
        McJavaExtension extension = extension(project);
        String path = pathOrDefault(extension.testDescriptorSetFile,
                                    extension.defaultTestDescriptor(project));
        return new File(path);
    }

    public static String getGeneratedMainJavaDir(Project project) {
        return pathOrDefault(extension(project).generatedMainJavaDir,
                             def(project).generated()
                                         .mainJava());
    }

    public static String getGeneratedMainGrpcDir(Project project) {
        return pathOrDefault(extension(project).generatedMainGrpcJavaDir,
                             def(project).generated()
                                         .mainGrpc());
    }

    public static String getGeneratedMainResourcesDir(Project project) {
        return pathOrDefault(extension(project).generatedMainResourcesDir,
                             def(project).generated()
                                         .mainResources());
    }

    public static String getGeneratedTestJavaDir(Project project) {
        return pathOrDefault(extension(project).generatedTestJavaDir,
                             def(project).generated()
                                         .testJava());
    }

    public static String getGeneratedTestResourcesDir(Project project) {
        return pathOrDefault(extension(project).generatedTestResourcesDir,
                             def(project).generated()
                                         .testResources());
    }

    public static String getGeneratedTestGrpcDir(Project project) {
        return pathOrDefault(extension(project).generatedTestGrpcDir,
                             def(project).generated()
                                         .testGrpc());
    }

    public static String getGeneratedMainRejectionsDir(Project project) {
        return pathOrDefault(extension(project).generatedMainRejectionsDir,
                             def(project).generated()
                                         .mainSpine());
    }

    public static String getGeneratedTestRejectionsDir(Project project) {
        return pathOrDefault(extension(project).generatedTestRejectionsDir,
                             def(project).generated()
                                         .testSpine());
    }

    private static String pathOrDefault(String path, Object defaultValue) {
        return isNullOrEmpty(path)
               ? defaultValue.toString()
               : path;
    }

    public static Indent getIndent(Project project) {
        Indent result = extension(project).indent;
        _debug().log("The current indent is %d.", result.size());
        return result;
    }

    @SuppressWarnings("unused")
    public void setIndent(int indent) {
        this.indent = Indent.of(indent);
        _debug().log("Indent has been set to %d.", indent);
    }

    public static List<String> getDirsToClean(Project project) {
        List<String> dirsToClean = newLinkedList(spineDirs(project));
        _debug().log("Finding the directories to clean.");
        List<String> dirs = extension(project).dirsToClean;
        String singleDir = extension(project).dirToClean;
        if (dirs.size() > 0) {
            logger.atInfo()
                  .log("Found %d directories to clean: `%s`.", dirs.size(), dirs);
            dirsToClean.addAll(dirs);
        } else if (singleDir != null && !singleDir.isEmpty()) {
            _debug().log("Found directory to clean: `%s`.", singleDir);
            dirsToClean.add(singleDir);
        } else {
            String defaultValue = def(project).generated()
                                              .toString();
            _debug().log("Default directory to clean: `%s`.", defaultValue);
            dirsToClean.add(defaultValue);
        }
        return ImmutableList.copyOf(dirsToClean);
    }

    public static @Nullable Severity getSpineCheckSeverity(Project project) {
        Severity result = extension(project).defaultCheckSeverity;
        _debug().log("The severity of Spine-custom Error Prone checks is `%s`.",
                     (result == null ? "unset" : result.name()));
        return result;
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Closure<?> closure) {
        ConfigureUtil.configure(closure, generateAnnotations);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Action<? super CodeGenAnnotations> action) {
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

    public static CodeGenAnnotations getCodeGenAnnotations(Project project) {
        CodeGenAnnotations annotations = extension(project).generateAnnotations;
        return annotations;
    }

    public static Interfaces getInterfaces(Project project) {
        Interfaces interfaces = extension(project).interfaces;
        return interfaces;
    }

    public static Methods getMethods(Project project) {
        Methods methods = extension(project).methods;
        return methods;
    }

    public static NestedClasses getNestedClasses(Project project) {
        NestedClasses nestedClasses = extension(project).nestedClasses;
        return nestedClasses;
    }

    public static Fields getFields(Project project) {
        Fields fields = extension(project).fields;
        return fields;
    }

    public static EntityQueries getEntityQueries(Project project) {
        EntityQueries columns = extension(project).entityQueries;
        return columns;
    }

    public static boolean shouldGenerateValidatingBuilders(Project project) {
        boolean shouldGenerate = extension(project).generateValidatingBuilders;
        return shouldGenerate;
    }

    public static boolean shouldGenerateValidation(Project project) {
        boolean shouldGenerate = extension(project).generateValidation;
        return shouldGenerate;
    }

    public static ImmutableSet<String> getInternalClassPatterns(Project project) {
        List<String> patterns = extension(project).internalClassPatterns;
        return ImmutableSet.copyOf(patterns);
    }

    public static ImmutableSet<String> getInternalMethodNames(Project project) {
        List<String> patterns = extension(project).internalMethodNames;
        return ImmutableSet.copyOf(patterns);
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
        File spinePath = DefaultJavaPaths.at(projectDir)
                                         .tempArtifacts();
        if (spinePath.exists()) {
            return Optional.of(spinePath.toString());
        } else {
            return Optional.empty();
        }
    }

    private static McJavaExtension extension(Project project) {
        return (McJavaExtension)
                project.getExtensions()
                       .getByName(McJavaPlugin.extensionName());
    }
}
