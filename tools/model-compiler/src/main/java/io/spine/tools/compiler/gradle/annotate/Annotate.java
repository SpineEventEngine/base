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

package io.spine.tools.compiler.gradle.annotate;

import com.google.common.collect.ImmutableSet;
import io.spine.code.java.ClassName;
import io.spine.tools.compiler.annotation.AnnotatorFactory;
import io.spine.tools.compiler.annotation.DefaultAnnotatorFactory;
import io.spine.tools.compiler.annotation.ModuleAnnotator;
import io.spine.tools.compiler.gradle.Annotations;
import io.spine.tools.compiler.gradle.Extension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.compiler.annotation.ApiOption.beta;
import static io.spine.tools.compiler.annotation.ApiOption.experimental;
import static io.spine.tools.compiler.annotation.ApiOption.internal;
import static io.spine.tools.compiler.annotation.ApiOption.spi;
import static io.spine.tools.compiler.annotation.ModuleAnnotator.translate;
import static io.spine.tools.compiler.gradle.Extension.getCodeGenAnnotations;
import static io.spine.tools.compiler.gradle.Extension.getInternalClassPatterns;
import static io.spine.tools.compiler.gradle.Extension.getInternalMethodNames;

/**
 * A task action which performs generated code annotation.
 */
class Annotate implements Action<Task> {

    private final ProtoAnnotatorPlugin plugin;
    private final Project project;
    private final boolean productionTask;
    private final Extension extension;

    Annotate(ProtoAnnotatorPlugin plugin, Project project, boolean productionTask) {
        this.plugin = checkNotNull(plugin);
        this.project = checkNotNull(project);
        this.productionTask = productionTask;
        this.extension = Extension.of(project);
    }

    @Override
    public void execute(Task task) {
        File descriptorSetFile = descriptorSet();
        String generatedProtoDir = generatedProtoDir();
        String generatedGrpcDir = generatedGrpcDir();
        if (descriptorSetFile.exists()) {
            ModuleAnnotator moduleAnnotator = createAnnotator(
                    descriptorSetFile,
                    generatedProtoDir,
                    generatedGrpcDir);
            moduleAnnotator.annotate();
        } else {
            plugin.logMissingDescriptorSetFile(descriptorSetFile);
        }
    }

    private ModuleAnnotator createAnnotator(File descriptorSetFile,
                                            String generatedProtoDir,
                                            String generatedGrpcDir) {
        Path generatedProtoPath = Paths.get(generatedProtoDir);
        Path generatedGrpcPath = Paths.get(generatedGrpcDir);
        AnnotatorFactory annotatorFactory = DefaultAnnotatorFactory
                .newInstance(descriptorSetFile, generatedProtoPath, generatedGrpcPath);
        Annotations annotations = getCodeGenAnnotations(project);
        ClassName internalClassName = annotations.internalClassName();
        ImmutableSet<String> internalClassPatterns = getInternalClassPatterns(project);
        ImmutableSet<String> internalMethodNames = getInternalMethodNames(project);
        return ModuleAnnotator.newBuilder()
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

    private File descriptorSet() {

        return productionTask
               ? extension.mainDescriptorSetFile()
               : extension.testDescriptorSetFile();
    }

    private String generatedGrpcDir() {
        return productionTask
               ? extension.generatedMainGrpcJavaDir()
               : extension.generatedTestGrpcJavaDir();
    }

    private String generatedProtoDir() {
        return productionTask
               ? extension.generatedMainJavaDir()
               : extension.generatedTestJavaDir();
    }
}
