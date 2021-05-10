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

package io.spine.tools.mc.js.code.field;

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.js.code.FieldName;

/**
 * The generator for the {@code repeated} Protobuf fields.
 *
 * <p>The generator expects a JS object to always be a list, iterating over it and adding its
 * values to the field.
 *
 * <p>Although the {@code map} fields are technically also {@code repeated}, they are not handled
 * by this class.
 */
final class RepeatedFieldGenerator extends FieldGenerator {

    /**
     * The variable used to represent the list item during the JS object iteration.
     */
    @VisibleForTesting
    static final String LIST_ITEM = "listItem";

    private RepeatedFieldGenerator(Builder builder) {
        super(builder);
    }

    @Override
    public void generate() {
        String fieldValue = acquireFieldValue();
        iterateListValues(fieldValue);
        mergeFieldValue(LIST_ITEM);
        exitListValueIteration();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The merge format for the {@code repeated} field is calling the {@code add...} method on
     * the repeated field JS representation.
     */
    @Override
    String mergeFormat() {
        FieldName fieldName = FieldName.from(field());
        String addFunctionName = "add" + fieldName;
        String addToListFormat = targetVariable() + '.' + addFunctionName + "(%s);";
        return addToListFormat;
    }

    /**
     * Generates the code to iterate over given {@code jsObject} assuming it is a list.
     *
     * <p>Checks the value for not being {@code null} or {@code undefined}.
     *
     * @param jsObject
     *         the name of the variable holding the JS object to iterate
     */
    @VisibleForTesting
    void iterateListValues(String jsObject) {
        writer().ifNotNullOrUndefined(jsObject)
                .append(jsObject + ".forEach(")
                .increaseDepth()
                .enterBlock('(' + LIST_ITEM + ", index, array) =>");
    }

    /**
     * Generates the code to exit all blocks entered during the JS object iteration.
     *
     * <p>Returns the cursor to the {@code fromObject} method level.
     */
    private void exitListValueIteration() {
        writer().exitBlock()
                .decreaseDepth()
                .append(");")
                .exitBlock();
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends FieldGenerator.Builder<Builder> {

        @Override
        Builder self() {
            return this;
        }

        @Override
        RepeatedFieldGenerator build() {
            return new RepeatedFieldGenerator(this);
        }
    }
}
