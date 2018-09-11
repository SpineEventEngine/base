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

package io.spine.tools.protojs.code.primitive.parser;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protojs.code.JsImportGenerator.rawNamedImport;

final class BytesParser extends AbstractPrimitiveParser {

    @VisibleForTesting
    static final String BASE64_LIB = "base64-js";

    @VisibleForTesting
    static final String BASE64_VAR = "base64";

    private BytesParser(Builder builder) {
        super(builder);
    }

    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        String importStatement = rawNamedImport(BASE64_LIB, BASE64_VAR);
        jsGenerator().addLine(importStatement);
        String valueToByteArray = BASE64_VAR + ".toByteArray(" + value + ')';
        jsGenerator().addLine("let " + variable + " = " + valueToByteArray + ';');
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
