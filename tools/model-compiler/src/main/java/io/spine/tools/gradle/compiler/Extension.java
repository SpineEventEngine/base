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
import com.google.common.flogger.FluentLogger;
import groovy.lang.Closure;
import io.spine.code.fs.java.DefaultJavaProject;
import io.spine.code.gen.Indent;
import io.spine.tools.gradle.GradleExtension;
import io.spine.tools.protoc.GeneratedInterfaces;
import io.spine.tools.protoc.GeneratedMethods;
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
 * A configuration for the {@link ModelCompilerPlugin}.
 */
@SuppressWarnings({
        "PublicField", "WeakerAccess" /* Expose fields as a Gradle extension */,
        "ClassWithTooManyMethods" /* The methods are needed for handing default values. */,
        "ClassWithTooManyFields", "PMD.TooManyFields" /* Gradle extensions are flat like this. */})
public class Extension extends GradleExtension {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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
     *
     * <p>The file must have the {@code .desc} extension.
     */
    public String mainDescriptorSetPath;

    /**
     * The absolute path to the test Protobuf descriptor set file.
     *
     * <p>The file must have the {@code .desc} extension.
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
     * The absolute path to the main target generated columns root directory.
     */
    public String targetGenColumnsRootDir;

    /**
     * The absolute path to the test target generated columns root directory.
     */
    public String targetTestGenColumnsRootDir;

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
    public Severity spineCheckSeverity;

    public final CodeGenAnnotations generateAnnotations = new CodeGenAnnotations();

    public final GeneratedInterfaces interfaces = new GeneratedInterfaces();

    public final GeneratedMethods methods = new GeneratedMethods();

    public boolean generateValidatingBuilders = true;

    public List<String> internalClassPatterns = new ArrayList<>();

    public List<String> internalMethodNames = new ArrayList<>();

    @Override
    protected DefaultJavaProject defaultProject(Project project) {
        return def(project);
    }

    private static DefaultJavaProject def(Project project) {
        return DefaultJavaProject.at(project.getProjectDir());
    }

    @SuppressWarnings("PMD.MethodNamingConventions")
    private static FluentLogger.Api _debug() {
        return logger.atFine();
    }

    public static String getMainProtoSrcDir(Project project) {
        Extension extension = extension(project);
        _debug().log("Extension is `%s`.", extension);
        String protoDir = extension.mainProtoSrcDir;
        _debug().log("`modelCompiler.mainProtoSrcDir` is `%s`.", protoDir);
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

    public static File getMainDescriptorSet(Project project) {
        Extension extension = extension(project);
        String path = pathOrDefault(extension.mainDescriptorSetPath,
                                    extension.defaultMainDescriptor(project));
        return new File(path);
    }

    public static File getTestDescriptorSet(Project project) {
        Extension extension = extension(project);
        String path = pathOrDefault(extension.testDescriptorSetPath,
                                    extension.defaultTestDescriptor(project));
        return new File(path);
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

    public static String getTargetGenColumnsRootDir(Project project) {
        return pathOrDefault(extension(project).targetGenColumnsRootDir,
                             def(project).generated()
                                         .mainSpine());
    }

    public static String getTargetTestGenColumnsRootDir(Project project) {
        return pathOrDefault(extension(project).targetTestGenColumnsRootDir,
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
        _debug().log("The current indent is %d.", result.getSize());
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
        Severity result = extension(project).spineCheckSeverity;
        _debug().log("The severity of Spine-custom Error Prone checks is `%s`.",
                     (result == null ? "unset" : result.name()));
        return result;
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Closure closure) {
        ConfigureUtil.configure(closure, generateAnnotations);
    }

    @SuppressWarnings("unused") // Configures `generateAnnotations` closure.
    public void generateAnnotations(Action<? super CodeGenAnnotations> action) {
        action.execute(generateAnnotations);
    }

    @SuppressWarnings("unused") // Configures `interfaces` closure.
    public void interfaces(Closure closure) {
        ConfigureUtil.configure(closure, interfaces);
    }

    @SuppressWarnings("unused") // Configures `interfaces` closure.
    public void interfaces(Action<? super GeneratedInterfaces> action) {
        action.execute(interfaces);
    }

    @SuppressWarnings("unused") // Configures `methods` closure.
    public void methods(Closure closure) {
        ConfigureUtil.configure(closure, methods);
    }

    @SuppressWarnings("unused") // Configures `methods` closure.
    public void methods(Action<? super GeneratedMethods> action) {
        action.execute(methods);
    }

    public static CodeGenAnnotations getCodeGenAnnotations(Project project) {
        CodeGenAnnotations annotations = extension(project).generateAnnotations;
        return annotations;
    }

    public static GeneratedInterfaces getInterfaces(Project project) {
        GeneratedInterfaces interfaces = extension(project).interfaces;
        return interfaces;
    }

    public static GeneratedMethods getMethods(Project project) {
        GeneratedMethods methods = extension(project).methods;
        return methods;
    }

    public static boolean shouldGenerateValidatingBuilders(Project project) {
        boolean shouldGenerate = extension(project).generateValidatingBuilders;
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
}
