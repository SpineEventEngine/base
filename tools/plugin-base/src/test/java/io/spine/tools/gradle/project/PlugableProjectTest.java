/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.testing.TempDir;
import io.spine.testing.logging.LogRecordSubject;
import io.spine.testing.logging.LoggingTest;
import io.spine.tools.gradle.GradlePlugin;
import io.spine.tools.gradle.PluginScript;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.testing.NullPointerTester.Visibility.PACKAGE;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.GradlePlugin.implementedIn;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PlugableProject should")
class PlugableProjectTest {

    private PlugableProject plugableProject;
    private Project project;

    @BeforeEach
    void setUp() {
        File tempDir = TempDir.forClass(getClass());
        project = ProjectBuilder
                .builder()
                .withName(PlugableProjectTest.class.getSimpleName())
                .withProjectDir(tempDir)
                .build();
        plugableProject = new PlugableProject(project);
    }

    @Test
    @DisplayName("not accept null arguments")
    void notAcceptNulls() {
        NullPointerTester tester = new NullPointerTester()
                .setDefault(GradlePlugin.class, implementedIn(JavaPlugin.class));
        tester.testAllPublicInstanceMethods(plugableProject);
        tester.testConstructors(PlugableProject.class, PACKAGE);
    }

    @Test
    @DisplayName("apply a requested plugin")
    void applyPlugin() {
        GradlePlugin<?> plugin = implementedIn(JavaPlugin.class);

        assertTrue(plugableProject.isNotApplied(plugin));
        assertFalse(plugableProject.isApplied(plugin));

        plugableProject.apply(plugin);

        assertFalse(plugableProject.isNotApplied(plugin));
        assertTrue(plugableProject.isApplied(plugin));
    }

    @Nested
    class LogOnDuplicate extends LoggingTest {

        private GradlePlugin<?> plugin;

        LogOnDuplicate() {
            super(PlugableProject.class, Logging.debugLevel());
        }

        @BeforeEach
        void setUp() {
            plugin = implementedIn(JavaPlugin.class);
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

            LogRecordSubject assertLogRecord = assertLog().record();
            assertLogRecord.isDebug();
            assertLogRecord.hasMessageThat()
                           .contains(plugin.className()
                                           .value());
        }
    }

    @Test
    @DisplayName("apply Gradle scripts from classpath")
    void applyPluginScript() {
        Resource resource = Resource.file("test-script.gradle", getClass().getClassLoader());
        plugableProject.apply(PluginScript.declaredIn(resource));
        Object success = project.getExtensions()
                                .getExtraProperties()
                                .get("success");
        assertThat(success).isEqualTo(true);
    }

    @Test
    @DisplayName("execute a given action if a plugin is present")
    void runIfPresent() {
        GradlePlugin<IdeaPlugin> plugin = implementedIn(IdeaPlugin.class);
        plugableProject.apply(plugin);
        AtomicBoolean run = new AtomicBoolean(false);
        plugableProject.with(plugin, idea -> {
            assertThat(idea)
                    .isNotNull();
            run.set(true);
        });
        assertThat(run.get())
                .isTrue();
    }

    @Test
    @DisplayName("execute a given action after a plugin is applied")
    void runWhenPresent() {
        GradlePlugin<IdeaPlugin> plugin = implementedIn(IdeaPlugin.class);
        AtomicBoolean run = new AtomicBoolean(false);
        plugableProject.with(plugin, idea -> {
            assertThat(idea)
                    .isNotNull();
            run.set(true);
        });
        assertThat(run.get())
                .isFalse();
        plugableProject.apply(plugin);
        assertThat(run.get())
                .isTrue();
    }
}
