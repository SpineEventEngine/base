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

import io.spine.tools.mc.js.code.text.VariableDeclaration;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The generator of the code for parsing proto value from the JSON to itself.
 *
 * <p>The number of proto types like {@code int32}, {@code string}, {@code bool} and others are
 * represented in JSON in the same way as in the JS.
 *
 * <p>The {@code IdentityParser} "parses" them by just assigning the variable to the passed value.
 */
final class IdentityParser extends AbstractPrimitiveParser {

    private IdentityParser(Builder builder) {
        super(builder);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        jsOutput().append(VariableDeclaration.initialized(variable, value));
    }

    static Builder newBuilder() {
        return new Builder();
    }

    static class Builder extends AbstractPrimitiveParser.Builder<Builder> {

        @Override
        Builder self() {
            return this;
        }

        @Override
        public PrimitiveParser build() {
            return new IdentityParser(this);
        }
    }
}
