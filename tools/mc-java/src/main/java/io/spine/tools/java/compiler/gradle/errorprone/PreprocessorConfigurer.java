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

package io.spine.tools.java.compiler.gradle.errorprone;

import io.spine.tools.gradle.ConfigurationName;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;

/**
 * A helper that sets up and configures the preprocessor dependency for the {@link Project}.
 */
public final class PreprocessorConfigurer {

    private final Project project;

    private PreprocessorConfigurer(Project project) {
        this.project = project;
    }

    /**
     * Create the {@code PreprocessorConfigurer} instance for the given project.
     *
     * @param project the project
     * @return the {@code PreprocessorConfigurer} instance
     */
    public static PreprocessorConfigurer initFor(Project project) {
        checkNotNull(project);
        return new PreprocessorConfigurer(project);
    }

    /**
     * Creates the {@code annotationProcessor} config for the project if it does not exist.
     *
     * <p>In the newer Gradle versions ({@code 4.6} and above) the config most probably already
     * exists.
     *
     * @return the {@code annotationProcessor} configuration of the project
     */
    public Configuration setupPreprocessorConfig() {
        ConfigurationContainer configurations = project.getConfigurations();
        ConfigurationName config = annotationProcessor;
        Configuration preprocessorConfig = configurations.findByName(config.value());
        if (preprocessorConfig == null) {
            preprocessorConfig = configurations.create(config.value());
        }
        return preprocessorConfig;
    }
}
