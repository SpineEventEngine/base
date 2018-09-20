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
import io.spine.code.js.TypeName;
import io.spine.generate.JsOutput;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.message.MessageGenerator.FROM_OBJECT;

/**
 * The value parser for the proto fields of {@code message} type.
 *
 * <p>Handles all {@code message} fields except those who belong to standard Protobuf types which
 * are parsed separately.
 *
 * @author Dmytro Kuzmin
 */
final class MessageFieldParser implements FieldParser {

    private final TypeName typeName;
    private final JsOutput jsOutput;

    private MessageFieldParser(TypeName typeName, JsOutput jsOutput) {
        this.typeName = typeName;
        this.jsOutput = jsOutput;
    }

    /**
     * Creates the {@code MessageFieldParser} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all the generated code
     */
    static MessageFieldParser createFor(FieldDescriptor field, JsOutput jsOutput) {
        checkNotNull(field);
        checkNotNull(jsOutput);
        Descriptor messageType = field.getMessageType();
        TypeName typeName = TypeName.from(messageType);
        return new MessageFieldParser(typeName, jsOutput);
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the {@code message} types that do not belong to the well-known types, the parse
     * operation is executed via the recursive {@code fromObject} method call.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        String recursiveCall = typeName.value() + '.' + FROM_OBJECT + '(' + value + ')';
        jsOutput.declareVariable(variable, recursiveCall);
    }
}
