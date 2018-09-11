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

package io.spine.tools.protojs.files;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.testing.UtilityClassTest;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.io.Files.createTempDir;
import static io.spine.tools.protojs.given.Given.file;
import static io.spine.tools.protojs.given.Writers.assertFileContains;
import static io.spine.tools.protojs.given.Writers.assertFileNotContains;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JsFiles utility should")
class JsFilesTest extends UtilityClassTest<JsFiles> {

    private static final String TEST_FILE_JS = "test_file.js";
    private static final String TEST_LINE_1 = "test line 1";
    private static final String TEST_LINE_2 = "test line 2";

    JsFilesTest() {
        super(JsFiles.class);
    }

    @Test
    @DisplayName("write `JsOutput` to new file")
    void writeToFile() throws IOException {
        String tempDirPath = createTempDir().getAbsolutePath();
        Path path = Paths.get(tempDirPath, TEST_FILE_JS);

        JsOutput testLine1 = generateCode(TEST_LINE_1);
        JsFiles.writeToFile(path, testLine1);

        assertFileContains(path, TEST_LINE_1);
    }

    @Test
    @DisplayName("overwrite existing file")
    void overwriteExisting() throws IOException {
        String tempDirPath = createTempDir().getAbsolutePath();
        Path path = Paths.get(tempDirPath, TEST_FILE_JS);

        JsOutput testLine1 = generateCode(TEST_LINE_1);
        JsFiles.writeToFile(path, testLine1);

        JsOutput testLine2 = generateCode(TEST_LINE_2);
        JsFiles.writeToFile(path, testLine2);

        assertFileNotContains(path, TEST_LINE_1);
        assertFileContains(path, TEST_LINE_2);
    }

    @Test
    @DisplayName("append `JsOutput` to existing file")
    void appendToFile() throws IOException {
        String tempDirPath = createTempDir().getAbsolutePath();
        Path path = Paths.get(tempDirPath, TEST_FILE_JS);

        JsOutput testLine1 = generateCode(TEST_LINE_1);
        JsFiles.writeToFile(path, testLine1);

        JsOutput testLine2 = generateCode(TEST_LINE_2);
        JsFiles.appendToFile(path, testLine2);

        assertFileContains(path, TEST_LINE_1);
        assertFileContains(path, TEST_LINE_2);
    }

    @Test
    @DisplayName("return JS file name for the `FileDescriptor`")
    void getJsFileName() {
        FileDescriptor file = file();
        String jsFileName = JsFiles.jsFileName(file);
        String nameWithoutExtension = FileName.from(file)
                                              .nameWithoutExtension();
        String expected = nameWithoutExtension + "_pb.js";
        assertEquals(expected, jsFileName);
    }

    private static JsOutput generateCode(String lineOfCode) {
        JsGenerator jsGenerator = new JsGenerator();
        jsGenerator.addLine(lineOfCode);
        return jsGenerator.getGeneratedCode();
    }
}
