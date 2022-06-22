/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.tools.gradle.compiler;

import com.google.common.collect.ImmutableMap;
import io.spine.tools.protoc.SpineProtocConfig;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.compiler.ModelCompilerPlugin.extensionName;

@DisplayName("`ProtocPluginConfiguration` should")
class ProtocPluginConfigurationTest {

    private static final String PLUGIN = "plugin";
    private File configFile;
    private File projectDir;

    @BeforeEach
    void setUp(@TempDir File tempDir, @TempDir File projectDir) {
        this.configFile = new File(tempDir, "config");
        this.projectDir = projectDir;
    }

    @Test
    @DisplayName("generate configuration file")
    void generate() throws IOException {
        Project project = ProjectBuilder
                .builder()
                .withProjectDir(projectDir)
                .build();
        project.apply(ImmutableMap.of(PLUGIN, "java"));
        Extension extension = new Extension();
        extension.generateValidatingBuilders = false;
        project.getExtensions().add(extensionName(), extension);

        ProtocPluginConfiguration configuration = ProtocPluginConfiguration.forProject(project);
        assertThat(configFile.exists()).isFalse();
        configuration.writeTo(configFile.toPath());
        assertThat(configFile.exists()).isTrue();

        try (FileInputStream fileContent = new FileInputStream(configFile)) {
            SpineProtocConfig actualConfig =
                    SpineProtocConfig.parseFrom(fileContent);
            assertThat(actualConfig.getSkipValidatingBuilders()).isTrue();
        }
    }
}
