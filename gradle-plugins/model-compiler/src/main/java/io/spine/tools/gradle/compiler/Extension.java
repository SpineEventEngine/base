/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.tools.Indent;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static io.spine.tools.gradle.compiler.ModelCompilerPlugin.SPINE_MODEL_COMPILER_EXTENSION_NAME;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A configuration for the {@link ModelCompilerPlugin}.
 *
 * @author Alex Tymchenko
 */
@SuppressWarnings({"PublicField", "ClassWithTooManyMethods", "WeakerAccess"})
// as this is a Gradle extension.
public class Extension {

    /**
     * The Spine internal directory name for storing temporary build artifacts.
     *
     * <p>Spine Gradle tasks may write some temporary files into this directory.
     *
     * <p>The directory is deleted on {@code :pre-clean"}.
     */
    @Internal
    public static final String SPINE_BUILD_ARTIFACT_STORAGE_DIR = ".spine";

    private static final String DEFAULT_GEN_ROOT_DIR = "/generated";
    private static final String DEFAULT_MAIN_PROTO_SRC_DIR = "/src/main/proto";
    private static final String DEFAULT_MAIN_GEN_RES_DIR = DEFAULT_GEN_ROOT_DIR + "/main/resources";
    private static final String DEFAULT_MAIN_GEN_DIR = DEFAULT_GEN_ROOT_DIR + "/main/java";
    private static final String DEFAULT_MAIN_GEN_GRPC_DIR = DEFAULT_GEN_ROOT_DIR + "/main/grpc";
    private static final String DEFAULT_MAIN_GEN_SPINE_DIR = DEFAULT_GEN_ROOT_DIR + "/main/spine";
    private static final String DEFAULT_MAIN_DESCRIPTORS_PATH = "/build/descriptors/main.desc";

    private static final String DEFAULT_TEST_PROTO_SRC_DIR = "/src/test/proto";
    private static final String DEFAULT_TEST_GEN_RES_DIR = DEFAULT_GEN_ROOT_DIR + "/test/resources";
    private static final String DEFAULT_TEST_GEN_DIR = DEFAULT_GEN_ROOT_DIR + "/test/java";
    private static final String DEFAULT_TEST_GEN_GRPC_DIR = DEFAULT_GEN_ROOT_DIR + "/test/grpc";
    private static final String DEFAULT_TEST_GEN_SPINE_DIR = DEFAULT_GEN_ROOT_DIR + "/test/spine";
    private static final String DEFAULT_TEST_DESCRIPTORS_PATH = "/build/descriptors/test.desc";

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
     * The flag which enables the generation of validating builders for all Protobuf
     * messages in the classpath.
     *
     * <p>By default, only the Protobuf messages from the current module are generated.
     */
    public boolean generateBuildersFromClasspath = false;

    /**
     * The absolute paths to directories to delete.
     *
     * <p>Either this property OR {@code dirToClean} property is used.
     */
    public List<String> dirsToClean = new LinkedList<>();

    public static String getMainProtoSrcDir(Project project) {
        return pathOrDefault(spineProtobuf(project).mainProtoSrcDir,
                             root(project) + DEFAULT_MAIN_PROTO_SRC_DIR);
    }

    public static String getMainTargetGenResourcesDir(Project project) {
        return pathOrDefault(spineProtobuf(project).mainTargetGenResourcesDir,
                             root(project) + DEFAULT_MAIN_GEN_RES_DIR);
    }

    public static String getMainGenGrpcDir(Project project) {
        return pathOrDefault(spineProtobuf(project).mainGenGrpcDir,
                             root(project) + DEFAULT_MAIN_GEN_GRPC_DIR);
    }

    public static String getMainGenProtoDir(Project project) {
        return pathOrDefault(spineProtobuf(project).mainGenProtoDir,
                             root(project) + DEFAULT_MAIN_GEN_DIR);
    }

    public static String getTestTargetGenResourcesDir(Project project) {
        return pathOrDefault(spineProtobuf(project).testTargetGenResourcesDir,
                             root(project) + DEFAULT_TEST_GEN_RES_DIR);
    }

    public static String getTestProtoSrcDir(Project project) {
        return pathOrDefault(spineProtobuf(project).testProtoSrcDir,
                             root(project) + DEFAULT_TEST_PROTO_SRC_DIR);
    }

    public static String getTestGenGrpcDir(Project project) {
        return pathOrDefault(spineProtobuf(project).testGenGrpcDir,
                             root(project) + DEFAULT_TEST_GEN_GRPC_DIR);
    }

    public static String getTestGenProtoDir(Project project) {
        return pathOrDefault(spineProtobuf(project).testGenProtoDir,
                             root(project) + DEFAULT_TEST_GEN_DIR);
    }

    public static String getMainDescriptorSetPath(Project project) {
        return pathOrDefault(spineProtobuf(project).mainDescriptorSetPath,
                             root(project) + DEFAULT_MAIN_DESCRIPTORS_PATH);
    }

    public static String getTestDescriptorSetPath(Project project) {
        return pathOrDefault(spineProtobuf(project).testDescriptorSetPath,
                             root(project) + DEFAULT_TEST_DESCRIPTORS_PATH);
    }

