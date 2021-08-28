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

import io.spine.testing.UtilityClassTest;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.ConfigurationName.annotationProcessor;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("`AnnotationProcessorConfiguration` should")
class AnnotationProcessorConfigurationTest
        extends UtilityClassTest<AnnotationProcessorConfiguration> {

    private static Project project = null;
    private static ConfigurationContainer configurations = null;
    private static Configuration preprocessorConfig = null;

    AnnotationProcessorConfigurationTest() {
        super(AnnotationProcessorConfiguration.class);
    }

    @BeforeAll
    static void createProject() {
        project = StubProject.createFor(AnnotationProcessorConfigurationTest.class).get();
        configurations = project.getConfigurations();
        preprocessorConfig = configurations.getByName(annotationProcessor.value());
    }

    @Test
    @DisplayName("create annotation processor config if it does not exist")
    void createAndReturnAnnotationProcessorConfigIfItDoesNotExist() {
        configurations.remove(preprocessorConfig);
        assertThat(configurations.findByName(annotationProcessor.value()))
                .isNull();

        Configuration cfg = AnnotationProcessorConfiguration.findOrCreateIn(project);
        Configuration found = configurations.findByName(annotationProcessor.value());
        assertThat(cfg)
                .isEqualTo(found);
    }
}
