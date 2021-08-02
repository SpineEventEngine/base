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

import com.google.common.annotations.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;

/**
 * A helper for adding
 * {@link io.spine.tools.gradle.ConfigurationName#annotationProcessor annotationProcessor}
 * configuration to a Gradle Project.
 */
final class AnnotationProcessorConfiguration {

    /** Prevents instantiation of this utility class. */
    private AnnotationProcessorConfiguration() {
    }

    /**
     * Obtains the {@code annotationProcessor} configuration for the project.
     *
     * <p>In the newer Gradle versions ({@code 4.6} and above) the configuration most probably
     * already exists. If not, it will be created.
     *
     * @return the {@code annotationProcessor} configuration of the project
     */
    static Configuration findOrCreateIn(Project project) {
        checkNotNull(project);
        ConfigurationContainer configurations = project.getConfigurations();
        @Nullable Configuration config = configurations.findByName(annotationProcessor.value());
        if (config == null) {
            config = configurations.create(annotationProcessor.value());
        }
        return config;
    }

    /**
     * Get {@code annotationProcessor} configuration in the project.
     */
    @VisibleForTesting
    static Configuration in(Project project) {
        checkNotNull(project);
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration result = configurations.getByName(annotationProcessor.value());
        return result;
    }
}
