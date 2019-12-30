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

package io.spine.test.options;

import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.Constraint;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.option.FieldConstraint;
import io.spine.validate.option.FieldValidatingOption;

import static io.spine.test.options.BytesDirectionOptionProto.direction;

/**
 * A custom validation option for {@code bytes}.
 *
 * <p>This option is used for testing the custom options loading. The constraint produced by this
 * option cannot be violated.
 */
public final class Direction extends FieldValidatingOption<BytesDirection> {

    Direction() {
        super(direction);
    }

    @Override
    public Constraint constraintFor(FieldContext field) {
        FieldDeclaration declaration = field.targetDeclaration();
        BytesDirection optionValue = optionValue(field);
        return new FieldConstraint<BytesDirection>(optionValue, declaration) {
            @Override
            public String errorMessage(FieldContext field) {
                return "";
            }

            @Override
            public void accept(ConstraintTranslator<?> visitor) {
                // NoOp.
            }
        };
    }
}
