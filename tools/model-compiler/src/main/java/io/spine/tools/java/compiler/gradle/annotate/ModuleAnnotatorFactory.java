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

package io.spine.tools.java.compiler.gradle.annotate;

import io.spine.tools.java.compiler.annotation.AnnotatorFactory;
import io.spine.tools.java.compiler.annotation.DefaultAnnotatorFactory;
import io.spine.tools.java.compiler.annotation.Job;
import io.spine.tools.java.compiler.annotation.ModuleAnnotator;
import io.spine.tools.compiler.gradle.Annotations;
import io.spine.tools.compiler.gradle.Extension;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.spine.tools.java.compiler.annotation.ApiOption.beta;
import static io.spine.tools.java.compiler.annotation.ApiOption.experimental;
import static io.spine.tools.java.compiler.annotation.ApiOption.internal;
import static io.spine.tools.java.compiler.annotation.ApiOption.spi;
import static io.spine.tools.java.compiler.annotation.ModuleAnnotator.translate;

/**
 * Creates a {@link io.spine.tools.java.compiler.annotation.ModuleAnnotator ModuleAnnotator} for
 * the passed Gradle project.
 */
final class ModuleAnnotatorFactory {

    private final boolean productionTask;
    private final Extension extension;

    ModuleAnnotatorFactory(Project project, boolean productionTask) {
        this.extension = Extension.of(project);
        this.productionTask = productionTask;
    }

    ModuleAnnotator createAnnotator() {
        AnnotatorFactory factory = DefaultAnnotatorFactory.newInstance(
                descriptorSetFile(), generatedProtoDir(), generatedGrpcDir()
        );
        Annotations configured = extension.generateAnnotations;
        Job annotateSpi = translate(spi()).as(configured.spiClassName());
        Job annotateBeta = translate(beta()).as(configured.betaClassName());
        Job annotateExperimental = translate(experimental()).as(configured.experimentalClassName());
        Job annotateInternal = translate(internal()).as(configured.internalClassName());

        return ModuleAnnotator.newBuilder()
                .setAnnotatorFactory(factory)
                .add(annotateSpi)
                .add(annotateBeta)
                .add(annotateExperimental)
                .add(annotateInternal)
                .setInternalPatterns(extension.internalClassPatterns())
                .setInternalMethodNames(extension.internalMethodNames())
                .setInternalAnnotation(configured.internalClassName())
                .build();
    }

    File descriptorSetFile() {
        return productionTask
               ? extension.mainDescriptorSetFile()
               : extension.testDescriptorSetFile();
    }

    private Path generatedGrpcDir() {
        String path = productionTask
                   ? extension.generatedMainGrpcJavaDir()
                   : extension.generatedTestGrpcJavaDir();
        return Paths.get(path);
    }

    private Path generatedProtoDir() {
        String path = productionTask
                   ? extension.generatedMainJavaDir()
                   : extension.generatedTestJavaDir();
        return Paths.get(path);
    }
}
