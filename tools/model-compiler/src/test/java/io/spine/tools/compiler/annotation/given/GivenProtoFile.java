/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.annotation.given;

import io.spine.code.proto.FileName;

/**
 * Test proto file names.
 *
 * See {@code resources/annotator-plugin-test/src/main/proto/} directory for the files.
 */
public class GivenProtoFile {

    public static final FileName NO_INTERNAL_OPTIONS = FileName.of("no_internal_options.proto");
    public static final FileName NO_INTERNAL_OPTIONS_MULTIPLE = FileName.of("no_internal_options_multiple.proto");
    public static final FileName INTERNAL_ALL = FileName.of("internal_all.proto");
    public static final FileName INTERNAL_ALL_SERVICE = FileName.of("internal_all_service.proto");
    public static final FileName INTERNAL_ALL_MULTIPLE = FileName.of("internal_all_multiple.proto");
    public static final FileName INTERNAL_MESSAGE = FileName.of("internal_message.proto");
    public static final FileName INTERNAL_MESSAGE_MULTIPLE = FileName.of("internal_message_multiple.proto");
    public static final FileName INTERNAL_FIELD = FileName.of("internal_field.proto");
    public static final FileName INTERNAL_FIELD_MULTIPLE = FileName.of("internal_field_multiple.proto");
    public static final FileName SPI_SERVICE = FileName.of("spi_service.proto");
    public static final FileName POTENTIAL_ANNOTATION_DUP = FileName.of("potential_annotation_duplication.proto");

    /** Prevent instantiation of this utility class. */
    private GivenProtoFile() {
    }
}
