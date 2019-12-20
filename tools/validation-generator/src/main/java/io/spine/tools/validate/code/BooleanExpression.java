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
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An expression which yields a {@code boolean} value.
 */
public final class BooleanExpression
        extends CodeExpression<Boolean> {

    private static final long serialVersionUID = 0L;

    private static final BooleanExpression TRUE = new BooleanExpression(true);
    private static final BooleanExpression FALSE = new BooleanExpression(false);

    private final @Nullable Boolean literalValue;

    private BooleanExpression(String value) {
        super(value);
        this.literalValue = null;
    }

    private BooleanExpression(boolean literalValue) {
        super(String.valueOf(literalValue));
        this.literalValue = literalValue;
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
     * Obtains a {@code BooleanExpression} representing literal {@code true}.
     */
    public static BooleanExpression trueLiteral() {
        return TRUE;
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

    /**
     * Checks if this expression is a constant, {@code `true`} literal or {@code `false`} literal.
     *
     * @return {@code true} if this expression is a {@code `true`} of {@code `false`} literal,
     *         {@code false} otherwise
     */
    public boolean isConstant() {
        return literalValue != null;
    }

    /**
     * Obtains the literal value of this constant expression.
     *
     * <p>Throws an {@code IllegalStateException} if this expression is not a literal.
     *
     * @return {@code true} if this expression is a {@code `true`} literal, {@code false} if its
     *         a {@code `false`} literal
     * @see #isConstant()
     */
    public boolean isConstantTrue() {
        checkState(literalValue != null, "`%s` is not a literal.", this);
        return literalValue;
    }

    /**
     * Obtains an expression which yields the opposite boolean value.
     *
     * <p>If this expression is a literal, obtains the other literal.
     *
     * @return the negated expression
     */
    public BooleanExpression negate() {
        if (this.equals(TRUE)) {
            return FALSE;
        } else if (this.equals(FALSE)) {
            return TRUE;
        } else {
            return fromCode("!($L)", this);
        }
    }

    /**
     * Obtains an expression which yields the result of {@code &&} (AND) operation between this
     * expression and the {@code other operation}.
     *
     * @param other
     *         the second operand
     * @return {@code this && other}
     */
    public BooleanExpression and(BooleanExpression other) {
        if (this.isConstant()) {
            return this.isConstantTrue() ? other : falseLiteral();
        }
        if (other.isConstant()) {
            return other.isConstantTrue() ? this : falseLiteral();
        }
        return fromCode("($L && $L)", this, other);
    }

    /**
     * Obtains an expression which yields the result of {@code ||} (OR) operation between this
     * expression and the {@code other operation}.
     *
     * @param other
     *         the second operand
     * @return {@code this || other}
     */
    public BooleanExpression or(BooleanExpression other) {
        if (this.isConstant()) {
            return this.isConstantTrue() ? trueLiteral() : other;
        }
        if (other.isConstant()) {
            return other.isConstantTrue() ? trueLiteral() : this;
        }
        return fromCode("($L || $L)", this, other);
    }
}
