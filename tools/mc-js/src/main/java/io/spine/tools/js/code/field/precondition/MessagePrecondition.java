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

package io.spine.tools.js.code.field.precondition;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Value;
import io.spine.tools.js.code.output.CodeLines;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The precondition for the proto fields of {@code message} types.
 */
final class MessagePrecondition implements FieldPrecondition {

    private final FieldDescriptor field;
    private final CodeLines jsOutput;

    /**
     * Creates a new {@code MessagePrecondition} for the given {@code field}.
     *
     * @param field
     *         the processed field
     * @param jsOutput
     *         the {@code JsOutput} which accumulates all generated code
     */
    MessagePrecondition(FieldDescriptor field, CodeLines jsOutput) {
        this.field = field;
        this.jsOutput = jsOutput;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For messages, if the parsed value equals to {@code null}, the message value is also set
     * to {@code null} via the {@code mergeFieldFormat}. The further parsing does not happen in
     * this case.
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
        jsOutput.append(mergeNull);
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
