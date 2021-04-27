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

import io.spine.tools.java.fs.DefaultJavaPaths;
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
import static io.spine.testing.TestValues.randomString;
import static io.spine.tools.gradle.testing.Project.newProject;
import static io.spine.tools.mc.java.gradle.Extension.dirsToCleanIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Extension` should")
class ExtensionTest {

    private Project project;
    private File projectDir;
    private Extension extension;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        projectDir = tempDirPath.toFile();
        project = newProject(projectDir).get();
        project.getPluginManager()
               .apply(ModelCompilerPlugin.id());
        extension = Extension.of(project);
    }

    @Nested
    @DisplayName("for `mainTargetGenResourcesDir` return")
    class MainTargetGenResourceDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = extension.generatedMainResourcesDir();

            assertUnderProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void setValue() {
            extension.generatedMainResourcesDir = randomString();

            assertThat(extension.generatedMainResourcesDir())
                    .isEqualTo(extension.generatedMainResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `testTargetGenResourcesDir` return")
    class TestTargetGenResourcesDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = extension.generatedTestResourcesDir();

            assertUnderProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.generatedTestResourcesDir = randomString();

            assertThat(extension.generatedTestResourcesDir())
                    .isEqualTo(extension.generatedTestResourcesDir);
        }
    }

    @Nested
    @DisplayName("for `mainDescriptorSetPath` return")
    class MainDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = extension.mainDescriptorSetFile();

            assertUnderProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.mainDescriptorSetFile = randomString();

            assertThat(extension.mainDescriptorSetFile().toString())
                    .isEqualTo(extension.mainDescriptorSetFile);
        }
    }

    @Nested
    @DisplayName("for `testDescriptorSetPath` return")
    class TestDescriptorSetPath {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            File file = extension.testDescriptorSetFile();

            assertUnderProjectDir(file.toString());
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.testDescriptorSetFile = randomString();

            assertThat(extension.testDescriptorSetFile().toString())
                    .isEqualTo(extension.testDescriptorSetFile);
        }
    }

    @Nested
    @DisplayName("for `targetGenRejectionsRootDir` return")
    class TargetGenRejectionsRootDir {

        @Test
        @DisplayName("default value, if not set")
        void defaultValue() {
            String dir = extension.generatedMainRejectionsDir();

            assertUnderProjectDir(dir);
        }

        @Test
        @DisplayName("specified value, if set")
        void specifiedValue() {
            extension.generatedMainRejectionsDir = randomString();

            assertThat(extension.generatedMainRejectionsDir())
                    .isEqualTo(extension.generatedMainRejectionsDir);
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
                List<String> actualDirs = dirsToCleanIn(project);

                assertEquals(1, actualDirs.size());
                assertUnderProjectDir(actualDirs.get(0));
            }

            @Test
            @DisplayName("single value, if set")
            void singleValue() {
                extension.dirToClean = randomString();

                assertThat(dirsToCleanIn(project))
                        .containsExactly(extension.dirToClean);
            }

            @Test
            @DisplayName("list, if array is set")
            void list() {
                extension.dirsToClean = newArrayList(randomString(), randomString());

                assertThat(dirsToCleanIn(project))
                        .containsExactlyElementsIn(extension.dirsToClean);
            }

            @Test
            @DisplayName("list, if array and single are set")
            void listIfArrayAndSingle() {
                extension.dirsToClean = newArrayList(randomString(), randomString());
                extension.dirToClean = randomString();

                assertThat(dirsToCleanIn(project))
                        .containsExactlyElementsIn(extension.dirsToClean);
            }
        }

        @Test
        @DisplayName("include `.spine` dir, if exists")
        void includeSpineDir() throws IOException {
            DefaultJavaPaths defaultProject = DefaultJavaPaths.at(projectDir);
            File spineDir = defaultProject.tempArtifacts().path().toFile();
            assertTrue(spineDir.mkdir());
            String generatedDir =
                    defaultProject.generated()
                                  .path()
                                  .toFile()
                                  .getCanonicalPath();

            assertThat(dirsToCleanIn(project))
                 .containsAtLeast(spineDir.getCanonicalPath(), generatedDir);
        }
    }

    private void assertUnderProjectDir(String path) {
        assertThat(path)
                .isNotEmpty();
        assertThat(path)
                .startsWith(project.getProjectDir().getAbsolutePath());
    }
}
