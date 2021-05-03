/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.dart.fs;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.AbstractFileName;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Name of a Dart file generated from Protobuf.
 *
 * <p>Always has the {@code .pb.dart} extension.
 */
public final class FileName extends AbstractFileName<FileName> {

    private static final long serialVersionUID = 0L;

    private FileName(String value) {
        super(value);
    }

    /**
     * Constructs a relative file path for a file generated from the given Protobuf file.
     *
     * @param file the source Protobuf file
     * @return new {@code FileName}, relative to the code generation root
     */
    public static FileName relative(io.spine.code.proto.FileName file) {
        String relativePath = file.nameWithoutExtension() + GeneratedExtension.OF_MESSAGE.value;
        return new FileName(relativePath);
    }

    /**
     * Constructs a relative file path for a file generated from the given Protobuf file descriptor.
     *
     * @param file the source Protobuf file descriptor
     * @return new {@code FileName}, relative to the code generation root
     */
    public static FileName relative(FileDescriptor file) {
        io.spine.code.proto.FileName protoName = io.spine.code.proto.FileName.from(file);
        return relative(protoName);
    }

    /**
     * Verifies if the passed path belongs to a Dart source code file generated from
     * a Protobuf definition.
     */
    public static boolean isGenerated(Path file) {
        Predicate<GeneratedExtension> nameHasExtension =
                extension -> file.toString().endsWith(extension.value);
        boolean result = Arrays.stream(GeneratedExtension.values())
                               .anyMatch(nameHasExtension);
        return result;
    }

    /**
     * Enumerates extensions of Dart files generated from Protobuf definitions.
     */
    public enum GeneratedExtension {
        OF_MESSAGE(".pb.dart"),
        OF_ENUM(".pbenum.dart"),
        OF_SERVER(".pbserver.dart"),
        OF_JSON(".pbjson.dart");

        private final String value;

        GeneratedExtension(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
