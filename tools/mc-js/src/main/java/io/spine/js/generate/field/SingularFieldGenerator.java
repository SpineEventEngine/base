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

package io.spine.js.generate.field;

import io.spine.tools.js.code.FieldName;

/**
 * The generator of the ordinary proto field (i.e. non-{@code repeated} and non-{@code map}).
 */
final class SingularFieldGenerator extends FieldGenerator {

    private SingularFieldGenerator(Builder builder) {
        super(builder);
    }

    @Override
    public void generate() {
        String fieldValue = acquireFieldValue();
        checkNotUndefined(fieldValue);
        mergeFieldValue(fieldValue);
        exitUndefinedCheck();
    }

    /**
     * {@inheritDoc}
     *
     * <p>The merge format for a singular field is just calling the corresponding field setter on
     * the message object.
     */
    @Override
    String mergeFormat() {
        FieldName fieldName = FieldName.from(field());
        String setterName = "set" + fieldName;
        String setFieldFormat = targetVariable() + '.' + setterName + "(%s);";
        return setFieldFormat;
    }

    /**
     * Generates the code which will check the provided {@code jsObject} for not being
     * {@code undefined}.
     *
     * @param jsObject
     *         the name of the variable which holds the JS object to check
     */
    private void checkNotUndefined(String jsObject) {
        jsOutput().ifNotUndefined(jsObject);
    }

    /**
     * Generates the code which exits all blocks entered when checking for undefined.
     *
     * <p>Returns the cursor to the {@code fromObject} method level.
     */
    private void exitUndefinedCheck() {
        jsOutput().exitBlock();
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
        SingularFieldGenerator build() {
            return new SingularFieldGenerator(this);
        }
    }
}
