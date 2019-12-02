/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.validate.field;

import com.google.protobuf.Descriptors;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * The representation of the number.
 *
 * <p>Each element corresponds to multiple types in Java and multiple types in Protobuf.
 */
enum NumberKind {

    /**
     * Integer number fields.
     *
     * <p>In Java, represented by {@code int} and {@code long}.
     *
     * <p>In Protobuf, represented by 32-bit and 64-bit {@code int}, {@code uint}, {@code sint},
     * {@code fixed}, and {@code sfixed}.
     *
     * <p>String representations are parsed into {@link Long} for maximum precision.
     */
    INTEGER {
        @Override
        Number parse(String value) {
            return Long.parseLong(value);
        }
    },

    /**
     * Fraction number fields with a floating point.
     *
     * <p>In Java, represented by {@code float} and {@code double}.
     *
     * <p>In Protobuf, represented by {@code float} and {@code double}.
     *
     * <p>String representations are parsed into {@link Double} for maximum precision.
     */
    FLOAT {
        @Override
        Number parse(String value) {
            return Double.parseDouble(value);
        }
    };

    /**
     * Parses the given string as a number.
     */
    abstract Number parse(String value);

    /**
     * Chooses a {@code NumberKind} for the given field.
     *
     * <p>Throws an {@code IllegalArgumentException} if the field is not a number field.
     */
    @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
    // `default` covers everything else.
    static NumberKind forField(Descriptors.FieldDescriptor.JavaType type) {
        switch (type) {
            case INT:
            case LONG:
                return INTEGER;
            case FLOAT:
            case DOUBLE:
                return FLOAT;
            default:
                throw newIllegalArgumentException("Unexpected type of field: %s.", type);
        }
    }
}
