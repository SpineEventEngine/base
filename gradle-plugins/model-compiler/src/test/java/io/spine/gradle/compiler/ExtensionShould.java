/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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
package io.spine.gradle.compiler;

import org.gradle.api.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.spine.gradle.compiler.Given.SPINE_PROTOBUF_PLUGIN_ID;
import static io.spine.gradle.compiler.Given.newProject;
import static io.spine.gradle.compiler.Given.newUuid;
import static io.spine.gradle.compiler.ModelCompilerPlugin.SPINE_MODEL_COMPILER_EXTENSION_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Tymchenko
 */
public class ExtensionShould {

    private Project project;

    @Before
    public void setUp() {
        project = newProject();
        project.getPluginManager()
               .apply(SPINE_PROTOBUF_PLUGIN_ID);
    }

    @Test
    public void return_default_mainTargetGenResourcesDir_if_not_set() {
        final String dir = Extension.getMainTargetGenResourcesDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_mainTargetGenResourcesDir_if_set() {

        spineProtobuf().mainTargetGenResourcesDir = newUuid();

        final String dir = Extension.getMainTargetGenResourcesDir(project);

        assertEquals(spineProtobuf().mainTargetGenResourcesDir, dir);
    }

    @Test
    public void return_default_testTargetGenResourcesDir_if_not_set() {
        final String dir = Extension.getTestTargetGenResourcesDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_testTargetGenResourcesDir_if_set() {
        spineProtobuf().testTargetGenResourcesDir = newUuid();

        final String dir = Extension.getTestTargetGenResourcesDir(project);

        assertEquals(spineProtobuf().testTargetGenResourcesDir, dir);
    }

    @Test
    public void return_default_mainDescriptorSetPath_if_not_set() {
        final String dir = Extension.getMainDescriptorSetPath(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_mainDescriptorSetPath_if_set() {
        spineProtobuf().mainDescriptorSetPath = newUuid();

        final String dir = Extension.getMainDescriptorSetPath(project);

        assertEquals(spineProtobuf().mainDescriptorSetPath, dir);
    }

    @Test
    public void return_default_testDescriptorSetPath_if_not_set() {
        final String dir = Extension.getTestDescriptorSetPath(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_testDescriptorSetPath_if_set() {
        spineProtobuf().testDescriptorSetPath = newUuid();

        final String dir = Extension.getTestDescriptorSetPath(project);

        assertEquals(spineProtobuf().testDescriptorSetPath, dir);
    }

    @Test
    public void return_default_targetGenRejectionsRootDir_if_not_set() {
        final String dir = Extension.getTargetGenRejectionsRootDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_targetGenRejectionsRootDir_if_set() {
        spineProtobuf().targetGenRejectionsRootDir = newUuid();

        final String dir = Extension.getTargetGenRejectionsRootDir(project);

        assertEquals(spineProtobuf().targetGenRejectionsRootDir, dir);
    }

    @Test
    public void return_targetGenValidatorsRootDir_if_not_set(){
        final String dir = Extension.getTargetGenValidatorsRootDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_targetTestGenValidatorsRootDir_if_set() {
        spineProtobuf().targetTestGenVBuildersRootDir = newUuid();

        final String dir = Extension.getTargetTestGenValidatorsRootDir(project);

        assertEquals(spineProtobuf().targetTestGenVBuildersRootDir, dir);
    }

    @Test
    public void return_targetTestGenValidatorsRootDir_if_not_set(){
        final String dir = Extension.getTargetTestGenValidatorsRootDir(project);

        assertNotEmptyAndIsInProjectDir(dir);
    }

    @Test
    public void return_targetGenValidatorsRootDir_if_set() {
        spineProtobuf().targetGenVBuildersRootDir = newUuid();

        final String dir = Extension.getTargetGenValidatorsRootDir(project);

        assertEquals(spineProtobuf().targetGenVBuildersRootDir, dir);
    }

    @Test
    public void return_default_dirsToClean_if_not_set() {
        final List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(1, actualDirs.size());
        assertNotEmptyAndIsInProjectDir(actualDirs.get(0));
    }

    @Test
    public void return_single_dirToClean_if_set() {
        spineProtobuf().dirToClean = newUuid();

        final List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(1, actualDirs.size());
        assertEquals(spineProtobuf().dirToClean, actualDirs.get(0));
    }

    @Test
    public void return_dirsToClean_list_if_array_is_set() {
        spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());

        final List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(spineProtobuf().dirsToClean, actualDirs);
    }

    @Test
    public void return_dirsToClean_list_if_array_and_single_are_set() {
        spineProtobuf().dirsToClean = newArrayList(newUuid(), newUuid());
        spineProtobuf().dirToClean = newUuid();

        final List<String> actualDirs = Extension.getDirsToClean(project);

        assertEquals(spineProtobuf().dirsToClean, actualDirs);
    }

    private void assertNotEmptyAndIsInProjectDir(String path) {
        assertFalse(path.trim()
                        .isEmpty());
        assertTrue(path.startsWith(project.getProjectDir()
                                          .getAbsolutePath()));
    }

    private Extension spineProtobuf() {
        return (Extension) project.getExtensions()
                                  .getByName(SPINE_MODEL_COMPILER_EXTENSION_NAME);
    }
}
