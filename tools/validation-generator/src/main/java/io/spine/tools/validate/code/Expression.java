/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.validate.code;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.squareup.javapoet.CodeBlock;

import static java.lang.String.format;

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
     * Creates an {@code Expression} from the given value.
     */
    static <R> Expression<R> of(String code) {
        return new CodeExpression<>(code);
    }

    /**
     * Creates an {@code Expression} from the given value.
     */
    static <R> Expression<R> of(CodeBlock code) {
        return new CodeExpression<>(code.toString());
    }

    /**
     * Creates an {@code Expression} by formatting the given template string by the rules of
     * {@code String.format()}.
     */
    @FormatMethod
    static <R> Expression<R> formatted(@FormatString String template, Object... args) {
        String code = format(template, args);
        return of(code);
    }
}
