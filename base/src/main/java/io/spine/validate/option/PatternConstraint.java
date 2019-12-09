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

package io.spine.validate.option;

import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.PatternOption;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.FieldValue;

import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint, which when applied to a string field, checks whether that field matches the
 * specified pattern.
 */
@Immutable
public final class PatternConstraint extends FieldConstraint<PatternOption> {

    PatternConstraint(PatternOption optionValue, FieldDeclaration field) {
        super(optionValue, field);
    }

    @Override
    public String errorMessage(FieldValue value) {
        PatternOption option = optionValue();
        return errorMsgFormat(option, option.getMsgFormat());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitPattern(this);
    }
}
