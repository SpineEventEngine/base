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

package io.spine.tools.mc.java.validate;

import com.squareup.javapoet.CodeBlock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.lineSeparator;

/**
 * A builder for {@code if} - {@code else} statements.
 *
 * <p>An instance of {@code ConditionalStatement} is not reusable. See {@link #toCode()} doc for
 * the details.
 */
final class ConditionalStatement {

    private final CodeBlock.Builder code;
    private boolean complete = false;

    /**
     * Creates a new {@code ConditionalStatement}.
     *
     * @param condition
     *         the {@code if} condition
     * @param positiveBranch
     *         the code to be executed if the {@code condition} is {@code true}
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
        code.add(lineSeparator());
        return code;
    }

    /**
     * Adds an alternative branch to this statement.
     *
     * @param condition
     *         the condition of the new {@code if}
     * @param alternative
     *         the code to be executed if the {@code condition} is {@code true}
     * @return {@code this} for method chaining
     */
    public ConditionalStatement elseIf(BooleanExpression condition, CodeBlock alternative) {
        checkNotNull(condition);
        checkNotNull(alternative);

        code.nextControlFlow("else if ($L)", condition);
        code.add(alternative);
        code.add(lineSeparator());
        return this;
    }

    /**
     * Adds the {@code else} branch to this statement.
     *
     * <p>Completes this statement. If the statement is already complete, throws
     * an {@link IllegalStateException}.
     *
     * @param alternativeBranch
     *         the code to be executed if none of the {@code if} conditions is {@code true}
     */
    public CodeBlock orElse(CodeBlock alternativeBranch) {
        code.nextControlFlow("else");
        code.add(alternativeBranch);
        code.add(lineSeparator());
        return toCode();
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

    private void complete() {
        checkState(!complete,
                   "%s is already complete.",
                   ConditionalStatement.class.getSimpleName());
        complete = true;
    }
}
