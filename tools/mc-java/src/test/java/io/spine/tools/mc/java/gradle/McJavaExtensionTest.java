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
import io.spine.tools.java.fs.DefaultJavaPaths;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.MC_JAVA_GRADLE_PLUGIN_ID;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.newProject;
import static io.spine.tools.mc.java.gradle.given.ModelCompilerTestEnv.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`McJavaExtension` should")
class McJavaExtensionTest {

    private Project project;
    private File projectDir;

    @BeforeEach
    void setUp() {
        projectDir = TempDir.forClass(getClass());
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
            String dir = McJavaExtension.getGeneratedMainResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void setValue() {
            spineProtobuf().generatedMainResourcesDir = newUuid();

            String dir = McJavaExtension.getGeneratedMainResourcesDir(project);

            assertThat(dir)
                    .isEqualTo(spineProtobuf().generatedMainResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `testTargetGenResourcesDir` return")
    class TestTargetGenResourcesDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = McJavaExtension.getGeneratedTestResourcesDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().generatedTestResourcesDir = newUuid();

            String dir = McJavaExtension.getGeneratedTestResourcesDir(project);

            assertThat(dir)
                    .isEqualTo(spineProtobuf().generatedTestResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `mainDescriptorSetPath` return")
    class MainDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = McJavaExtension.getMainDescriptorSetFile(project);

            assertNotEmptyAndIsInProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().mainDescriptorSetFile = newUuid();

            File file = McJavaExtension.getMainDescriptorSetFile(project);

            assertThat(file.toString())
                    .isEqualTo(spineProtobuf().mainDescriptorSetFile);
        }
    }

    @Nested
    @DisplayName("for `testDescriptorSetPath` return")
    class TestDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = McJavaExtension.getTestDescriptorSetFile(project);

            assertNotEmptyAndIsInProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().testDescriptorSetFile = newUuid();

            File file = McJavaExtension.getTestDescriptorSetFile(project);

            assertThat(file.toString())
                    .isEqualTo(spineProtobuf().testDescriptorSetFile);
        }
    }

    @Nested
    @DisplayName("for `targetGenRejectionsRootDir` return")
    class TargetGenRejectionsRootDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = McJavaExtension.getGeneratedMainRejectionsDir(project);

            assertNotEmptyAndIsInProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            spineProtobuf().generatedMainRejectionsDir = newUuid();

            String dir = McJavaExtension.getGeneratedMainRejectionsDir(project);

            assertThat(dir)
                    .isEqualTo(spineProtobuf().generatedMainRejectionsDir);
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

                assertThat(actualDirs).hasSize(1);
                assertNotEmptyAndIsInProjectDir(actualDirs.get(0));
            }

            @Test
            @DisplayName("single value, if set")
            void singleValue() {
                spineProtobuf().dirToClean = newUuid();

                List<String> actualDirs = actualDirs();

                assertThat(actualDirs).hasSize(1);
                assertThat(actualDirs.get(0))
                        .isEqualTo(spineProtobuf().dirToClean);
            }

            @Test
            @DisplayName("list, if array is set")
            void list() {
                spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());

                List<String> actualDirs = actualDirs();

                assertThat(actualDirs)
                        .isEqualTo(spineProtobuf().dirsToClean);
            }

            @Test
            @DisplayName("list, if array and single are set")
            void listIfArrayAndSingle() {
                spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());
                spineProtobuf().dirToClean = newUuid();

                List<String> actualDirs = actualDirs();

                assertThat(actualDirs)
                        .isEqualTo(spineProtobuf().dirsToClean);
            }
        }

        @Test
        @DisplayName("include `.spine` dir, if exists")
        void includeSpineDir() throws IOException {
            DefaultJavaPaths defaultProject = DefaultJavaPaths.at(projectDir);
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
            return McJavaExtension.getDirsToClean(project);
        }
    }

    @Nested
    @DisplayName("for Spine checker return")
    class SpineChecker {

        @Test
        @DisplayName("severity, if set")
        void specifiedValue() {
            spineProtobuf().defaultCheckSeverity = Severity.ERROR;
            Severity actualSeverity = McJavaExtension.getSpineCheckSeverity(project);
            assertEquals(spineProtobuf().defaultCheckSeverity, actualSeverity);
        }

        @Test
        @DisplayName("`null`, if not set")
        void nullValue() {
            Severity actualSeverity = McJavaExtension.getSpineCheckSeverity(project);
            assertNull(actualSeverity);
        }
    }

    private void assertNotEmptyAndIsInProjectDir(String path) {
        assertFalse(path.trim()
                        .isEmpty());
        assertTrue(path.startsWith(project.getProjectDir()
                                          .getAbsolutePath()));
    }

    private McJavaExtension spineProtobuf() {
        return (McJavaExtension) project.getExtensions()
                                        .getByName(McJavaPlugin.extensionName());
    }
}
