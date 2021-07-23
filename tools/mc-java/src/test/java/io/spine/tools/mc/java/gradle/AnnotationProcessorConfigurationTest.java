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

import com.google.common.testing.NullPointerTester;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("`PreprocessorConfig` should")
class AnnotationProcessorConfigurationTest
        extends UtilityClassTest<AnnotationProcessorConfiguration> {

    private Project project;
    private ConfigurationContainer projectConfigs;
    private Configuration preprocessorConfig;

    AnnotationProcessorConfigurationTest() {
        super(AnnotationProcessorConfiguration.class);
    }

    @BeforeEach
    void setUp() {
        project = StubProject.createFor(getClass()).get();
        projectConfigs = project.getConfigurations();
        preprocessorConfig = projectConfigs.getByName(annotationProcessor.value());
    }

    @Test
    @DisplayName("return annotation processor config if it exists")
    void returnAnnotationProcessorConfigIfItExists() {
        Configuration returnedConfig = AnnotationProcessorConfiguration.findOrCreateIn(project);
        assertEquals(preprocessorConfig, returnedConfig);
    }

    @Test
    @DisplayName("create and return annotation processor config if it does not exist")
    void createAndReturnAnnotationProcessorConfigIfItDoesNotExist() {
        projectConfigs.remove(preprocessorConfig);
        assertNull(projectConfigs.findByName(annotationProcessor.value()));

        Configuration cfg = AnnotationProcessorConfiguration.findOrCreateIn(project);
        Configuration found = projectConfigs.findByName(annotationProcessor.value());
        assertThat(cfg)
                .isEqualTo(found);
    }
}