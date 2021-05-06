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

import com.google.common.annotations.VisibleForTesting;
import io.spine.tools.mc.js.code.imports.Import;
import io.spine.tools.mc.js.code.text.Let;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The generator of the code parsing {@code bytes} value from its JSON representation.
 *
 * <p>The JSON representation of the {@code bytes} value is the base-64 encoded {@code string}.
 *
 * <p>The parser thus imports the "base64" lib and decodes the value.
 */
final class BytesParser extends AbstractPrimitiveParser {

    /**
     * Base-64 JS lib name to import.
     *
     * @see <a href="https://www.npmjs.com/package/base64-js">The lib page</a>
     */
    @VisibleForTesting
    static final String BASE64_LIB = "base64-js";

    /**
     * The name of the "base64-js" import.
     */
    @VisibleForTesting
    static final String BASE64_VAR = "base64";

    private BytesParser(Builder builder) {
        super(builder);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        Import base64Import = Import.library(BASE64_LIB);
        jsOutput().append(base64Import.namedAs(BASE64_VAR));
        jsOutput().append(parsedVariable(variable, value));
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Necessary duplication with own test.
    private static Let parsedVariable(String name, String valueToParse) {
        String initializer = BASE64_VAR + ".toByteArray(" + valueToParse + ')';
        return Let.withValue(name, initializer);
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
            return new BytesParser(this);
        }
    }
}
