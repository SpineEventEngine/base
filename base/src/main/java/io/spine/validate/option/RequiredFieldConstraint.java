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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;
import io.spine.validate.Alternative;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A constraint that, when applied to a message, checks whether the specified combination of fields
 * has non-default values.
 */
@Immutable
public final class RequiredFieldConstraint implements Constraint {

    private final String optionValue;
    private final MessageType messageType;
    private final ImmutableSet<Alternative> alternatives;

    RequiredFieldConstraint(String optionValue, MessageType messageType) {
        this.optionValue = checkNotNull(optionValue);
        this.messageType = checkNotNull(messageType);
        checkNotNull(optionValue);
        this.alternatives = Alternative.parse(optionValue, messageType);
    }

    @Override
    public MessageType targetType() {
        return messageType;
    }

    @Override
    public String errorMessage(FieldContext field) {
        return format("Field named `%s` is not found.", field.targetDeclaration());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRequiredField(this);
    }

    public String optionValue() {
        return optionValue;
    }

    public ImmutableSet<Alternative> alternatives() {
        return alternatives;
    }
}
