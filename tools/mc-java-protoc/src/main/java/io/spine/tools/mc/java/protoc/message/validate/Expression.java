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

package io.spine.tools.mc.java.protoc.message.validate;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.squareup.javapoet.CodeBlock;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;

/**
 * A Java code structure which may yield a value.
 *
 * <p>The type parameter {@code R} is not used directly in the definition of {@code Expression} API.
 * {@code R} signifies the return type of the Java code expression represented by a given instance
 * of {@code Expression} type. For example, if the Java code constructs a list of strings,
 * the expression should be parametrized as {@code Expression<List<String>>}.
 *
 * <p>Descendants may choose to utilize the type parameter. In such a case, the party relying on
 * the type parameter value must check for an absent parameter (i.e. bound by
 * {@code java.lang.Object}).
 *
 * @param <R>
 *         the type of the value yielded by this expression; the type parameter is unused and serves
 *         for clarification purposes only
 */
@SuppressWarnings("unused") // Unused type param <R>.
public interface Expression<R> {

    /**
     * Prints this expression as a {@link CodeBlock}.
     */
    CodeBlock toCode();

    /**
     * Builds a return statement based on this expression.
     *
     * <p>The resulting code returns the value of this expression from a method.
     */
    default CodeBlock returnStatement() {
        return CodeBlock.of("return $L;$L", this.toCode(), lineSeparator());
    }

    /**
     * Creates an {@code Expression} from the given value.
     */
    static <R> Expression<R> of(String code) {
        checkNotEmptyOrBlank(code);
        return new CodeExpression<>(code);
    }

    /**
     * Creates an {@code Expression} from the given value.
     */
    static <R> Expression<R> of(CodeBlock code) {
        checkNotNull(code);
        return new CodeExpression<>(code.toString());
    }

    /**
     * Creates an {@code Expression} from the given code.
     *
     * @param code
     *         Java code formatted as for {@link CodeBlock#of(String, Object...)}
     * @param args
     *         formatting arguments as for {@link CodeBlock#of(String, Object...)}
     * @return new expression
     */
    static <R> Expression<R> fromCode(String code, Object... args) {
        checkNotNull(code);
        return of(CodeBlock.of(code, args));
    }

    /**
     * Creates an {@code Expression} by formatting the given template string by the rules of
     * {@code String.format()}.
     */
    @FormatMethod
    static <R> Expression<R> formatted(@FormatString String template, Object... args) {
        checkNotNull(template);
        String code = format(template, args);
        return of(code);
    }
}
