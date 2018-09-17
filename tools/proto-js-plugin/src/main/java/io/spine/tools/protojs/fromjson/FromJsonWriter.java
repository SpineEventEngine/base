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

package io.spine.tools.protojs.fromjson;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.tools.protojs.generate.FileGenerator;
import io.spine.tools.protojs.generate.JsOutput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.files.JsFiles.appendToFile;
import static io.spine.tools.protojs.files.JsFiles.jsFileName;
import static io.spine.tools.protojs.types.Types.isStandardOrSpineOptions;

/**
 * The appender of the {@code fromJson(json)} method into the existing JS Proto definitions.
 *
 * <p>The class processes all the known types except the standard ones and Spine Options.
 *
 * <p>The method and all the related code is simply appended at the end of the JS file.
 *
 * @author Dmytro Kuzmin
 * @see FileGenerator
 */
public final class FromJsonWriter {

    private final Path protoJsLocation;
    private final FileSet fileSet;

    private FromJsonWriter(Path protoJsLocation, FileSet fileSet) {
        this.protoJsLocation = protoJsLocation;
        this.fileSet = fileSet;
    }

    /**
     * Creates a new {@code FromJsonWriter}.
     *
     * @param protoJsLocation
     *         the location to lookup JS proto definitions
     * @param fileSet
     *         the {@code FileSet} containing all the known types
     * @return the new {@code FromJsonWriter} instance
     */
    public static FromJsonWriter createFor(Path protoJsLocation, FileSet fileSet) {
        checkNotNull(protoJsLocation);
        checkNotNull(fileSet);
        return new FromJsonWriter(protoJsLocation, fileSet);
    }

    /**
     * Writes the {@code fromJson(json)} method and related code into the JS proto definitions.
     *
     * <p>Standard Google Protobuf types and Spine Options are skipped.
     *
     * <p>If the JS definition for the {@link FileDescriptor} is not found, the file is skipped.
     */
    public void writeIntoFiles() {
        for (FileDescriptor file : fileSet.files()) {
            if (!isStandardOrSpineOptions(file)) {
                writeIntoFile(file);
            }
        }
    }

    /**
     * Generates the {@code fromJson} code for the given {@code file} and appends it to the
     * corresponding JS file.
     */
    private void writeIntoFile(FileDescriptor file) {
        Path jsFilePath = composeFilePath(file);
        if (!Files.exists(jsFilePath)) {
            return;
        }
        JsOutput jsOutput = new JsOutput();
        FileGenerator generator = new FileGenerator(file, jsOutput);
        generator.generateJs();
        appendToFile(jsFilePath, jsOutput);
    }

    /**
     * Calculates the supposed JS definition path for the given {@code FileDescriptor}.
     */
    @VisibleForTesting
    Path composeFilePath(FileDescriptor file) {
        String jsFileName = jsFileName(file);
        Path path = Paths.get(protoJsLocation.toString(), jsFileName);
        return path;
    }
}
