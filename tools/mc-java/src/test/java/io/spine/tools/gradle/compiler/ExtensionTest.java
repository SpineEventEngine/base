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
package io.spine.tools.gradle.compiler;

import io.spine.tools.java.fs.DefaultJavaProject;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.MC_JAVA_GRADLE_PLUGIN_ID;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Extension` should")
class ExtensionTest {

    private Project project;
    private File projectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        projectDir = tempDirPath.toFile();
        project = newProject(projectDir);
        project.getPluginManager()
               .apply(MC_JAVA_GRADLE_PLUGIN_ID);
    }

    @Nested
    @DisplayName("for `mainTargetGenResourcesDir` return")
    class MainTargetGenResourceDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = Extension.getMainTargetGenResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void setValue() {
            spineProtobuf().mainTargetGenResourcesDir = newUuid();

            String dir = Extension.getMainTargetGenResourcesDir(project);

            assertEquals(spineProtobuf().mainTargetGenResourcesDir, dir);
        }
    }

    @Nested
    @DisplayName("for `testTargetGenResourcesDir` return")
    class TestTargetGenResourcesDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = Extension.getTestTargetGenResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().testTargetGenResourcesDir = newUuid();

            String dir = Extension.getTestTargetGenResourcesDir(project);

            assertEquals(spineProtobuf().testTargetGenResourcesDir, dir);
        }
    }

    @Nested
    @DisplayName("for `mainDescriptorSetPath` return")
    class MainDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = Extension.getMainDescriptorSet(project);

            assertNotEmptyAndIsInProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().mainDescriptorSetPath = newUuid();

            File file = Extension.getMainDescriptorSet(project);

            assertEquals(spineProtobuf().mainDescriptorSetPath, file.toString());
        }
    }

    @Nested
    @DisplayName("for `testDescriptorSetPath` return")
    class TestDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = Extension.getTestDescriptorSet(project);

            assertNotEmptyAndIsInProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().testDescriptorSetPath = newUuid();

            File file = Extension.getTestDescriptorSet(project);

            assertEquals(spineProtobuf().testDescriptorSetPath, file.toString());
        }
    }

    @Nested
    @DisplayName("for `targetGenRejectionsRootDir` return")
    class TargetGenRejectionsRootDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = Extension.getTargetGenRejectionsRootDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().targetGenRejectionsRootDir = newUuid();

            String dir = Extension.getTargetGenRejectionsRootDir(project);

            assertEquals(spineProtobuf().targetGenRejectionsRootDir, dir);
        }
    }

    @Nested
    @DisplayName("for `dirsToClean`")
    class DirsToClean {

        @Nested
        @DisplayName("return")
        class Return {
            
            @Test
            @DisplayName("default value, if not set")
            void defaultValue() {
                List<String> actualDirs = actualDirs();

                assertEquals(1, actualDirs.size());
                assertNotEmptyAndIsInProjectDir(actualDirs.get(0));
            }

            @Test
            @DisplayName("single value, if set")
            void singleValue() {
                spineProtobuf().dirToClean = newUuid();

                List<String> actualDirs = actualDirs();

                assertEquals(1, actualDirs.size());
                assertEquals(spineProtobuf().dirToClean, actualDirs.get(0));
            }

            @Test
            @DisplayName("list, if array is set")
            void list() {
                spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());

                List<String> actualDirs = actualDirs();

                assertEquals(spineProtobuf().dirsToClean, actualDirs);
            }

            @Test
            @DisplayName("list, if array and single are set")
            void listIfArrayAndSingle() {
                spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());
                spineProtobuf().dirToClean = newUuid();

                List<String> actualDirs = actualDirs();

                assertEquals(spineProtobuf().dirsToClean, actualDirs);
            }
        }

        @Test
        @DisplayName("include `.spine` dir, if exists")
        void includeSpineDir() throws IOException {
            DefaultJavaProject defaultProject = DefaultJavaProject.at(projectDir);
            File spineDir = defaultProject.tempArtifacts();
            assertTrue(spineDir.mkdir());
            String generatedDir =
                    defaultProject.generated()
                                  .path()
                                  .toFile()
                                  .getCanonicalPath();

            List<String> dirsToClean = actualDirs();

            assertThat(dirsToClean)
                 .containsAtLeast(spineDir.getCanonicalPath(), generatedDir);
        }

        private List<String> actualDirs() {
            return Extension.getDirsToClean(project);
        }
    }

    @Nested
    @DisplayName("for Spine checker return")
    class SpineChecker {

        @Test
        @DisplayName("severity, if set")
        void specifiedValue() {
            spineProtobuf().spineCheckSeverity = Severity.ERROR;
            Severity actualSeverity = Extension.getSpineCheckSeverity(project);
            assertEquals(spineProtobuf().spineCheckSeverity, actualSeverity);
        }

        @Test
        @DisplayName("`null`, if not set")
        void nullValue() {
            Severity actualSeverity = Extension.getSpineCheckSeverity(project);
            assertNull(actualSeverity);
        }
    }

    private void assertNotEmptyAndIsInProjectDir(String path) {
        assertFalse(path.trim()
                        .isEmpty());
        assertTrue(path.startsWith(project.getProjectDir()
                                          .getAbsolutePath()));
    }

    private Extension spineProtobuf() {
        return (Extension) project.getExtensions()
                                  .getByName(ModelCompilerPlugin.extensionName());
    }
}
