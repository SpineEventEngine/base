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
package io.spine.tools.gradle.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import groovy.lang.Closure;
import io.spine.code.generate.Indent;
import io.spine.code.java.DefaultJavaProject;
import io.spine.logging.Logging;
import io.spine.tools.gradle.BaseExtension;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;
import org.slf4j.Logger;

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
 */
@SuppressWarnings({
        "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
        "ClassWithTooManyMethods" /* The methods are needed for handing default values. */,
        "ClassWithTooManyFields" /* OK for a Gradle extension to have a flat structure. */})
public class Extension extends BaseExtension {

    /**
     * The absolute path to the main target generated resources directory.
     */
    public String mainTargetGenResourcesDir;

    /**
     * The absolute path to the main Protobuf source directory.
     */
    public String mainProtoSrcDir;

    /**
     * The absolute path to the main Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String mainGenProtoDir;

    /**
     * The absolute path to the main {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String mainGenGrpcDir;

    /**
     * The absolute path to the test target generated resources directory.
     */
    public String testTargetGenResourcesDir;

    /**
     * The absolute path to the test Protobuf source directory.
     */
    public String testProtoSrcDir;

    /**
     * The absolute path to the test Java sources directory,
     * generated basing on Protobuf definitions.
     */
    public String testGenProtoDir;

    /**
     * The absolute path to the test {@code gRPC} services directory,
     * generated basing on Protobuf definitions.
     */
    public String testGenGrpcDir;

    /**
     * The absolute path to the main Protobuf descriptor set file.
     */
    public String mainDescriptorSetPath;

    /**
     * The absolute path to the test Protobuf descriptor set file.
     */
    public String testDescriptorSetPath;

    /**
     * The absolute path to the main target generated rejections root directory.
     */
    public String targetGenRejectionsRootDir;

    /**
     * The absolute path to the test target generated rejections root directory.
     */
    public String targetTestGenRejectionsRootDir;

    /**
     * The absolute path to the main target generated validating builders root directory.
     */
    public String targetGenVBuildersRootDir;

    /**
     * The absolute path to the test target generated generated validating builders root directory.
     */
    public String targetTestGenVBuildersRootDir;

    /**
     * The absolute path to directory to delete.
     *
     * <p>Either this property OR {@code dirsToClean} property is used.
     */
    public String dirToClean;

    /**
     * The flag which determines validating builders generation is needed or not.
     */
    public boolean generateValidatingBuilders = true;

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
    public Severity spineCheckSeverity;

    public final CodeGenAnnotations generateAnnotations = new CodeGenAnnotations();

    public final GeneratedInterfaces generateInterfaces = GeneratedInterfaces.withDefaults();

    public List<String> internalClassPatterns = new ArrayList<>();

    public List<String> internalMethodNames = new ArrayList<>();

    @Override
    protected DefaultJavaProject defaultProject(Project project) {
        return def(project);
    }

    private static DefaultJavaProject def(Project project) {
        return DefaultJavaProject.at(project.getProjectDir());
    }

    public static String getMainProtoSrcDir(Project project) {
        Logger log = log();
        Extension extension = extension(project);
        log.debug("Extension is {}", extension);
        String protoDir = extension.mainProtoSrcDir;
        log.debug("modelCompiler.mainProtoSrcDir is {}", protoDir);
        return pathOrDefault(protoDir,
                             def(project).src()
                                         .mainProto());
    }

    public static String getMainTargetGenResourcesDir(Project project) {
        return pathOrDefault(extension(project).mainTargetGenResourcesDir,
                             def(project).generated()
                                         .mainResources());
    }

    public static String getMainGenGrpcDir(Project project) {
        return pathOrDefault(extension(project).mainGenGrpcDir,
                             def(project).generated()
                                         .mainGrpc());
    }

    public static String getMainGenProtoDir(Project project) {
        return pathOrDefault(extension(project).mainGenProtoDir,
                             def(project).generated()
                                         .mainJava());
    }

    public static String getTestTargetGenResourcesDir(Project project) {
        return pathOrDefault(extension(project).testTargetGenResourcesDir,
                             def(project).generated()
                                         .testResources());
    }

    public static String getTestProtoSrcDir(Project project) {
        return pathOrDefault(extension(project).testProtoSrcDir,
                             def(project).src()
                                         .testProto());
    }

    public static String getTestGenGrpcDir(Project project) {
        return pathOrDefault(extension(project).testGenGrpcDir,
                             def(project).generated()
                                         .testGrpc());
    }

    public static String getTestGenProtoDir(Project project) {
        return pathOrDefault(extension(project).testGenProtoDir,
                             def(project).generated()
                                         .testJava());
    }

    public static String getMainDescriptorSetPath(Project project) {
        Extension extension = extension(project);
        return pathOrDefault(extension.mainDescriptorSetPath,
                             extension.defaultMainDescriptor(project));
    }

