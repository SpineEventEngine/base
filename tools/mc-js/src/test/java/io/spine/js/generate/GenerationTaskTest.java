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

package io.spine.js.generate;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.fs.js.Directory;
import io.spine.code.proto.FileDescriptors;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.given.TestGenerationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.js.generate.given.GivenProject.mainFileSet;
import static io.spine.js.generate.given.GivenProject.mainProtoSources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerationTask should")
class GenerationTaskTest {

    private static final String MISSING_PATH = "non-existent";

    @Test
    @DisplayName("check if there are files to process")
    void checkFilesToProcess() {
        TestGenerationTask task = new TestGenerationTask(mainProtoSources());
        assertPerformed(task, mainFileSet());
    }

    @Test
    @DisplayName("recognize there are no generated files to process")
    void recognizeThereAreNoFiles() {
        Directory nonExistentRoot = Directory.at(Paths.get(MISSING_PATH));
        TestGenerationTask task = new TestGenerationTask(nonExistentRoot);
        assertNotPerformed(task, mainFileSet());
    }

    @Test
    @DisplayName("recognize there are no known types to process")
    void recognizeThereAreNoTypes() {
        FileSet emptyFileSet = FileSet.of(ImmutableSet.of());
        TestGenerationTask task = new TestGenerationTask(mainProtoSources());
        assertNotPerformed(task, emptyFileSet);
    }

    @Test
    @DisplayName("process files compiled to JavaScript")
    void processCompiledJsFiles() {
        TestGenerationTask task = new TestGenerationTask(mainProtoSources());
        FileSet passedFiles = mainFileSet();
        task.performFor(passedFiles);
        FileSet processedFiles = task.processedFileSet();
        // It is expected that standard Protobuf types won't be generated (see test build script).
        Collection<FileDescriptor> expectedFilteredFiles = passedFiles
                .filter(FileDescriptors::isGoogle)
                .files();
        int expectedProcessedFiles = passedFiles.size() - expectedFilteredFiles.size();
        assertThat(processedFiles.size()).isEqualTo(expectedProcessedFiles);
    }

    @Test
    @DisplayName("skip files not compiled to JavaScript")
    void skipNotCompiledJsFiles(@TempDir Path tempDir) {
        Directory emptyDirectory = Directory.at(tempDir);
        TestGenerationTask task = new TestGenerationTask(emptyDirectory);
        FileSet passedFiles = mainFileSet();
        // Check the file set is originally not empty.
        assertFalse(passedFiles.isEmpty());
        // Check all passed files were filtered out since they were not compiled to JS.
        assertNotPerformed(task, passedFiles);
        assertTrue(task.areFilesFiltered());
    }

    private static void assertPerformed(TestGenerationTask task, FileSet fileSet) {
        assertPerformed(task, fileSet, true);
    }

    private static void assertNotPerformed(TestGenerationTask task, FileSet fileSet) {
        assertPerformed(task, fileSet, false);
    }

    private static void assertPerformed(TestGenerationTask task,
                                        FileSet fileSet,
                                        boolean expectedToBePerformed) {
        task.performFor(fileSet);
        assertEquals(expectedToBePerformed, task.areSourcesProcessed());
    }
}
