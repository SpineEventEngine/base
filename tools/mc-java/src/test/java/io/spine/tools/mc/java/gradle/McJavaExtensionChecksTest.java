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

import io.spine.testing.TempDir;
import io.spine.tools.mc.java.gradle.given.StubProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.spine.tools.mc.java.gradle.McJavaExtension.getSpineCheckSeverity;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.MC_JAVA_GRADLE_PLUGIN_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("`McJavaExtension` for checks return")
class McJavaExtensionChecksTest {

    private Project project = null;

    @BeforeEach
    void setUp() {
        File projectDir = TempDir.forClass(McJavaExtensionChecksTest.class);
        project = StubProject.createAt(projectDir);
        RepositoryHandler repositories = project.getRepositories();
        repositories.mavenLocal();
        repositories.mavenCentral();
        project.getPluginManager()
               .apply(MC_JAVA_GRADLE_PLUGIN_ID);
    }

    @Test
    @DisplayName("severity, if set")
    void specifiedValue() {
        spineProtobuf().defaultCheckSeverity = Severity.ERROR;
        Severity actualSeverity = getSpineCheckSeverity(project);
        assertEquals(spineProtobuf().defaultCheckSeverity, actualSeverity);
    }

    @Test
    @DisplayName("`null`, if not set")
    void nullValue() {
        Severity actualSeverity = getSpineCheckSeverity(project);
        assertNull(actualSeverity);
    }

    private McJavaExtension spineProtobuf() {
        return (McJavaExtension) project.getExtensions()
                                        .getByName(McJavaPlugin.extensionName());
    }
}
