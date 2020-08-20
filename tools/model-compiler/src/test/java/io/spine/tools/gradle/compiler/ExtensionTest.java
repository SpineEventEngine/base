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
package io.spine.tools.gradle.compiler;

import io.spine.code.fs.java.DefaultJavaProject;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.SPINE_PROTOBUF_PLUGIN_ID;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newProject;
import static io.spine.tools.gradle.compiler.given.ModelCompilerTestEnv.newUuid;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.io.TempDir;

@DisplayName("Extension should")
class ExtensionTest {

    private Project project;
    private File projectDir;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        projectDir = tempDirPath.toFile();
        project = newProject(projectDir);
        project.getPluginManager()
               .apply(SPINE_PROTOBUF_PLUGIN_ID);
    }

    @Test
    @DisplayName("return default mainTargetGenResourcesDir if not set")
    void return_default_mainTargetGenResourcesDir_if_not_set() {
        String dir = Extension.getMainTargetGenResourcesDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    @DisplayName("return mainTargetGenResourcesDir if set")
    void return_mainTargetGenResourcesDir_if_set() {

        spineProtobuf().mainTargetGenResourcesDir = newUuid();

        String dir = Extension.getMainTargetGenResourcesDir(project);

        assertEquals(spineProtobuf().mainTargetGenResourcesDir, dir);
    }

    @Test
    @DisplayName("return default testTargetGenResourcesDir if not set")
    void return_default_testTargetGenResourcesDir_if_not_set() {
        String dir = Extension.getTestTargetGenResourcesDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    @DisplayName("return testTargetGenResourcesDir if set")
    void return_testTargetGenResourcesDir_if_set() {
        spineProtobuf().testTargetGenResourcesDir = newUuid();

        String dir = Extension.getTestTargetGenResourcesDir(project);

        assertEquals(spineProtobuf().testTargetGenResourcesDir, dir);
    }

    @Test
    @DisplayName("return default mainDescriptorSetPath if not set")
    void return_default_mainDescriptorSetPath_if_not_set() {
        File file = Extension.getMainDescriptorSet(project);

        assertNotEmptyAndIsInProjectDir(file.toString());
    }

    @Test
    @DisplayName("return mainDescriptorSetPath if set")
    void return_mainDescriptorSetPath_if_set() {
        spineProtobuf().mainDescriptorSetPath = newUuid();

        File file = Extension.getMainDescriptorSet(project);

        assertEquals(spineProtobuf().mainDescriptorSetPath, file.toString());
    }

    @Test
    @DisplayName("return default testDescriptorSetPath if not set")
    void return_default_testDescriptorSetPath_if_not_set() {
        File file = Extension.getTestDescriptorSet(project);

        assertNotEmptyAndIsInProjectDir(file.toString());
    }

    @Test
    @DisplayName("return testDescriptorSetPath if set")
    void return_testDescriptorSetPath_if_set() {
        spineProtobuf().testDescriptorSetPath = newUuid();

        File file = Extension.getTestDescriptorSet(project);

        assertEquals(spineProtobuf().testDescriptorSetPath, file.toString());
    }

    @Test
    @DisplayName("return default targetGenRejectionsRootDir if not set")
    void return_default_targetGenRejectionsRootDir_if_not_set() {
        String dir = Extension.getTargetGenRejectionsRootDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    @DisplayName("return targetGenRejectionsRootDir if set")
    void return_targetGenRejectionsRootDir_if_set() {
        spineProtobuf().targetGenRejectionsRootDir = newUuid();

        String dir = Extension.getTargetGenRejectionsRootDir(project);

        assertEquals(spineProtobuf().targetGenRejectionsRootDir, dir);
    }

    @Test
    @DisplayName("return default dirsToClean if not set")
    void return_default_dirsToClean_if_not_set() {
        List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(1, actualDirs.size());
        assertNotEmptyAndIsInProjectDir(actualDirs.get(0));
    }

    @Test
    @DisplayName("return single dirsToClean if set")
    void return_single_dirToClean_if_set() {
        spineProtobuf().dirToClean = newUuid();

        List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(1, actualDirs.size());
        assertEquals(spineProtobuf().dirToClean, actualDirs.get(0));
    }

    @Test
    @DisplayName("return dirsToClean list if array is set")
    void return_dirsToClean_list_if_array_is_set() {
        spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());

        List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(spineProtobuf().dirsToClean, actualDirs);
    }

    @Test
    @DisplayName("return dirsToClean list if array and single are set")
    void return_dirsToClean_list_if_array_and_single_are_set() {
        spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());
        spineProtobuf().dirToClean = newUuid();

        List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(spineProtobuf().dirsToClean, actualDirs);
    }

    @Test
    @DisplayName("include spine dir in dirsToClean if exists")
    void include_spine_dir_in_dirsToClean_if_exists() throws IOException {
        DefaultJavaProject defaultProject = DefaultJavaProject.at(projectDir);
        File spineDir = defaultProject.tempArtifacts();
        assertTrue(spineDir.mkdir());
        String generatedDir = defaultProject.generated()
                                            .path()
                                            .toFile()
                                            .getCanonicalPath();

        List<String> dirsToClean = Extension.getDirsToClean(project);

        assertThat(dirsToClean,
                   containsInAnyOrder(spineDir.getCanonicalPath(), generatedDir)
        );
    }

    @Test
    @DisplayName("return spine checker severity")
    void return_spine_checker_severity() {
        spineProtobuf().spineCheckSeverity = Severity.ERROR;
        Severity actualSeverity = Extension.getSpineCheckSeverity(project);
        assertEquals(spineProtobuf().spineCheckSeverity, actualSeverity);
    }

    @Test
    @DisplayName("return null spine checker severity if not set")
    void return_null_spine_checker_severity_if_not_set() {
        Severity actualSeverity = Extension.getSpineCheckSeverity(project);
        assertNull(actualSeverity);
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
