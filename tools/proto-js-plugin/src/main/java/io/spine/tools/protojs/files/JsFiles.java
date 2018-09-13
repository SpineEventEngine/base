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
import io.spine.tools.protojs.code.JsOutput;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * The utility for writing the JavaScript files.
 *
 * <p>{@code JsFiles} also hold the names of common JS files used in the plugin.
 *
 * @author Dmytro Kuzmin
 * @see JsOutput
 */
public final class JsFiles {

    /**
     * The file that exports the global known types map.
     *
     * @see io.spine.tools.protojs.knowntypes.KnownTypesWriter
     */
    public static final String KNOWN_TYPES = "known_types.js";

    /**
     * The file that exports the standard Protobuf type parsers.
     *
     * @see io.spine.tools.protojs.knowntypes.KnownTypeParsersWriter
     */
    public static final String KNOWN_TYPE_PARSERS = "known_type_parsers.js";

    /**
     * The suffix of the files generated by the Protobuf JS compiler.
     */
    public static final String JS_PROTO_SUFFIX = "_pb.js";

    /** Prevents the instantiation of this utility class. */
    private JsFiles() {
    }

    /**
     * Writes the given {@code JsOutput} to the file.
     *
     * <p>Overwrites the previous file content.
     *
     * @param path
     *         the file path
     * @param output
     *         the {@code JsOutput} to write
     * @throws IllegalStateException
     *         if something went wrong when writing to the file
     */
    public static void writeToFile(Path path, JsOutput output) {
        checkNotNull(path);
        checkNotNull(output);
        try {
            String content = output.toString();
            write(path, content.getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Appends the given {@code JsOutput} to the end of the file.
     *
     * @param path
     *         the file path
     * @param output
     *         the {@code JsOutput} to append
     * @throws IllegalStateException
     *         if something went wrong when writing to the file
     */
    public static void appendToFile(Path path, JsOutput output) {
        checkNotNull(path);
        checkNotNull(output);
        try {
            String content = output.toString();
            write(path, content.getBytes(), CREATE, APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the JS proto definition name for the proto file.
     *
     * <p>The file name is relative to the proto definitions root.
     *
     * @param file
     *         the descriptor of the file
     * @return the JS proto file name
     */
    public static String jsFileName(FileDescriptor file) {
        checkNotNull(file);
        FileName fileName = FileName.from(file);
        String nameWithoutExtension = fileName.nameWithoutExtension();
        String jsFileName = nameWithoutExtension + JS_PROTO_SUFFIX;
        return jsFileName;
    }
}
