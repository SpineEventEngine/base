/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.IfMissingOption;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.diags.ViolationText;

/**
 * A constraint that, when applied to a field, checks whether the field is set to a non-default
 * value.
 */
@Immutable
public final class RequiredConstraint extends FieldConstraint<Boolean> {

    RequiredConstraint(boolean required,
                       FieldDeclaration declaration) {
        super(required, declaration);
    }

    @Override
    public String errorMessage(FieldContext field) {
        IfMissing ifMissing = new IfMissing();
        IfMissingOption option = ifMissing.valueOrDefault(field.target());
        return ViolationText.errorMessage(option, option.getMsgFormat());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRequired(this);
    }
}
