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

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.newProject;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This test contains very basic scenarios of the plugin usage.
 *
 * <p>For the tests of actual plugin functionality, see {@code io.spine.tools.check} test suites
 * from this module.
 */
@DisplayName("ErrorProneChecksPlugin should")
class ErrorProneChecksPluginTest {

    @Test
    @DisplayName("create Spine check extension")
    void create_spine_check_extension() {
        Project project = newProject();
        project.getPluginManager()
               .apply(ErrorProneChecksPlugin.class);
        ExtensionContainer extensions = project.getExtensions();
        Object found = extensions.findByName(ErrorProneChecksPlugin.extensionName());
        assertNotNull(found);
    }

    @Test
    @DisplayName("apply to empty project")
    void apply_to_empty_project_without_exceptions() {
        Project project = newProject();
        project.getPluginManager()
               .apply(ErrorProneChecksPlugin.class);
    }
}
