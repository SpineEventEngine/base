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

package io.spine.tools.protojs;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.FileWriter;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.file.FileGenerator;
import io.spine.generate.JsOutput;
import io.spine.generate.KnownTypesGenerator;
import io.spine.generate.ParserMapGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.spine.code.js.FileName.spineOptionsJs;
import static io.spine.code.proto.FileSet.parseOrEmpty;
import static io.spine.code.proto.ProtoPackage.GOOGLE_PROTOBUF_PACKAGE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * The JSON parsers writer used by the {@link ProtoJsPlugin}.
 *
 * <p>This class generates JS code to parse JS proto definitions from JSON and writes it to the
 * project files, more specifically:
 * <ol>
 *     <li>Writes all known types to the {@code known_types.js} file in proto JS location root.
 *         The types are stored in a global {@code Map} in the
 *         "{@linkplain io.spine.type.TypeUrl type-url}-to-JS-type" format.
 *     <li>Writes all standard type parsers to the {@code known_type_parsers.js} file in Proto
 *         JS location root. The parsers are stored in a global {@code Map} in the
 *         "type-url-to-parser" format.
 *     <li>Appends {@code fromJson(json)} method to all proto JS files, one for each message stored
 *         in a file.
 * </ol>
 *
 * @author Dmytro Kuzmin
 */
final class JsonParsersWriter {

    /**
     * The path to the {@code known_type_parsers} resource which contains the parser definitions.
     */
    private static final String PARSERS_RESOURCE =
            "io/spine/tools/protojs/knowntypes/known_type_parsers";

    private final Path knownTypes;
    private final Path knownTypeParsers;
    private final Directory generatedProtoDir;
    private final FileSet fileSet;

    private JsonParsersWriter(Builder builder) {
        this.knownTypes = builder.knownTypes;
        this.knownTypeParsers = builder.knownTypeParsers;
        this.generatedProtoDir = builder.generatedProtoDir;
        this.fileSet = parseOrEmpty(builder.descriptorSetFile);
    }

    /**
     * Generates and writes the JS code necessary to parse proto JS messages from the JSON format.
     */
    void write() {
        if (!hasFilesToProcess()) {
            return;
        }
        writeKnownTypes();
        writeKnownTypeParsers();
        writeFromJsonMethod();
    }

    /**
     * Checks if {@code ProtoJsonWriter}'s {@link FileSet} has any known types to process.
     *
     * @return {@code true} if the file set has files to process and {@code false} otherwise
     */
    private boolean hasFilesToProcess() {
        boolean hasFilesToProcess = !fileSet.isEmpty();
        return hasFilesToProcess;
    }

    /**
     * Generates global JS known types map and records it to the {@code known_types.js} file.
     *
     * <p>The types in map are stored in the
     * "{@linkplain io.spine.type.TypeUrl type-url}-to-JS-type" format.
     *
     * <p>The file is written to the root of the proto JS location.
     */
    @VisibleForTesting
    void writeKnownTypes() {
        JsOutput jsOutput = new JsOutput();
        KnownTypesGenerator generator = new KnownTypesGenerator(fileSet, jsOutput);
        generator.generate();
        FileWriter writer = new FileWriter(knownTypes);
        writer.write(jsOutput.toString());
    }

    /**
     * Stores the standard Protobuf type parsers code in the {@code known_type_parsers.js} file.
     *
     * <p>The parsers can be accessed via the global map, where they are stored in the
     * "{@linkplain io.spine.type.TypeUrl type-url}-to-parser" format.
     *
     * <p>The file is written to the root of the proto JS location.
     */
    @VisibleForTesting
    void writeKnownTypeParsers() {
        copyParsersCode();
        JsOutput jsOutput = new JsOutput();
        ParserMapGenerator generator = new ParserMapGenerator(jsOutput);
        generator.generate();
        FileWriter writer = new FileWriter(knownTypeParsers);
        writer.write(jsOutput.toString());
    }

    /**
     * Copies the {@code known_type_parsers.js} resource content and stores it in the target file.
     *
     * <p>Possible {@link IOException} when copying the resource is wrapped as the
     * {@link IllegalStateException}.
     */
    private void copyParsersCode() {
        try (InputStream in = JsonParsersWriter.class
                .getClassLoader()
                .getResourceAsStream(PARSERS_RESOURCE)) {
            Files.copy(in, knownTypeParsers, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Appends the {@code fromJson(json)} methods for all known types into the corresponding files.
     */
    @VisibleForTesting
    void writeFromJsonMethod() {
        for (FileDescriptor file : fileSet.files()) {
            writeIntoFile(file);
        }
    }

    private void writeIntoFile(FileDescriptor file) {
        FileName fileName = FileName.from(file);
        boolean isSpineOptions = spineOptionsJs().equals(fileName);
        boolean isStandardType = file.getPackage()
                                     .startsWith(GOOGLE_PROTOBUF_PACKAGE.packageName());
        if (isSpineOptions || isStandardType) {
            return;
        }
        JsOutput jsOutput = new JsOutput();
        FileGenerator generator = new FileGenerator(file, jsOutput);
        generator.generate();
        Path filePath = generatedProtoDir.resolve(fileName);
        FileWriter writer = new FileWriter(filePath);
        writer.append(jsOutput.toString());
    }

    @VisibleForTesting
    FileSet fileSet() {
        return fileSet;
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static final class Builder {

        private Path knownTypes;
        private Path knownTypeParsers;
        private Directory generatedProtoDir;
        private File descriptorSetFile;

        Builder setKnownTypes(Path knownTypes) {
            this.knownTypes = knownTypes;
            return this;
        }

        Builder setKnownTypeParsers(Path knownTypeParsers) {
            this.knownTypeParsers = knownTypeParsers;
            return this;
        }

        Builder setGeneratedProtoDir(Directory generatedProtoDir) {
            this.generatedProtoDir = generatedProtoDir;
            return this;
        }

        Builder setDescriptorSetFile(File descriptorSetFile) {
            this.descriptorSetFile = descriptorSetFile;
            return this;
        }

        public JsonParsersWriter build() {
            return new JsonParsersWriter(this);
        }
    }
}
