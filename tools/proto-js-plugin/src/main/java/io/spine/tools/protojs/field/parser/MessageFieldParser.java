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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsGenerator;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

/**
 * The value parser for the Protobuf message fields.
 *
 * <p>Handles all fields of message types except those who belong to standard Protobuf
 * {@linkplain io.spine.tools.protojs.field.Fields#isWellKnownType(FieldDescriptor) types} which
 * are parsed separately.
 *
 * <p>The class is {@code public} for the test purposes.
 *
 * @author Dmytro Kuzmin
 */
public final class MessageFieldParser implements FieldValueParser {

    private final FieldDescriptor field;
    private final JsGenerator jsGenerator;

    /**
     * Creates the {@code MessageFieldParser} for the given {@code field}.
     *
     * <p>All the generated code will be stored in the given {@code jsGenerator}.
     *
     * @param field
     *         the descriptor of the field for which to create the parser
     * @param jsGenerator
     *         the {@code JsGenerator} which accumulates all the generated code
     */
    MessageFieldParser(FieldDescriptor field, JsGenerator jsGenerator) {
        this.field = field;
        this.jsGenerator = jsGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the message type which does not belong to the well-known types (see
     * {@link WellKnownFieldParser}), the parse operation is executed via the recursive
     * {@code fromObject} method call.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        Descriptor fieldType = field.getMessageType();
        String typeName = typeWithProtoPrefix(fieldType);
        jsGenerator.addLine("let " + variable + " = " + typeName + ".fromObject(" + value + ");");
    }
}
