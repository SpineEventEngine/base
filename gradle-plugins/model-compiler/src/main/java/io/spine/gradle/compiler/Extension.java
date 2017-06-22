/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.gradle.compiler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.spine.gradle.compiler.ModelCompilerPlugin.SPINE_MODEL_COMPILER_EXTENSION_NAME;
import static java.util.Collections.singletonList;

/**
 * A configuration for the {@link ModelCompilerPlugin}.
 *
 * @author Alex Tymchenko
 */
@SuppressWarnings("PublicField")    // as this is a Gradle extension.
public class Extension {

    private static final String DEFAULT_GEN_ROOT_DIR = "/generated";

    private static final String DEFAULT_MAIN_PROTO_SRC_DIR = "/src/main/proto";
    private static final String DEFAULT_MAIN_GEN_RES_DIR = DEFAULT_GEN_ROOT_DIR + "/main/resources";
    private static final String DEFAULT_MAIN_GEN_DIR = DEFAULT_GEN_ROOT_DIR + "/main/java";
    private static final String DEFAULT_MAIN_GEN_GRPC_DIR = DEFAULT_GEN_ROOT_DIR + "/main/grpc";
    private static final String DEFAULT_MAIN_GEN_FAILURES_DIR = DEFAULT_GEN_ROOT_DIR + "/main/spine";
    private static final String DEFAULT_MAIN_DESCRIPTORS_PATH = "/build/descriptors/main.desc";

    private static final String DEFAULT_TEST_PROTO_SRC_DIR = "/src/test/proto";
    private static final String DEFAULT_TEST_GEN_RES_DIR = DEFAULT_GEN_ROOT_DIR + "/test/resources";
    private static final String DEFAULT_TEST_GEN_DIR = DEFAULT_GEN_ROOT_DIR + "/test/java";
    private static final String DEFAULT_TEST_GEN_GRPC_DIR = DEFAULT_GEN_ROOT_DIR + "/test/grpc";
    private static final String DEFAULT_TEST_GEN_FAILURES_DIR = DEFAULT_GEN_ROOT_DIR + "/test/spine";
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
     * The absolute path to the main target generated failures root directory.
     */
    public String targetGenFailuresRootDir;

    /**
     * The absolute path to the test target generated failures root directory.
     */
    public String targetTestGenFailuresRootDir;

    /**
     * The absolute path to the main target generated validators root directory.
     */
    public String targetGenValidatorsRootDir;

