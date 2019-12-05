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
import static com.google.common.base.Preconditions.checkState;

/**
 * A builder for conditional statements.
 *
 * <p>An instance of {@code ConditionalStatement} is not reusable. See {@link #toCode()} and
 * {@link #orElse(CodeBlock)} doc for the details.
 */
public final class ConditionalStatement {

    private final CodeBlock.Builder code;
    private boolean complete = false;

    /**
     * Creates a new {@code ConditionalStatement}.
     *
     * @param condition
     *         the {@code if} condition
     * @param positiveBranch
     *         the code which should be executed if {@code condition} is {@code true}
     */
    ConditionalStatement(BooleanExpression condition, CodeBlock positiveBranch) {
        checkNotNull(condition);
        checkNotNull(positiveBranch);
        CodeBlock.Builder code = startStatement(condition, positiveBranch);
        this.code = checkNotNull(code);
    }

    private static CodeBlock.Builder startStatement(BooleanExpression condition,
                                                    CodeBlock positiveBranch) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("if ($L)", condition.value());
        code.add(positiveBranch);
        return code;
    }

    /**
     * Converts this statement into Java code.
     *
     * <p>Completes this statement. If the statement is already complete, throws
     * an {@link IllegalStateException}.
     */
    public CodeBlock toCode() {
        complete();
        code.endControlFlow();
        return code.build();
    }

    /**
     * Adds the {@code else} block and converts this statement into Java code.
     *
     * <p>Completes this statement. If the statement is already complete, throws
     * an {@link IllegalStateException}.
     *
     * @param branch
     *         the code which should be executed if the {@code if} condition is {@code false}
     */
    public CodeBlock orElse(CodeBlock branch) {
        return branch.isEmpty()
               ? toCode()
               : alternativeBranch(branch);
    }

    private CodeBlock alternativeBranch(CodeBlock branch) {
        complete();
        code.nextControlFlow("else");
        code.add(branch);
        code.endControlFlow();
        return code.build();
    }

    private void complete() {
        checkState(!complete,
                   "%s is already complete.",
                   ConditionalStatement.class.getSimpleName());
        complete = true;
    }
}
