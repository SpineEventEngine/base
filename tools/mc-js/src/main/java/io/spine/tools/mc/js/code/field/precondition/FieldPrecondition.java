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

package io.spine.tools.mc.js.code.field.precondition;

import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.mc.js.code.text.CodeLines;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.code.proto.FieldTypes.isMessage;

/**
 * The generator of the code which performs various checks on the proto field value.
 *
 * @apiNote
 * The descendants are supposed to operate on the provided {@link io.spine.tools.mc.js.code.text.CodeLines},
 * so the interface methods are not returning any generated code.
 */
public interface FieldPrecondition {

    /**
     * Generates the code which checks the given field value for {@code null}.
     *
     * <p>The merge field format is specified so the precondition can interact with the field
     * itself in case the check passes/fails.
     *
     * @param value
     *         the name of the variable representing the field value to check
     * @param mergeFieldFormat
     *         the code that sets/adds value to the field
     */
    void performNullCheck(String value, String mergeFieldFormat);

    /**
     * Generates the code to exit the {@code null} check block and return to the upper level.
     */
    void exitNullCheck();

    /**
     * Creates a new precondition for the given field.
     *
     * @param field
     *         the descriptor of the Protobuf field to create the precondition for
     * @param jsOutput
     *         the {@code JsOutput} which will accumulate all the generated code
     * @return a {@code FieldPrecondition} of the appropriate type
     */
    static FieldPrecondition preconditionFor(FieldDescriptor field, CodeLines jsOutput) {
        checkNotNull(field);
        checkNotNull(jsOutput);
        if (isMessage(field)) {
            return new MessagePrecondition(field, jsOutput);
        }
        return new PrimitivePrecondition(jsOutput);
    }
}
