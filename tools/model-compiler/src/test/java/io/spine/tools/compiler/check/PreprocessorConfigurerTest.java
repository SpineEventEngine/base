/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.check;

import com.google.common.testing.NullPointerTester;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.compiler.check.PreprocessorConfigurer.PREPROCESSOR_ARG;
import static io.spine.tools.compiler.check.PreprocessorConfigurer.PREPROCESSOR_CONFIG_NAME;
import static io.spine.tools.compiler.check.given.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("PreprocessorConfigurer should")
class PreprocessorConfigurerTest {

    private Project project;
    private ConfigurationContainer projectConfigs;
    private Configuration preprocessorConfig;
    private PreprocessorConfigurer configurer;

    @BeforeEach
    void setUp() {
        project = newProject();
        projectConfigs = project.getConfigurations();
        preprocessorConfig = projectConfigs.getByName(PREPROCESSOR_CONFIG_NAME);
        configurer = PreprocessorConfigurer.initFor(project);
    }

    @Test
    @DisplayName("pass null tolerance check")
    void pass_null_tolerance_check() {
        new NullPointerTester().testAllPublicStaticMethods(PreprocessorConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(configurer);
    }

    @Test
    @DisplayName("return annotation processor config if it exists")
    void return_annotation_processor_config_if_it_exists() {
        Configuration returnedConfig = configurer.setupPreprocessorConfig();
        assertEquals(preprocessorConfig, returnedConfig);
    }

    @Test
    @DisplayName("create and return annotation processor config if it does not exist")
    void create_and_return_annotation_processor_config_if_it_does_not_exist() {
        projectConfigs.remove(preprocessorConfig);
        assertNull(projectConfigs.findByName(PREPROCESSOR_CONFIG_NAME));

        Configuration preprocessorConfig = configurer.setupPreprocessorConfig();
        Configuration foundConfig = projectConfigs.findByName(PREPROCESSOR_CONFIG_NAME);
        assertEquals(preprocessorConfig, foundConfig);
    }

    @Test
    @DisplayName("add configure preprocessor action")
    void add_configure_preprocessor_action() {
        configurer.addConfigurePreprocessorAction(preprocessorConfig);
        assertCompileTasksContain(project, PREPROCESSOR_ARG, preprocessorConfig.getAsPath());
    }
}
