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

package io.spine.tools.protojs.field.checker;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Value;
import io.spine.tools.protojs.generate.JsOutput;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value checker for the proto fields of {@code message} type.
 *
 * @author Dmytro Kuzmin
 */
public final class MessageFieldPrecondition implements FieldPrecondition {

    private final FieldDescriptor field;
    private final JsOutput jsOutput;

    /**
     * Creates a new {@code MessageFieldPrecondition} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all generated code
     */
    MessageFieldPrecondition(FieldDescriptor field, JsOutput jsOutput) {
        this.field = field;
        this.jsOutput = jsOutput;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For messages, if the parsed value equals to {@code null}, the message value is also set
     * to null via the {@code mergeFieldFormat}. The further parsing does not happen in this case.
     *
     * <p>The only exception is the Protobuf {@link Value} type, where the check does not take
     * place and the {@code null} is allowed to reach the parser, which later converts it to the
     * {@link com.google.protobuf.NullValue}.
     */
    @Override
    public void performNullCheck(String value, String mergeFieldFormat) {
        checkNotNull(value);
        checkNotNull(mergeFieldFormat);
        if (isProtobufValueType()) {
            return;
        }
        jsOutput.ifNull(value);
        String mergeNull = String.format(mergeFieldFormat, "null");
        jsOutput.addLine(mergeNull);
        jsOutput.enterElseBlock();
    }

    @Override
    public void exitNullCheck() {
        if (!isProtobufValueType()) {
            jsOutput.exitBlock();
        }
    }

    /**
     * Checks if the processed {@code field} is of the Protobuf {@link Value} type.
     */
    private boolean isProtobufValueType() {
        String valueType = Value.getDescriptor()
                                .getFullName();
        String fieldType = field.getMessageType()
                                .getFullName();
        boolean isValueType = fieldType.equals(valueType);
        return isValueType;
    }
}
