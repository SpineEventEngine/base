/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.code.js.Directory;
import io.spine.code.proto.FileSet;
import io.spine.js.generate.given.TestGenerationTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static io.spine.js.generate.given.GivenProject.mainFileSet;
import static io.spine.js.generate.given.GivenProject.mainProtoSources;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        File nonExistentDescriptors = new File(MISSING_PATH);
        FileSet emptyFileSet = FileSet.parseOrEmpty(nonExistentDescriptors);
        TestGenerationTask task = new TestGenerationTask(mainProtoSources());
        assertNotPerformed(task, emptyFileSet);
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
        assertEquals(expectedToBePerformed, task.isSourcesProcessed());
    }
}
