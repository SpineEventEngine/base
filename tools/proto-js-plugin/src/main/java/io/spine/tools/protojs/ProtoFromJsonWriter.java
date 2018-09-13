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
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.fromjson.FromJsonWriter;
import io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter;
import io.spine.tools.protojs.knowntypes.KnownTypesWriter;

import java.io.File;
import java.nio.file.Path;

/**
 * The global writer used by the {@link ProtoJsPlugin}.
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
final class ProtoFromJsonWriter {

    private final Path protoJsLocation;
    private final FileSet fileSet;

    private ProtoFromJsonWriter(Path protoJsLocation, FileSet fileSet) {
        this.protoJsLocation = protoJsLocation;
        this.fileSet = fileSet;
    }

    /**
     * Creates the {@code ProtoFromJsonWriter} for the specified {@code protoJsLocation} and
     * {@code descriptorSetFile}.
     *
     * <p>The {@code protoJsLocation} will be used to look for the proto JS files and the
     * {@code descriptorSetFile} is used to acquire all the known types.
     *
     * @param protoJsLocation
     *         the location of the JS proto definitions
     * @param descriptorSetFile
     *         the descriptor set file generated by Protobuf compiler
     * @return the new instance of the {@code ProtoFromJsonWriter}
     */
    static ProtoFromJsonWriter createFor(Path protoJsLocation, File descriptorSetFile) {
        FileSet fileSet = collectFileSet(descriptorSetFile);
        return new ProtoFromJsonWriter(protoJsLocation, fileSet);
    }

    /**
     * Checks if {@code ProtoJsonWriter}'s {@link FileSet} has any known types to process.
     *
     * @return {@code true} if the file set has files to process and {@code false} otherwise
     */
    boolean hasFilesToProcess() {
        boolean hasFilesToProcess = !fileSet.isEmpty();
        return hasFilesToProcess;
    }

    /**
     * Generates and writes the JS code necessary to parse proto JS messages from the JSON format.
     */
    void writeFromJsonForProtos() {
        writeKnownTypes();
        writeKnownTypeParsers();
        writeFromJsonMethod();
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
        KnownTypesWriter writer = KnownTypesWriter.createFor(protoJsLocation, fileSet);
        writer.writeFile();
    }

    /**
     * Stores the standard Protobuf type parsers code in the {@code known_type_parsers.js} file.
     *
     * <p>The parsers can be accessed via a global map, where they are stored in the
     * "{@linkplain io.spine.type.TypeUrl type-url}-to-parser" format.
     *
     * <p>The file is written to the root of the proto JS location.
     */
    @VisibleForTesting
    void writeKnownTypeParsers() {
        KnownTypeParsersWriter writer = KnownTypeParsersWriter.createFor(protoJsLocation);
        writer.writeFile();
    }

    /**
     * Appends the {@code fromJson(json)} methods for all known types into the corresponding files.
     */
    @VisibleForTesting
    void writeFromJsonMethod() {
        FromJsonWriter writer = FromJsonWriter.createFor(protoJsLocation, fileSet);
        writer.writeIntoFiles();
    }

    @VisibleForTesting
    FileSet fileSet() {
        return fileSet;
    }

    /**
     * Collects the {@code FileSet} from the given descriptor set file, returns
     * {@linkplain FileSet#newInstance() empty} file set in case the file does not exist.
     */
    private static FileSet collectFileSet(File descriptorSetFile) {
        if (descriptorSetFile.exists()) {
            FileSet fileSet = FileSet.parse(descriptorSetFile);
            return fileSet;
        }
        FileSet emptySet = FileSet.newInstance();
        return emptySet;
    }
}
