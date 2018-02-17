/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

/**
 * Test proto file names.
 *
 * See {@code resources/annotator-plugin-test/src/main/proto/} directory for the files.
 *
 * @author Dmytro Grankin
 */
public class GivenProtoFile {

    public static final String NO_SPI_OPTIONS = "no_spi_options.proto";
    public static final String NO_SPI_OPTIONS_MULTIPLE = "no_spi_options_multiple.proto";
    public static final String SPI_ALL = "spi_all.proto";
    public static final String SPI_ALL_SERVICE = "spi_all_service.proto";
    public static final String SPI_ALL_MULTIPLE = "spi_all_multiple.proto";
    public static final String SPI_MESSAGE = "spi_message.proto";
    public static final String SPI_MESSAGE_MULTIPLE = "spi_message_multiple.proto";
    public static final String SPI_FIELD = "spi_field.proto";
    public static final String SPI_FIELD_MULTIPLE = "spi_field_multiple.proto";
    public static final String SPI_SERVICE = "spi_service.proto";
    public static final String POTENTIAL_ANNOTATION_DUP = "potential_annotation_duplication.proto";

    /** Prevent instantiation of this utility class. */
    private GivenProtoFile() {
    }
}