    /**
     * The absolute path to the test target generated validators root directory.
     */
    public String targetTestGenValidatorsRootDir;

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
    public Indent indent = new Indent(4);

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
        final String path = spineProtobuf(project).mainProtoSrcDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_PROTO_SRC_DIR;
        } else {
            return path;
        }
    }

    public static String getMainTargetGenResourcesDir(Project project) {
        final String path = spineProtobuf(project).mainTargetGenResourcesDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_GEN_RES_DIR;
        } else {
            return path;
        }
    }

    public static String getMainGenGrpcDir(Project project) {
        final String path = spineProtobuf(project).mainGenGrpcDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_GEN_GRPC_DIR;
        } else {
            return path;
        }
    }

    public static String getMainGenProtoDir(Project project) {
        final String path = spineProtobuf(project).mainGenProtoDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_GEN_DIR;
        } else {
            return path;
        }
    }

    public static String getTestTargetGenResourcesDir(Project project) {
        final String path = spineProtobuf(project).testTargetGenResourcesDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_GEN_RES_DIR;
        } else {
            return path;
        }
    }

    public static String getTestProtoSrcDir(Project project) {
        final String path = spineProtobuf(project).testProtoSrcDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_PROTO_SRC_DIR;
        } else {
            return path;
        }
    }

    public static String getTestGenGrpcDir(Project project) {
        final String path = spineProtobuf(project).testGenGrpcDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_GEN_GRPC_DIR;
        } else {
            return path;
        }
    }

    public static String getTestGenProtoDir(Project project) {
        final String path = spineProtobuf(project).testGenProtoDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_GEN_DIR;
        } else {
            return path;
        }
    }

    public static String getMainDescriptorSetPath(Project project) {
        final String path = spineProtobuf(project).mainDescriptorSetPath;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_DESCRIPTORS_PATH;
        } else {
            return path;
        }
    }

    public static String getTestDescriptorSetPath(Project project) {
        final String path = spineProtobuf(project).testDescriptorSetPath;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_DESCRIPTORS_PATH;
        } else {
            return path;
        }
    }

    public static String getTargetGenFailuresRootDir(Project project) {
        final String path = spineProtobuf(project).targetGenFailuresRootDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_GEN_FAILURES_DIR;
        } else {
            return path;
        }
    }

    public static String getTargetTestGenFailuresRootDir(Project project) {
        final String path = spineProtobuf(project).targetTestGenFailuresRootDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_GEN_FAILURES_DIR;
        } else {
            return path;
        }
    }

    public static String getTargetGenValidatorsRootDir(Project project) {
        final String path = spineProtobuf(project).targetGenValidatorsRootDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_MAIN_GEN_DIR;
        } else {
            return path;
        }
    }

    public static String getTargetTestGenValidatorsRootDir(Project project) {
        final String path = spineProtobuf(project).targetTestGenValidatorsRootDir;
        if (isNullOrEmpty(path)) {
            return project.getProjectDir()
                          .getAbsolutePath() + DEFAULT_TEST_GEN_DIR;
        } else {
            return path;
        }
    }

    public static boolean isGenerateValidatingBuilders(Project project) {
        final boolean result = spineProtobuf(project).generateValidatingBuilders;
        log().trace("The current validating builder generation setting is {}", result);
        return result;
    }

    public static Indent getIndent(Project project) {
        final Indent result = spineProtobuf(project).indent;
        log().trace("The current indent is {}", result);
        return result;
    }

    public static boolean isGenerateValidatingBuildersFromClasspath(Project project) {
        final boolean result = spineProtobuf(project).generateBuildersFromClasspath;
        log().trace("Validating builder are generated from  {}",
                    (result ? "the classpath" : "this module only"));
        return result;
    }

    // The variable and its setter is named according to its meaning.
    @SuppressWarnings({"InstanceMethodNamingConvention", "MethodParameterNamingConvention"})
    public void setGenerateValidatingBuildersFromClasspath(boolean generateFromClasspath) {
        this.generateBuildersFromClasspath = generateFromClasspath;
        log().trace("Validating builder are set to be generated from  {}",
                    (generateFromClasspath ? "the whole classpath" : "the current module only"));
    }

    // The variable and its setter is named according to its meaning.
    @SuppressWarnings({"MethodParameterNamingConvention", "unused"})
    public void setGenerateValidatingBuilders(boolean generateValidatingBuilders) {
        this.generateValidatingBuilders = generateValidatingBuilders;
        log().trace("Validating builder generation has been {}",
                    (generateValidatingBuilders ? "enabled" : "disabled"));
    }

    public void setIndent(int indent) {
        this.indent = new Indent(indent);
        log().trace("Indent has been set to {}", generateValidatingBuilders);
    }

    public static List<String> getDirsToClean(Project project) {
        log().debug("Finding the directories to clean");
        final List<String> dirs = spineProtobuf(project).dirsToClean;
        if (dirs.size() > 0) {
            log().error("Found {} directories to clean: {}", dirs.size(), dirs);
            return ImmutableList.copyOf(dirs);
        }
        final String singleDir = spineProtobuf(project).dirToClean;
        if (singleDir != null && !singleDir.isEmpty()) {
            log().debug("Found directory to clean: {}", singleDir);
            return singletonList(singleDir);
        }
        final String defaultValue = project.getProjectDir()
                                           .getAbsolutePath() + DEFAULT_GEN_ROOT_DIR;
        log().debug("Default directory to clean: {}", defaultValue);
        return singletonList(defaultValue);
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

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(Extension.class);
    }
}
