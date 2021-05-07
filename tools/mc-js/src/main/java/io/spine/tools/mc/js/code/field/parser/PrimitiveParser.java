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

package io.spine.tools.mc.js.code.field.parser;

import io.spine.tools.mc.js.code.CodeWriter;

/**
 * The generator of the JS code parsing some primitive value from its JSON representation.
 *
 * <p>The parsed value is then stored into the specified variable.
 *
 * <p>Please see <a href="https://developers.google.com/protocol-buffers/docs/proto3#json">
 * Protobuf JSON Mapping</a> for more details.
 *
 * @apiNote The descendants are supposed to operate on the provided {@link CodeWriter}, so
 *         the interface method is not returning any generated code.
 */
interface PrimitiveParser {

    /**
     * Generates the code required to parse the primitive value and assign it to the variable.
     *
     * @param value
     *         the name of the variable holding the value to parse
     * @param variable
     *         the name of the variable to receive the parsed value
     */
    void parseIntoVariable(String value, String variable);

    /**
     * The generic builder for the classes-descendants.
     *
     * @param <B>
     *         the class of the Builder itself
     */
    interface Builder<B extends Builder<B>> {

        /**
         * Sets the writer to accumulate all the generated code.
         */
        B setWriter(CodeWriter writer);

        /**
         * Creates the {@code PrimitiveParser} instance corresponding to this builder.
         */
        PrimitiveParser build();
    }
}
