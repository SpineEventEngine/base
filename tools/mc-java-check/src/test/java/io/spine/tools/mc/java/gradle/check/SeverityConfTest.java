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

package io.spine.tools.mc.java.gradle.check;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.DisplayNames;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.gradle.testing.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.gradle.testing.ProjectConfigurations.assertCompileTasksEmpty;
import static io.spine.tools.mc.java.gradle.check.Severity.ERROR;
import static io.spine.tools.mc.java.gradle.check.Severity.OFF;
import static io.spine.tools.mc.java.gradle.check.given.ChecksTestEnv.newProject;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link Severity}.
 */
@DisplayName("`SeverityConfig` should")
class SeverityConfTest {

    private Project project;
    private Extension extension;
    private SeverityConf config;

    @BeforeEach
    void setUp() {
        project = newProject();
        extension = configureSpineCheckExtension();
        config = SeverityConf.initFor(project);
    }

    @Test
    @DisplayName(DisplayNames.NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(SeverityConf.class);
        new NullPointerTester().testAllPublicInstanceMethods(config);
    }

    @Test
    @DisplayName("configure check severity")
    void configureCheckSeverity() {
        extension.useValidatingBuilder = ERROR;
        config.setHasErrorProneChecksPlugin(true);
        config.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("configure check severity for all checks")
    void configureCheckSeverityForAllChecks() {
        extension.defaultSeverity = ERROR;
        config.setHasErrorProneChecksPlugin(true);
        config.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("override ModelCompiler extension by ErrorProne checks extension")
    void overrideModelCompilerCheck() {
        extension.defaultSeverity = OFF;
        extension.useValidatingBuilder = ERROR;
        config.setHasErrorProneChecksPlugin(true);
        config.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("not add severity args if ErrorProne plugin not applied")
    void detectErrorProne() {
        config.setHasErrorProneChecksPlugin(false);
        config.addConfigureSeverityAction();
        checkSeverityNotConfigured();
    }

    private Extension configureSpineCheckExtension() {
        ExtensionContainer extensions = project.getExtensions();
        Extension extension =
                extensions.create(Extension.name(),
                                  Extension.class);
        return extension;
    }

    @Test
    @DisplayName("Do not set default severity level automatically")
    void noDefaultSeverityLevel() {
        assertNull(extension.defaultSeverity);
    }

    private void checkSeverityConfiguredToError() {
        assertCompileTasksContain(project, "-Xep:UseValidatingBuilder:ERROR");
    }

    private void checkSeverityNotConfigured() {
        assertCompileTasksEmpty(project);
    }
}
