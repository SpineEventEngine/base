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

package io.spine.tools.mc.js.code.snippet;

import io.spine.tools.js.code.MethodReference;
import io.spine.tools.code.Line;
import io.spine.tools.mc.js.code.CodeWriter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.join;

/**
 * The declaration of a method in JavaScript code.
 */
public class Method implements Snippet {

    private final MethodReference reference;
    /** Names of the method parameters. */
    private final List<String> parameters;
    private final List<Line> body;

    private Method(Builder builder) {
        this.reference = builder.methodReference;
        this.parameters = builder.parameters;
        this.body = builder.body;
    }

    @Override
    public CodeWriter writer() {
        CodeWriter lines = new CodeWriter();
        lines.append(declaration());
        lines.increaseDepth();
        try {
            for (Line bodyLine : body) {
                lines.append(bodyLine);
            }
        } finally {
            lines.decreaseDepth();
        }
        lines.append("};");
        return lines;
    }

    /**
     * Declares JS method and enters its body.
     */
    private String declaration() {
        String argString = join(", ", parameters);
        return reference + " = function(" + argString + ") {";
    }

    /**
     * Obtains the builder to compose a method.
     *
     * @param methodReference
     *         the reference to identify the method by
     * @return the builder
     */
    public static Builder newBuilder(MethodReference methodReference) {
        return new Builder(methodReference);
    }

    /**
     * The builder of a method.
     */
    public static class Builder {

        private final MethodReference methodReference;
        private final List<Line> body = newArrayList();
        private List<String> parameters = newArrayList();

        private Builder(MethodReference methodReference) {
            checkNotNull(methodReference);
            this.methodReference = methodReference;
        }

        /**
         * Specifies the parameter names of the method.
         */
        public Builder withParameters(String... parameters) {
            this.parameters = newArrayList(parameters);
            return this;
        }

        /**
         * Appends a line to the body of the method.
         */
        public Builder appendToBody(String line) {
            Line codeLine = Line.of(line);
            body.add(codeLine);
            return this;
        }

        /**
         * Appends a line to the body of the method.
         */
        public Builder appendToBody(Line line) {
            checkNotNull(line);
            body.add(line);
            return this;
        }

        /**
         * Obtains the method composed from the builder.
         */
        public Method build() {
            return new Method(this);
        }
    }
}
