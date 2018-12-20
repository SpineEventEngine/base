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

package io.spine.js.generate.statement;

import io.spine.js.generate.CodeLine;
import io.spine.js.generate.CodeLines;
import io.spine.js.generate.RawLine;
import io.spine.js.generate.Snippet;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static io.spine.validate.Validate.checkNotEmptyOrBlank;
import static java.lang.String.join;

/**
 * The declaration of a method in Javascript code.
 */
public class Method implements Snippet {

    private final String name;
    private final List<String> arguments;
    private final List<CodeLine> bodyLines;

    private Method(Builder builder) {
        this.name = builder.name;
        this.arguments = builder.arguments;
        this.bodyLines = builder.body;
    }

    @Override
    public CodeLines value() {
        CodeLines output = new CodeLines();
        output.append(declaration());
        output.increaseDepth();
        for (CodeLine bodyLine : bodyLines) {
            output.append(bodyLine);
        }
        output.decreaseDepth();
        output.append(ending());
        return output;
    }

    /**
     * Declares JS method and enters its body.
     */
    private RawLine declaration() {
        String argString = join(", ", arguments);
        return RawLine.of(name + " = function(" + argString + ") {");
    }

    private static RawLine ending() {
        return RawLine.of("};");
    }

    /**
     * Obtains the builder to compose a method.
     *
     * @param methodName
     *         the name of the method
     * @return the builder
     */
    public static Builder newBuilder(String methodName) {
        return new Builder(methodName);
    }

    /**
     * The builder of a method.
     */
    public static class Builder {

        private final String name;
        private final List<CodeLine> body = newArrayList();
        private List<String> arguments = newArrayList();

        Builder(String name) {
            checkNotEmptyOrBlank(name, "name");
            this.name = name;
        }

        /**
         * Specifies the argument names of the method.
         */
        public Builder withArguments(String... arguments) {
            this.arguments = newArrayList(arguments);
            return this;
        }

        /**
         * Appends a line to the body of the method.
         */
        public Builder appendToBody(String line) {
            RawLine codeLine = RawLine.of(line);
            body.add(codeLine);
            return this;
        }

        /**
         * Appends a line to the body of the method.
         */
        public Builder appendToBody(CodeLine line) {
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
