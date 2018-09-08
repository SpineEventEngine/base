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

import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPE_PARSERS;
import static io.spine.tools.protojs.files.JsFiles.appendToFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class KnownTypeParsersWriter {

    private static final String RESOURCES_DIR = "io/spine/tools/protojs/knowntypes/";
    private static final String PARSERS_CODE = RESOURCES_DIR + KNOWN_TYPE_PARSERS;

    private final Path filePath;

    private KnownTypeParsersWriter(Path filePath) {
        this.filePath = filePath;
    }

    public static KnownTypeParsersWriter createFor(Path protoJsLocation) {
        checkNotNull(protoJsLocation);
        Path filePath = composeFilePath(protoJsLocation);
        return new KnownTypeParsersWriter(filePath);
    }

    public void writeFile() {
        writeParsersCode();
        writeParserMap();
    }

    private void writeParsersCode() {
        try (InputStream in = KnownTypeParsersWriter.class
                .getClassLoader()
                .getResourceAsStream(PARSERS_CODE)) {
            Files.copy(in, filePath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeParserMap() {
        int indent = 4;
        JsGenerator jsGenerator = new JsGenerator(indent);
        ParserMapGenerator generator = new ParserMapGenerator(jsGenerator);
        generator.generateJs();
        JsOutput generatedCode = jsGenerator.getGeneratedCode();
        appendToFile(filePath, generatedCode);
    }

    private static Path composeFilePath(Path protoJsLocation) {
        Path path = Paths.get(protoJsLocation.toString(), KNOWN_TYPE_PARSERS);
        return path;
    }
}
