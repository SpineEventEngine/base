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
import io.spine.option.OptionsProto;
import io.spine.validate.Constraint;

/**
 * An option that indicates that the message field should be validated according to its constraints.
 */
@Immutable
public final class Valid extends FieldValidatingOption<Boolean> {

    /** Creates a new instance of this option. */
    public Valid() {
        super(OptionsProto.validate);
    }

    @Override
    public Constraint constraintFor(FieldContext field) {
        boolean shouldValidate = optionValue(field);
        return new ValidateConstraint(shouldValidate, field.targetDeclaration());
    }

    @Override
    public boolean shouldValidate(FieldContext context) {
        return valueFrom(context).orElse(false);
    }
}
