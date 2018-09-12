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

package io.spine.tools.protojs.code.primitive;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The generator of the JS code parsing {@code enum} value from its JSON representation.
 *
 * <p>The {@code enum} proto value in JSON is represented as a plain {@code string}. Thus, the
 * parser obtains the value by parsing the JS enum-like object from the given {@code string}.
 *
 * @author Dmytro Kuzmin
 */
final class EnumParser extends AbstractPrimitiveParser {

    private final String enumType;

    private EnumParser(Builder builder) {
        super(builder);
        this.enumType = builder.enumType;
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        jsOutput().addLine("let " + variable + " = " + enumType + '[' + value + "];");
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends AbstractPrimitiveParser.Builder<Builder> {

        private String enumType;

        /**
         * Sets the enum type of the field.
         *
         * @param enumType
         *         the full enum type name with the "proto." prefix
         * @return self
         */
        Builder setEnumType(String enumType) {
            this.enumType = enumType;
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        public PrimitiveParser build() {
            return new EnumParser(this);
        }
    }
}
