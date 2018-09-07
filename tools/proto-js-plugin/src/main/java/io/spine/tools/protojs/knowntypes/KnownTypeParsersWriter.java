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

package io.spine.tools.protojs.knowntypes;

import io.spine.tools.protojs.code.JsOutput;
import io.spine.tools.protojs.code.JsWriter;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;

public class KnownTypeParsersWriter {

    public static final String FILE_NAME = "known_type_parsers";
    private static final String JS_FILE_NAME = FILE_NAME + ".js";
    private static final String JS_RESOURCE_PATH =
            "io/spine/tools/protojs/knowntypes/" + JS_FILE_NAME;

    private final Path filePath;

    private KnownTypeParsersWriter(Path filePath) {
        this.filePath = filePath;
    }

    public static KnownTypeParsersWriter createFor(Project project) {
        Path filePath = composeFilePath(project);
        return new KnownTypeParsersWriter(filePath);
    }

    public void writeFile() {
        writeParsers();
        writeParserMap();
    }

    private void writeParsers() {
        try (InputStream in = KnownTypeParsersWriter.class
                .getClassLoader()
                .getResourceAsStream(JS_RESOURCE_PATH)) {
            Files.copy(in, filePath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // todo address "generate", "write", etc. naming
    private void writeParserMap() {
        int indent = 4;
        JsWriter jsWriter = new JsWriter(indent);
        jsWriter.addEmptyLine();
        ParserMapGenerator generator = new ParserMapGenerator(jsWriter);
        generator.generateParserMap();
        JsOutput generatedCode = jsWriter.getGeneratedCode();
        writeToFile(filePath, generatedCode);
    }

    private static Path composeFilePath(Project project) {
        File projectDir = project.getProjectDir();
        String absolutePath = projectDir.getAbsolutePath();
        Path filePath = Paths.get(absolutePath, "proto", "test", "js", JS_FILE_NAME);
        return filePath;
    }

    // todo remove copypaste
    private static void writeToFile(Path path, JsOutput output) {
        try {
            String content = output.toString();
            Files.write(path, content.getBytes(), APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