    public static String getTargetGenRejectionsRootDir(Project project) {
        return pathOrDefault(spineProtobuf(project).targetGenRejectionsRootDir,
                             root(project) + DEFAULT_MAIN_GEN_SPINE_DIR);
    }

    public static String getTargetTestGenRejectionsRootDir(Project project) {
        return pathOrDefault(spineProtobuf(project).targetTestGenRejectionsRootDir,
                             root(project) + DEFAULT_TEST_GEN_SPINE_DIR);
    }

    public static String getTargetGenValidatorsRootDir(Project project) {
        return pathOrDefault(spineProtobuf(project).targetGenVBuildersRootDir,
                             root(project) + DEFAULT_MAIN_GEN_SPINE_DIR);
    }

    public static String getTargetTestGenValidatorsRootDir(Project project) {
        return pathOrDefault(spineProtobuf(project).targetTestGenVBuildersRootDir,
                             root(project) + DEFAULT_TEST_GEN_SPINE_DIR);
    }

    private static String pathOrDefault(String path, String defaultValue) {
        return isNullOrEmpty(path)
                ? defaultValue
                : path;
    }

    public static boolean isGenerateValidatingBuilders(Project project) {
        final boolean result = spineProtobuf(project).generateValidatingBuilders;
        log().debug("The current validating builder generation setting is {}", result);
        return result;
    }

    public static Indent getIndent(Project project) {
        final Indent result = spineProtobuf(project).indent;
        log().debug("The current indent is {}", result);
        return result;
    }

    public static boolean isGenerateValidatingBuildersFromClasspath(Project project) {
        final boolean result = spineProtobuf(project).generateBuildersFromClasspath;
        log().debug("Validating builder are generated from  {}",
                    (result ? "the classpath" : "this module only"));
        return result;
    }

    @SuppressWarnings({"InstanceMethodNamingConvention", "unused"})
    public void setGenerateValidatingBuildersFromClasspath(boolean generateFromClasspath) {
        this.generateBuildersFromClasspath = generateFromClasspath;
        log().debug("Validating builder are set to be generated from  {}",
                    (generateFromClasspath ? "the whole classpath" : "the current module only"));
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
        log().trace("Indent has been set to {}", generateValidatingBuilders);
    }

    public static List<String> getDirsToClean(Project project) {
        final List<String> dirsToClean = newLinkedList(spineDirs(project));
        log().debug("Finding the directories to clean");
        final List<String> dirs = spineProtobuf(project).dirsToClean;
        final String singleDir = spineProtobuf(project).dirToClean;
        if (dirs.size() > 0) {
            log().error("Found {} directories to clean: {}", dirs.size(), dirs);
            dirsToClean.addAll(dirs);
        } else if (singleDir != null && !singleDir.isEmpty()) {
            log().debug("Found directory to clean: {}", singleDir);
            dirsToClean.add(singleDir);
        } else {
            final String defaultValue = root(project) + DEFAULT_GEN_ROOT_DIR;
            log().debug("Default directory to clean: {}", defaultValue);
            dirsToClean.add(defaultValue);
        }
        return ImmutableList.copyOf(dirsToClean);
    }

    private static Iterable<String> spineDirs(Project project) {
        final List<String> spineDirs = newLinkedList();
        final Optional<String> spineDir = spineDir(project);
        final Optional<String> rootSpineDir = spineDir(project.getRootProject());
        if (spineDir.isPresent()) {
            spineDirs.add(spineDir.get());
            if (rootSpineDir.isPresent() && !spineDir.equals(rootSpineDir)) {
                spineDirs.add(rootSpineDir.get());
            }
        }
        return spineDirs;
    }

    private static Optional<String> spineDir(Project project) {
        final File projectDir;
        try {
            projectDir = project.getProjectDir()
                                .getCanonicalFile();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Project directory %s is invalid!", project.getProjectDir()
            );
        }
        final Path projectPath = projectDir.toPath();
        final Path spinePath = projectPath.resolve(SPINE_BUILD_ARTIFACT_STORAGE_DIR);
        if (Files.exists(spinePath)) {
            return Optional.of(spinePath.toString());
        } else {
            return Optional.absent();
        }
    }

    private static String root(Project project) {
        return project.getProjectDir()
                      .getAbsolutePath();
    }

    private static Extension spineProtobuf(Project project) {
        return (Extension) project.getExtensions()
                                  .getByName(SPINE_MODEL_COMPILER_EXTENSION_NAME);
    }

    @VisibleForTesting
    public static String getDefaultMainGenDir() {
        return DEFAULT_MAIN_GEN_DIR;
    }

    @VisibleForTesting
    public static String getDefaultMainGenGrpcDir() {
        return DEFAULT_MAIN_GEN_GRPC_DIR;
    }

    @VisibleForTesting
    public static String getDefaultMainDescriptorsPath() {
        return DEFAULT_MAIN_DESCRIPTORS_PATH;
    }

    @VisibleForTesting
    public static String getDefaultMainGenResDir() {
        return DEFAULT_MAIN_GEN_RES_DIR;
    }

    @VisibleForTesting
    public static String getDefaultMainGenSpineDir() {
        return DEFAULT_MAIN_GEN_SPINE_DIR;
    }

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(Extension.class);
    }
}
