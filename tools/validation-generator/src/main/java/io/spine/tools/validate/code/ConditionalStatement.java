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

public final class ConditionalStatement {

    private final CodeBlock.Builder code;
    private boolean complete = false;

    ConditionalStatement(CodeBlock.Builder code) {
        this.code = checkNotNull(code);
    }

    public CodeBlock toCode() {
        complete();
        code.endControlFlow();
        return code.build();
    }

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
        checkState(!complete, "%s cannot be reused.", ConditionalStatement.class.getSimpleName());
        complete = true;
    }
}
