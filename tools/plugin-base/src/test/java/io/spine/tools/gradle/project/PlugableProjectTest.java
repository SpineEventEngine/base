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

package io.spine.tools.gradle.project;

import com.google.common.testing.NullPointerTester;
import io.spine.io.Resource;
import io.spine.logging.Logging;
import io.spine.testing.logging.LogRecordSubject;
import io.spine.testing.logging.LoggingTest;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.nio.file.Path;

import static com.google.common.testing.NullPointerTester.Visibility.PACKAGE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("PlugableProject should")
class PlugableProjectTest {

    private PlugableProject plugableProject;
    private Project project;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        project = ProjectBuilder
                .builder()
                .withName(PlugableProjectTest.class.getSimpleName())
                .withProjectDir(dir.toFile())
                .build();
        plugableProject = new PlugableProject(project);
    }

    @Test
    @DisplayName("not accept null arguments")
    void notAcceptNulls() {
        NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicInstanceMethods(plugableProject);
        tester.testConstructors(PlugableProject.class, PACKAGE);
    }

    @Test
    @DisplayName("apply a requested plugin")
    void applyPlugin() {
        GradlePlugin plugin = GradlePlugin.implementedIn(JavaPlugin.class);

        assertTrue(plugableProject.isNotApplied(plugin));
        assertFalse(plugableProject.isApplied(plugin));

        plugableProject.apply(plugin);

        assertFalse(plugableProject.isNotApplied(plugin));
        assertTrue(plugableProject.isApplied(plugin));
    }

    @Nested
    class LogOnDuplicate extends LoggingTest {

        private GradlePlugin plugin;

        LogOnDuplicate() {
            super(PlugableProject.class, Logging.debugLevel());
        }

        @BeforeEach
        void setUp() {
            plugin = GradlePlugin.implementedIn(JavaPlugin.class);
            applyPlugin();
            interceptLogging();
        }

        private void applyPlugin() {
            assertFalse(plugableProject.isApplied(plugin));
            plugableProject.apply(plugin);
            assertTrue(plugableProject.isApplied(plugin));
        }

        @AfterEach
        void restoreLogger() {
            restoreLogging();
        }

        @Test
        @DisplayName("log if a plugin is applied twice")
        void appliedTwice() {
            plugableProject.apply(plugin);
            assertTrue(plugableProject.isApplied(plugin));

            LogRecordSubject assertLogRecord = handler().assertRecord();
            assertLogRecord.isDebug();
            assertLogRecord.hasMessageThat()
                           .contains(plugin.className().value());
        }
    }

    @Test
    @DisplayName("apply Gradle scripts from classpath")
    void applyPluginScript() {
        plugableProject.apply(PluginScript.declaredIn(Resource.file("test-script.gradle")));
        Object success = project.getExtensions()
                                .getExtraProperties()
                                .get("success");
        assertThat(success).isEqualTo(true);
    }
}
