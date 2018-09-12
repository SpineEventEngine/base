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
import io.spine.tools.protojs.code.JsOutput;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value checker for fields of Protobuf message type.
 *
 * <p>The class is {@code public} for the testing purposes.
 *
 * @author Dmytro Kuzmin
 */
public final class MessageFieldChecker implements FieldValueChecker {

    private final FieldDescriptor field;
    private final JsOutput jsOutput;

    /**
     * Creates a new {@code MessageFieldChecker} for the given {@code field}.
     *
     * @param field
     *         the field to create the checker for
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all generated code
     */
    MessageFieldChecker(FieldDescriptor field, JsOutput jsOutput) {
        this.field = field;
        this.jsOutput = jsOutput;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For messages, if the parsed value equals to {@code null}, the message value is also set
     * to null via {@code setterFormat}. The further parsing does not happen in this case.
     *
     * <p>The only exception is Protobuf {@link Value} type, where the check does not take place
     * and the {@code null} is allowed to reach the parser, which later converts it to
     * {@link com.google.protobuf.NullValue}.
     */
    @Override
    public void performNullCheck(String value, String setterFormat) {
        checkNotNull(value);
        checkNotNull(setterFormat);
        if (isProtobufValueType()) {
            return;
        }
        jsOutput.ifNull(value);
        String setFieldToNull = String.format(setterFormat, "null");
        jsOutput.addLine(setFieldToNull);
        jsOutput.enterElseBlock();
    }

    @Override
    public void exitNullCheck() {
        if (!isProtobufValueType()) {
            jsOutput.exitBlock();
        }
    }

    /**
     * Checks if the stored {@link #field} is of the Protobuf {@link Value} type.
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
