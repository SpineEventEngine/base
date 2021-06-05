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

import com.google.common.collect.ImmutableSet;
import io.spine.code.java.ClassName;
import io.spine.tools.mc.java.annotation.AnnotatorFactory;
import io.spine.tools.mc.java.annotation.DefaultAnnotatorFactory;
import io.spine.tools.mc.java.annotation.ModuleAnnotator;
import io.spine.tools.gradle.SpinePlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.tools.mc.java.annotation.ApiOption.beta;
import static io.spine.tools.mc.java.annotation.ApiOption.experimental;
import static io.spine.tools.mc.java.annotation.ApiOption.internal;
import static io.spine.tools.mc.java.annotation.ApiOption.spi;
import static io.spine.tools.mc.java.annotation.ModuleAnnotator.translate;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.ModelCompilerTaskName.annotateProto;
import static io.spine.tools.gradle.ModelCompilerTaskName.annotateTestProto;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeDescriptorSet;
import static io.spine.tools.gradle.ModelCompilerTaskName.mergeTestDescriptorSet;
import static io.spine.tools.mc.java.gradle.Extension.getCodeGenAnnotations;
import static io.spine.tools.mc.java.gradle.Extension.getInternalClassPatterns;
import static io.spine.tools.mc.java.gradle.Extension.getInternalMethodNames;
import static io.spine.tools.mc.java.gradle.Extension.getMainDescriptorSetFile;
import static io.spine.tools.mc.java.gradle.Extension.getGeneratedMainGrpcDir;
import static io.spine.tools.mc.java.gradle.Extension.getGeneratedMainJavaDir;
import static io.spine.tools.mc.java.gradle.Extension.getTestDescriptorSetFile;
import static io.spine.tools.mc.java.gradle.Extension.getGeneratedTestGrpcDir;
import static io.spine.tools.mc.java.gradle.Extension.getGeneratedTestJavaDir;

/**
 * A plugin that annotates generated Java sources from {@code .proto} files.
 *
 * <p>Plugin annotates the Java sources depending on Protobuf option values.
 *
 * <p>To enable the Java sources annotation, apply the plugin to a Gradle project,
 * and annotation will be built into Gradle build lifecycle,
 * between Protobuf generation and Java compilation.
 *
 * <p>Examples:
 *
 * <p>For {@code FileOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * option (experimental_all) = true;
 *
 * message Message {
 * }
 *
 * service Service {
 * }
 * }</pre>
 *
 * <p>Will annotate regular generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      // Annotation goes here.
 *      public static final class Message ...
 *
 *      // Annotation goes here.
 *      public interface MessageOrBuilder ...
 *
 *      // And so on for every message and enum from a Protobuf file.
 * }}</pre>
 *
 * <p>And generated gPRC service like:
 *
 * <pre>{@code
 * // Annotation goes here.
 * public class ServiceGrpc {
 *      // ...
 * }}</pre>
 *
 * <p>For {@code MessageOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * message Message {
 *      option (experimental_type) = true;
 * }}</pre>
 *
 * <p>Will annotate generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      // Annotation goes here.
 *      public static final class Message ...
 *
 *      // Annotation goes here.
 *      public interface MessageOrBuilder ...
 * }}</pre>
 *
 * <p>For {@code ServiceOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * service Service {
 *      option (SPI_service) = true;
 * }}</pre>
 *
 * <p>Will annotate generated gRPC service like:
 *
 * <pre>{@code
 * // Annotation goes here.
 * ServiceGrpc {
 *      // ...
 * }}</pre>
 *
 * <p>For {@code FieldOptions}:
 *
 * <pre>{@code
 * import "spine/options.proto";
 *
 * message Message {
 *      string value = 1 [(experimental) = true] ;
 * }}</pre>
 *
 * <p>Will annotate generated file like:
 *
 * <pre>{@code
 * OuterClassName {
 *
 *      public static final class Message ... {
 *
 *              // Annotation goes here.
 *              public java.lang.String getEntityId() {
 *                  // ...
 *                  }
 *
 *              // And so on for every getter for the field.
 *
 *              public static final class Builder ... {
 *
 *                  // Annotation goes here.
 *                  public java.lang.String getEntityId() {
 *                      // ...
 *                  }
 *
 *                  // Annotation goes here.
 *                  public java.lang.String setEntityId() {
 *                      // ...
 *                  }
 *
 *                  // And so on for every getter/setter for the field.
 *              }
 *      }
 * }}</pre>
 *
 * <p>If {@code java_multiple_files = true} result of annotation will be similar.
 */
