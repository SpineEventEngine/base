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

package io.spine.tools.compiler.check;

import com.google.common.testing.NullPointerTester;
import io.spine.tools.gradle.compiler.ErrorProneChecksExtension;
import io.spine.tools.gradle.compiler.ErrorProneChecksPlugin;
import io.spine.tools.gradle.compiler.Extension;
import io.spine.tools.gradle.compiler.ModelCompilerPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.compiler.check.given.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.compiler.check.given.ProjectConfigurations.assertCompileTasksEmpty;
import static io.spine.tools.gradle.compiler.Severity.ERROR;
import static io.spine.tools.gradle.compiler.Severity.OFF;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;

/**
 * Tests {@link io.spine.tools.gradle.compiler.Severity}.
 */
@DisplayName("SeverityConfigurer should")
class SeverityConfigurerTest {

    private Project project;
    private SeverityConfigurer configurer;

    @BeforeEach
    void setUp() {
        project = newProject();
        configurer = SeverityConfigurer.initFor(project);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(SeverityConfigurer.class);
        new NullPointerTester().testAllPublicInstanceMethods(configurer);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    @DisplayName("configure check severity")
    void configureCheckSeverity() {
        configureModelCompilerExtension();
        ErrorProneChecksExtension extension = configureSpineCheckExtension();
        extension.useValidatingBuilder = ERROR;
        configurer.setHasErrorProneChecksPlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    @DisplayName("configure check severity for all checks")
    void configureCheckSeverityForAllChecks() {
        Extension extension = configureModelCompilerExtension();
        extension.spineCheckSeverity = ERROR;
        configureSpineCheckExtension();
        configurer.setHasErrorProneChecksPlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("override ModelCompiler extension by ErrorProne checks extension")
    void overrideModelCompilerCheck() {
        Extension modelCompilerExtension = configureModelCompilerExtension();
        modelCompilerExtension.spineCheckSeverity = OFF;
        ErrorProneChecksExtension errorProneChecksExtension = configureSpineCheckExtension();
        errorProneChecksExtension.useValidatingBuilder = ERROR;
        configurer.setHasErrorProneChecksPlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("not add severity args if ErrorProne plugin not applied")
    void detectErrorProne() {
        configurer.setHasErrorProneChecksPlugin(false);
        configurer.addConfigureSeverityAction();
        checkSeverityNotConfigured();
    }

    private ErrorProneChecksExtension configureSpineCheckExtension() {
        ExtensionContainer extensions = project.getExtensions();
        ErrorProneChecksExtension extension =
                extensions.create(ErrorProneChecksPlugin.extensionName(),
                                  ErrorProneChecksExtension.class);
        return extension;
    }

    private Extension configureModelCompilerExtension() {
        ExtensionContainer extensions = project.getExtensions();
        Extension extension =
                extensions.create(ModelCompilerPlugin.extensionName(), Extension.class);
        return extension;
    }

    private void checkSeverityConfiguredToError() {
        assertCompileTasksContain(project, "-Xep:UseValidatingBuilder:ERROR");
    }

    private void checkSeverityNotConfigured() {
        assertCompileTasksEmpty(project);
    }
}
