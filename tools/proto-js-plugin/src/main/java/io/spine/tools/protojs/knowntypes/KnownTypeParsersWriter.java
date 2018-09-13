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

import com.google.common.annotations.VisibleForTesting;
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

/**
 * A class that generates and writes the JS file containing JSON parsers for the standard Protobuf
 * types.
 *
 * <p>The file receives the name {@code "known_type_parsers.js"}.
 *
 * <p>The file contains the global JS {@code Map} of the
 * <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">standard Protobuf
 * type</a> parsers as well as their source code.
 *
 * <p>The parsers are stored in the map in the
 * "{@linkplain io.spine.type.TypeUrl type-url}-to-parser" format. The acquired parser then can be
 * used to parse the JSON value via the {@code parse(value)} method.
 *
 * @author Dmytro Kuzmin
 * @see ParserMapGenerator
 */
public final class KnownTypeParsersWriter {

    /**
     * The package under which the resource containing known type parsers code lays.
     */
    private static final String PARSERS_PACKAGE = "io/spine/tools/protojs/knowntypes/";

    /**
     * The path to the {@code known_type_parsers.js} resource which contains the parser JS
     * definitions.
     */
    private static final String PARSERS_CODE = PARSERS_PACKAGE + KNOWN_TYPE_PARSERS;

    /**
     * The indent for the generated code.
     */
    private static final int INDENT = 4;

    /**
     * The path of the recorded {@code known_type_parsers.js} file.
     */
    private final Path filePath;

    private KnownTypeParsersWriter(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates the {@code KnownTypeParsersWriter} instance for the specified
     * {@code protoJsLocation}.
     *
     * <p>The location is used by this class only as a root folder for the stored
     * {@code known_type_parsers.js}.
     *
     * @param protoJsLocation
     *         the JS proto definitions root folder
     * @return a new instance of {@code KnownTypeParsersWriter}
     */
    public static KnownTypeParsersWriter createFor(Path protoJsLocation) {
        checkNotNull(protoJsLocation);
        Path filePath = composeFilePath(protoJsLocation);
        return new KnownTypeParsersWriter(filePath);
    }

    /**
     * Writes the contents of the {@code known_type_parsers.js} to the file.
     *
     * <p>This method performs 2 steps:
     * <ol>
     *     <li>Copy the parser definitions from the {@code known_type_parsers.js} resource.
     *     <li>Generate and append the known type parsers map to the file.
     * </ol>
     *
     * @throws IllegalStateException
     *         if the {@code known_type_parsers.js} resource cannot be located or something goes
     *         wrong with writing the file
     */
    public void writeFile() {
        copyParsersCode();
        writeParserMap();
    }

    /**
     * Copies the {@code known_type_parsers.js} resource content and stores it in the target file.
     *
     * <p>Possible {@link IOException} when copying the resource is wrapped as the
     * {@link IllegalStateException}.
     */
    private void copyParsersCode() {
        try (InputStream in = KnownTypeParsersWriter.class
                .getClassLoader()
                .getResourceAsStream(PARSERS_CODE)) {
            Files.copy(in, filePath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates and writes known type parsers map to the file.
     */
    private void writeParserMap() {
        JsOutput jsOutput = new JsOutput(INDENT);
        ParserMapGenerator generator = new ParserMapGenerator(jsOutput);
        generator.generateJs();
        appendToFile(filePath, jsOutput);
    }

    /**
     * Composes the file path for the {@code known_type_parsers.js} file.
     */
    @VisibleForTesting
    static Path composeFilePath(Path protoJsLocation) {
        Path path = Paths.get(protoJsLocation.toString(), KNOWN_TYPE_PARSERS);
        return path;
    }
}
