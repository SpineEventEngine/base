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

package io.spine.tools.protojs.field.parser;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsGenerator;
import io.spine.tools.protojs.code.primitive.parser.PrimitiveParser;
import io.spine.tools.protojs.code.primitive.parser.PrimitiveParsers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value parser for the primitive Protobuf fields.
 *
 * <p>All Protobuf fields that are not of the message type are considered primitive and are thus
 * handled by this parser.
 *
 * <p>The class is {@code public} for the test purposes.
 *
 * @author Dmytro Kuzmin
 */
public final class PrimitiveFieldParser implements FieldValueParser {

    private final FieldDescriptor field;
    private final JsGenerator jsGenerator;

    /**
     * Creates a new {@code PrimitiveFieldParser} for the given field.
     *
     * <p>All the generated code will be accumulated in the given {@code jsGenerator}.
     *
     * @param field
     *         the descriptor of the field to create the parser for
     * @param jsGenerator
     *         the {@code JsGenerator} to store the generated code
     */
    PrimitiveFieldParser(FieldDescriptor field, JsGenerator jsGenerator) {
        this.field = field;
        this.jsGenerator = jsGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the primitive field, the {@link PrimitiveParser} implementation is used to convert
     * the field value into the appropriate type.
     *
     * @see PrimitiveParsers
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        PrimitiveParser parser = PrimitiveParsers.createFor(field, jsGenerator);
        parser.parseIntoVariable(value, variable);
    }
}