    public static String getTestDescriptorSetPath(Project project) {
        Extension extension = extension(project);
        return pathOrDefault(extension.testDescriptorSetPath,
                             extension.defaultTestDescriptor(project));
    }

    public static String getTargetGenRejectionsRootDir(Project project) {
        return pathOrDefault(extension(project).targetGenRejectionsRootDir,
                             def(project).generated()
                                         .mainSpine());
    }

    public static String getTargetTestGenRejectionsRootDir(Project project) {
        return pathOrDefault(extension(project).targetTestGenRejectionsRootDir,
                             def(project).generated()
                                         .testSpine());
    }

    public static String getTargetGenValidatorsRootDir(Project project) {
        return pathOrDefault(extension(project).targetGenVBuildersRootDir,
                             def(project).generated()
                                         .mainSpine());
    }

    public static String getTargetTestGenValidatorsRootDir(Project project) {
        return pathOrDefault(extension(project).targetTestGenVBuildersRootDir,
                             def(project).generated()
                                         .testSpine());
    }

    private static String pathOrDefault(String path, Object defaultValue) {
        return isNullOrEmpty(path)
               ? defaultValue.toString()
               : path;
    }

    public static boolean isGenerateValidatingBuilders(Project project) {
        boolean result = extension(project).generateValidatingBuilders;
        log().debug("The current validating builder generation setting is {}", result);
        return result;
    }

    public static Indent getIndent(Project project) {
        Indent result = extension(project).indent;
        log().debug("The current indent is {}", result.getSize());
        return result;
    }

    @SuppressWarnings("unused")
    public void setGenerateValidatingBuilders(boolean generateValidatingBuilders) {
        this.generateValidatingBuilders = generateValidatingBuilders;
        log().debug("Validating builder generation has been {}",
                    (generateValidatingBuilders ? "enabled" : "disabled"));
    }

    @SuppressWarnings("unused")
    public void setIndent(int indent) {
        this.indent = Indent.of(indent);
        log().debug("Indent has been set to {}", generateValidatingBuilders);
    }

    public static List<String> getDirsToClean(Project project) {
        List<String> dirsToClean = newLinkedList(spineDirs(project));
        Logger log = log();
        log.debug("Finding the directories to clean");
        List<String> dirs = extension(project).dirsToClean;
        String singleDir = extension(project).dirToClean;
        if (dirs.size() > 0) {
            log.error("Found {} directories to clean: {}", dirs.size(), dirs);
            dirsToClean.addAll(dirs);
        } else if (singleDir != null && !singleDir.isEmpty()) {
            log.debug("Found directory to clean: {}", singleDir);
            dirsToClean.add(singleDir);
        } else {
            String defaultValue = def(project).generated()
                                              .toString();
            log.debug("Default directory to clean: {}", defaultValue);
            dirsToClean.add(defaultValue);
        }
        return ImmutableList.copyOf(dirsToClean);
    }

    public static @Nullable Severity getSpineCheckSeverity(Project project) {
        Severity result = extension(project).spineCheckSeverity;
        log().debug("The severity of Spine-custom Error Prone checks is {}",
                    (result == null ? "unset" : result.name()));
        return result;
    }

    @SuppressWarnings("unused")
        // Used by Gradle to configure `generateAnnotations` with a closure.
    public void generateAnnotations(Closure closure) {
        ConfigureUtil.configure(closure, generateAnnotations);
    }

    @SuppressWarnings("unused")
        // Used by Gradle to configure `generateAnnotations` with a closure.
    public void generateAnnotations(Action<? super CodeGenAnnotations> action) {
        action.execute(generateAnnotations);
    }

    @SuppressWarnings("unused")
        // Used by Gradle to configure `generateInterfaces` with a closure.
    public void generateInterfaces(Closure closure) {
        ConfigureUtil.configure(closure, generateInterfaces);
    }

    @SuppressWarnings("unused")
        // Used by Gradle to configure `generateInterfaces` with a closure.
    public void generateInterfaces(Action<? super GeneratedInterfaces> action) {
        action.execute(generateInterfaces);
    }

    public static CodeGenAnnotations getCodeGenAnnotations(Project project) {
        CodeGenAnnotations annotations = extension(project).generateAnnotations;
        return annotations;
    }

    public static GeneratedInterfaces getGeneratedInterfaces(Project project) {
        GeneratedInterfaces interfaces = extension(project).generateInterfaces;
        return interfaces;
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
                    e, "Project directory %s is invalid!", project.getProjectDir()
            );
        }
        File spinePath = DefaultJavaProject.at(projectDir)
                                           .tempArtifacts();
        if (spinePath.exists()) {
            return Optional.of(spinePath.toString());
        } else {
            return Optional.empty();
        }
    }

    private static Extension extension(Project project) {
        return (Extension)
                project.getExtensions()
                       .getByName(ModelCompilerPlugin.extensionName());
    }

    private static Logger log() {
        return Logging.get(Extension.class);
    }
}
