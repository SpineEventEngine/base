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
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.GoesOption;
import io.spine.type.MessageType;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;

import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint which checks whether a field is set only if the specific related field is also set.
 */
@Immutable
public final class GoesConstraint implements Constraint {

    private final FieldDeclaration declaringField;
    private final GoesOption option;

    /**
     * Creates a constraint for the supplied {@code message} with a specified {@code goes} option.
     */
    GoesConstraint(FieldDeclaration declaringField, GoesOption option) {
        this.declaringField = declaringField;
        this.option = option;
    }

    @Override
    public MessageType targetType() {
        return declaringField.declaringType();
    }

    @Override
    public String errorMessage(FieldContext field) {
        return errorMsgFormat(option, option.getMsgFormat());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitGoesWith(this);
    }

    public GoesOption option() {
        return option;
    }

    public FieldDeclaration field() {
        return declaringField;
    }
}