public class ProtoAnnotatorPlugin extends SpinePlugin {

    @Override
    public void apply(Project project) {
        createMainTask(project);
        createTestTask(project);
    }

    private void createMainTask(Project project) {
        Action<Task> task = new Annotate(true);
        newTask(annotateProto, task)
                .insertBeforeTask(compileJava)
                .insertAfterTask(mergeDescriptorSet)
                .applyNowTo(project);
    }

    private void createTestTask(Project project) {
        Action<Task> testTask = new Annotate(false);
        newTask(annotateTestProto, testTask)
                .insertBeforeTask(compileTestJava)
                .insertAfterTask(mergeTestDescriptorSet)
                .applyNowTo(project);
    }

    /**
     * A task action which performs generated code annotation.
     */
    private class Annotate implements Action<Task> {

        private final boolean productionTask;

        private Annotate(boolean productionTask) {
            this.productionTask = productionTask;
        }

        @Override
        public void execute(Task task) {
            Project project = task.getProject();
            File descriptorSetFile = descriptorSet(project);
            String generatedProtoDir = generatedProtoDir(project);
            String generatedGrpcDir = generatedGrpcDir(project);
            if (descriptorSetFile.exists()) {
                ModuleAnnotator moduleAnnotator = createAnnotator(project,
                                                                  descriptorSetFile,
                                                                  generatedProtoDir,
                                                                  generatedGrpcDir);
                moduleAnnotator.annotate();
            } else {
                logMissingDescriptorSetFile(descriptorSetFile);
            }
        }

        private ModuleAnnotator createAnnotator(Project project,
                                                File descriptorSetFile,
                                                String generatedProtoDir,
                                                String generatedGrpcDir) {
            Path generatedProtoPath = Paths.get(generatedProtoDir);
            Path generatedGrpcPath = Paths.get(generatedGrpcDir);
            AnnotatorFactory annotatorFactory = DefaultAnnotatorFactory
                    .newInstance(descriptorSetFile, generatedProtoPath, generatedGrpcPath);
            CodeGenAnnotations annotations = getCodeGenAnnotations(project);
            ClassName internalClassName = annotations.internalClassName();
            ImmutableSet<String> internalClassPatterns = getInternalClassPatterns(project);
            ImmutableSet<String> internalMethodNames = getInternalMethodNames(project);
            return ModuleAnnotator
                    .newBuilder()
                    .setAnnotatorFactory(annotatorFactory)
                    .add(translate(spi()).as(annotations.spiClassName()))
                    .add(translate(beta()).as(annotations.betaClassName()))
                    .add(translate(experimental()).as(annotations.experimentalClassName()))
                    .add(translate(internal()).as(internalClassName))
                    .setInternalPatterns(internalClassPatterns)
                    .setInternalMethodNames(internalMethodNames)
                    .setInternalAnnotation(internalClassName)
                    .build();
        }

        private File descriptorSet(Project project) {
            return productionTask
                   ? getMainDescriptorSetFile(project)
                   : getTestDescriptorSetFile(project);
        }

        private String generatedGrpcDir(Project project) {
            return productionTask
                   ? getGeneratedMainGrpcDir(project)
                   : getGeneratedTestGrpcDir(project);
        }

        private String generatedProtoDir(Project project) {
            return productionTask
                   ? getGeneratedMainJavaDir(project)
                   : getGeneratedTestJavaDir(project);
        }
    }
}

