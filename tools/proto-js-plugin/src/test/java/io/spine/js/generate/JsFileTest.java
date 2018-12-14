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
import io.spine.code.js.FileName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.io.Files.createTempDir;
import static io.spine.code.js.FileName.of;
import static io.spine.js.generate.JsFile.createFor;
import static io.spine.js.generate.given.FileWriters.assertFileContains;
import static io.spine.js.generate.given.FileWriters.assertFileNotContains;

@DisplayName("JsFile should")
class JsFileTest {

    private static final FileName TASKS_JS = of("tasks.js");
    private static final String CREATE_TASK_1 = "createTask1();";
    private static final String CREATE_TASK_2 = "createTask2();";

    private JsFile file;
    private Path filePath;

    @BeforeEach
    void setUp() {
        File tempDir = createTempDir();
        Directory directory = Directory.at(tempDir.toPath());
        file = createFor(directory, TASKS_JS);
        filePath = directory.resolve(TASKS_JS);
    }

    @Test
    @DisplayName("write `JsOutput` to new file")
    void writeToFile() throws IOException {
        JsOutput testLine1 = generateCode(CREATE_TASK_1);
        file.write(testLine1);
        assertFileContains(filePath, CREATE_TASK_1);
    }

    @Test
    @DisplayName("overwrite existing file")
    void overwriteExisting() throws IOException {
        JsOutput line1 = generateCode(CREATE_TASK_1);
        file.write(line1);

        JsOutput line2 = generateCode(CREATE_TASK_2);
        file.write(line2);

        assertFileNotContains(filePath, CREATE_TASK_1);
        assertFileContains(filePath, CREATE_TASK_2);
    }

    @Test
    @DisplayName("append `JsOutput` to existing file")
    void appendToFile() throws IOException {
        JsOutput line1 = generateCode(CREATE_TASK_1);
        file.write(line1);

        JsOutput line2 = generateCode(CREATE_TASK_2);
        file.append(line2);

        assertFileContains(filePath, CREATE_TASK_1);
        assertFileContains(filePath, CREATE_TASK_2);
    }

    private static JsOutput generateCode(String codeLine) {
        JsOutput jsOutput = new JsOutput();
        jsOutput.addLine(codeLine);
        return jsOutput;
    }
}
