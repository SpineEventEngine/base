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
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.tools.mc.java.gradle.Severity.ERROR;
import static io.spine.tools.mc.java.gradle.Severity.OFF;
import static io.spine.tools.mc.java.gradle.given.ProjectConfigurations.assertCompileTasksContain;
import static io.spine.tools.mc.java.gradle.given.ProjectConfigurations.assertCompileTasksEmpty;

/**
 * Tests {@link io.spine.tools.gradle.compiler.Severity}.
 */
@DisplayName("SeverityConfigurer should")
class McJavaChecksSeverityTest {

    private Project project;
    private McJavaChecksSeverity configurer;

    @BeforeEach
    void createProject() {
        project = StubProject.createFor(getClass()).get();
        configurer = McJavaChecksSeverity.initFor(project);
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void nullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(McJavaChecksSeverity.class);
        new NullPointerTester().testAllPublicInstanceMethods(configurer);
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    @DisplayName("configure check severity")
    void configureCheckSeverity() {
        configureModelCompilerExtension();
        McJavaChecksExtension extension = configureSpineCheckExtension();
        extension.useValidatingBuilderSeverity = ERROR;
        configurer.setHasErrorPronePlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @SuppressWarnings({"CheckReturnValue", "ResultOfMethodCallIgnored"})
    // We use one extension and just create the other one.
    @Test
    @DisplayName("configure check severity for all checks")
    void configureCheckSeverityForAllChecks() {
        McJavaExtension extension = configureModelCompilerExtension();
        extension.defaultCheckSeverity = ERROR;
        configureSpineCheckExtension();
        configurer.setHasErrorPronePlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("override ModelCompiler extension by ErrorProne checks extension")
    void overrideModelCompilerCheck() {
        McJavaExtension modelCompilerExtension = configureModelCompilerExtension();
        modelCompilerExtension.defaultCheckSeverity = OFF;
        McJavaChecksExtension modelChecksExtension = configureSpineCheckExtension();
        modelChecksExtension.useValidatingBuilderSeverity = ERROR;
        configurer.setHasErrorPronePlugin(true);
        configurer.addConfigureSeverityAction();
        checkSeverityConfiguredToError();
    }

    @Test
    @DisplayName("not add severity args if ErrorProne plugin not applied")
    void detectErrorProne() {
        configurer.setHasErrorPronePlugin(false);
        configurer.addConfigureSeverityAction();
        checkSeverityNotConfigured();
    }

    private McJavaChecksExtension configureSpineCheckExtension() {
        ExtensionContainer extensions = project.getExtensions();
        McJavaChecksExtension extension =
                extensions.create(McJavaChecksPlugin.extensionName(),
                                  McJavaChecksExtension.class);
        return extension;
    }

    private McJavaExtension configureModelCompilerExtension() {
        ExtensionContainer extensions = project.getExtensions();
        McJavaExtension extension =
                extensions.create(McJavaPlugin.extensionName(), McJavaExtension.class, project);
        return extension;
    }

    private void checkSeverityConfiguredToError() {
        assertCompileTasksContain(project, "-Xep:UseValidatingBuilder:ERROR");
    }

    private void checkSeverityNotConfigured() {
        assertCompileTasksEmpty(project);
    }
}
