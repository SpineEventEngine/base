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

package io.spine.js.generate.output;

import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.js.generate.TaskProto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.spine.js.generate.given.FileWriters.assertFileContains;
import static io.spine.js.generate.given.FileWriters.assertFileNotContains;
import static io.spine.js.generate.output.FileWriter.createFor;

@DisplayName("FileWriter should")
class FileWriterTest {

    private static final FileName TASKS_JS = FileName.from(TaskProto.getDescriptor()
                                                                    .getFile());
    private static final String CREATE_TASK_1 = "createTask1();";
    private static final String CREATE_TASK_2 = "createTask2();";

    private FileWriter writer;
    private Path filePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        Directory directory = Directory.at(tempDir);
        writer = createFor(directory, TASKS_JS);
        filePath = directory.resolve(TASKS_JS);
        Files.createDirectories(filePath.getParent());
    }

    @Test
    @DisplayName("write code lines to new file")
    void writeToFile() throws IOException {
        CodeLines testLine1 = generateCode(CREATE_TASK_1);
        writer.write(testLine1);
        assertFileContains(filePath, CREATE_TASK_1);
    }

    @Test
    @DisplayName("overwrite existing file")
    void overwriteExisting() throws IOException {
        CodeLines line1 = generateCode(CREATE_TASK_1);
        writer.write(line1);

        CodeLines line2 = generateCode(CREATE_TASK_2);
        writer.write(line2);

        assertFileNotContains(filePath, CREATE_TASK_1);
        assertFileContains(filePath, CREATE_TASK_2);
    }

    @Test
    @DisplayName("append code lines to existing file")
    void appendToFile() throws IOException {
        CodeLines line1 = generateCode(CREATE_TASK_1);
        writer.write(line1);

        CodeLines line2 = generateCode(CREATE_TASK_2);
        writer.append(line2);

        assertFileContains(filePath, CREATE_TASK_1);
        assertFileContains(filePath, CREATE_TASK_2);
    }

    private static CodeLines generateCode(String codeLine) {
        CodeLines jsOutput = new CodeLines();
        jsOutput.append(codeLine);
        return jsOutput;
    }
}
