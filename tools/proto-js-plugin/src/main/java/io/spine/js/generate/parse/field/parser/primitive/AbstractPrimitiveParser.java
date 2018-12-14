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

package io.spine.js.generate.parse.field.parser.primitive;

import io.spine.js.generate.JsOutput;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The common base for the {@link PrimitiveParser} implementations.
 */
abstract class AbstractPrimitiveParser implements PrimitiveParser {

    private final JsOutput jsOutput;

    AbstractPrimitiveParser(Builder builder) {
        this.jsOutput = builder.jsOutput;
    }

    JsOutput jsOutput() {
        return jsOutput;
    }

    abstract static class Builder<B extends Builder<B>> implements PrimitiveParser.Builder<B> {

        private JsOutput jsOutput;

        @Override
        public B setJsOutput(JsOutput jsOutput) {
            this.jsOutput = checkNotNull(jsOutput);
            return self();
        }

        /**
         * Must return {@code this} in classes-descendants.
         */
        abstract B self();
    }
}
