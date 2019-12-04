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

import static java.lang.String.format;
import static java.lang.String.valueOf;

public final class BooleanExpression
        extends CodeExpression<Boolean> {

    private static final long serialVersionUID = 0L;

    private static final BooleanExpression FALSE = new BooleanExpression(valueOf(false));

    private BooleanExpression(String value) {
        super(value);
    }

    public static BooleanExpression formatted(String template, Object... args) {
        return new BooleanExpression(format(template, args));
    }

    public static BooleanExpression fromCode(String code, Object... args) {
        CodeBlock block = CodeBlock.of(code, args);
        return new BooleanExpression(block.toString());
    }

    public static BooleanExpression falseLiteral() {
        return FALSE;
    }

    public ConditionalStatement ifTrue(CodeBlock branch) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("if ($L)", value());
        code.add(branch);
        return new ConditionalStatement(code);
    }
}
