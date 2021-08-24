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
import org.checkerframework.checker.nullness.qual.NonNull;
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
import static io.spine.tools.mc.java.gradle.McJavaExtension.getCodeGenAnnotations;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getGeneratedMainGrpcDir;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getGeneratedMainJavaDir;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getGeneratedTestGrpcDir;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getGeneratedTestJavaDir;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getInternalClassPatterns;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getInternalMethodNames;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getMainDescriptorSetFile;
import static io.spine.tools.mc.java.gradle.McJavaExtension.getTestDescriptorSetFile;

/**
 * A task action which performs generated code annotation.
 */
final class AnnotationAction implements Action<Task> {

    private final AnnotatorPlugin plugin;
    private final boolean mainCode;

    AnnotationAction(AnnotatorPlugin plugin, boolean mainCode) {
        this.plugin = plugin;
        this.mainCode = mainCode;
    }

    @Override
    public void execute(Task task) {
        Project project = task.getProject();
        File descriptorSetFile = descriptorSet(project);
        if (!descriptorSetFile.exists()) {
            plugin.logMissingDescriptorSetFile(descriptorSetFile);
            return;
        }
        ModuleAnnotator annotator = createAnnotator(project);
        annotator.annotate();
    }

    @NonNull
    private ModuleAnnotator createAnnotator(Project project) {
        AnnotatorFactory annotatorFactory = createAnnotationFactory(project);
        CodeGenAnnotations annotations = getCodeGenAnnotations(project);
        ClassName internalClassName = annotations.internalClassName();
        ImmutableSet<String> internalClassPatterns = getInternalClassPatterns(project);
        ImmutableSet<String> internalMethodNames = getInternalMethodNames(project);
        ModuleAnnotator annotator = ModuleAnnotator.newBuilder()
                .setAnnotatorFactory(annotatorFactory)
                .add(translate(spi()).as(annotations.spiClassName()))
                .add(translate(beta()).as(annotations.betaClassName()))
                .add(translate(experimental()).as(annotations.experimentalClassName()))
                .add(translate(internal()).as(internalClassName))
                .setInternalPatterns(internalClassPatterns)
                .setInternalMethodNames(internalMethodNames)
                .setInternalAnnotation(internalClassName)
                .build();
        return annotator;
    }

    @NonNull
    private AnnotatorFactory createAnnotationFactory(Project project) {
        File descriptorSetFile = descriptorSet(project);
        Path generatedProtoPath = Paths.get(generatedJavaDir(project));
        Path generatedGrpcPath = Paths.get(generatedGrpcDir(project));
        AnnotatorFactory annotatorFactory = DefaultAnnotatorFactory.newInstance(
                descriptorSetFile, generatedProtoPath, generatedGrpcPath
        );
        return annotatorFactory;
    }

    private File descriptorSet(Project project) {
        return mainCode
               ? getMainDescriptorSetFile(project)
               : getTestDescriptorSetFile(project);
    }

    private String generatedGrpcDir(Project project) {
        return mainCode
               ? getGeneratedMainGrpcDir(project)
               : getGeneratedTestGrpcDir(project);
    }

    private String generatedJavaDir(Project project) {
        return mainCode
               ? getGeneratedMainJavaDir(project)
               : getGeneratedTestJavaDir(project);
    }
}
