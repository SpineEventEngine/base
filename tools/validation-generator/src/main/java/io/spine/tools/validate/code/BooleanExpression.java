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

import com.squareup.javapoet.CodeBlock;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.valueOf;

/**
 * An expression which yields a {@code boolean} value.
 */
public final class BooleanExpression
        extends CodeExpression<Boolean> {

    private static final long serialVersionUID = 0L;

    private static final BooleanExpression FALSE = new BooleanExpression(valueOf(false));

    private BooleanExpression(String value) {
        super(value);
    }

    /**
     * Creates a {@code BooleanExpression} from the given code.
     *
     * @param code
     *         Java code formatted as for {@link CodeBlock#of(String, Object...)}
     * @param args
     *         formatting arguments as for {@link CodeBlock#of(String, Object...)}
     * @return new expression
     */
    public static BooleanExpression fromCode(String code, Object... args) {
        CodeBlock block = CodeBlock.of(code, args);
        return new BooleanExpression(block.toString());
    }

    /**
     * Obtains a {@code BooleanExpression} representing literal {@code false}.
     */
    public static BooleanExpression falseLiteral() {
        return FALSE;
    }

    /**
     * Creates an {@code if} statement this expression as a condition and the given code as
     * the conditional code.
     *
     * @param branch
     *         the code which should execute if this expression yields {@code true}
     * @return a new conditional statement
     */
    public ConditionalStatement ifTrue(CodeBlock branch) {
        return new ConditionalStatement(this, checkNotNull(branch));
    }
}
