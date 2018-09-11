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
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.JsOutput;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.files.JsFiles.KNOWN_TYPES;
import static io.spine.tools.protojs.files.JsFiles.writeToFile;

/**
 * The writer which stores global known types {@code Map} in the {@code known_types.js} file.
 *
 * <p>The types are stored in the "{@linkplain io.spine.type.TypeUrl type-url}-to-JS-type" format.
 *
 * <p>The JS type acquired from the map can then be used as a constructor or for the static method
 * call.
 *
 * <p>This is mainly useful for the {@link com.google.protobuf.Any} type processing whose value
 * is not known until runtime.
 *
 * @author Dmytro Kuzmin
 * @see KnownTypesGenerator
 */
public final class KnownTypesWriter {

    private static final int INDENT = 4;

    private final Path filePath;
    private final FileSet fileSet;

    private KnownTypesWriter(Path filePath, FileSet fileSet) {
        this.filePath = filePath;
        this.fileSet = fileSet;
    }

    /**
     * Creates the {@code KnownTypesWriter} for the {@code protoJsLocation} and {@code fileSet}.
     *
     * <p>All known types will be acquired from the specified {@code FileSet} and the
     * {@code protoJsLocation} will be used to determine the {@code known_types.js} file path.
     *
     * @param protoJsLocation
     *         the root of the Proto JS files location
     * @param fileSet
     *         the {@code FileSet} containing known types
     * @return the new {@code KnownTypesWriter}
     */
    public static KnownTypesWriter createFor(Path protoJsLocation, FileSet fileSet) {
        checkNotNull(protoJsLocation);
        checkNotNull(fileSet);
        Path path = composeFilePath(protoJsLocation);
        return new KnownTypesWriter(path, fileSet);
    }

    /**
     * Generates the content of the {@code known_types.js} file and stores it to disk.
     *
     * <p>The file is written to the root of the Proto JS location.
     *
     * @throws IllegalStateException
     *         if something goes wrong when recording the file
     */
    public void writeFile() {
        JsGenerator jsGenerator = new JsGenerator(INDENT);
        KnownTypesGenerator generator = new KnownTypesGenerator(fileSet, jsGenerator);
        generator.generateJs();
        JsOutput generatedCode = jsGenerator.getGeneratedCode();
        writeToFile(filePath, generatedCode);
    }

    /**
     * Composes the file path for the {@code known_types.js}.
     */
    @VisibleForTesting
    static Path composeFilePath(Path protoJsLocation) {
        Path path = Paths.get(protoJsLocation.toString(), KNOWN_TYPES);
        return path;
    }
}
