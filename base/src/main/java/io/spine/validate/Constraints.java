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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FieldContext;
import io.spine.type.MessageType;
import io.spine.validate.option.RequiredField;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Constraints {

    private final ImmutableList<Constraint> constraints;

    private Constraints(ImmutableList<Constraint> constraints) {
        this.constraints = constraints;
    }

    public static Constraints of(MessageType type) {
        return of(type, FieldContext.empty());
    }

    public static Constraints of(MessageType type, FieldContext context) {
        checkNotNull(type);
        checkNotNull(context);
        ImmutableList.Builder<Constraint> constraintBuilder = ImmutableList.builder();
        fieldConstraints(type, context)
                .forEach(constraintBuilder::add);
        RequiredField requiredField = new RequiredField();
        if (requiredField.valuePresent(type.descriptor())) {
            Constraint requiredFieldConstraint = requiredField.constraintFor(type);
            constraintBuilder.add(requiredFieldConstraint);
        }
        return new Constraints(constraintBuilder.build());
    }

    private static Stream<Constraint> fieldConstraints(MessageType type, FieldContext context) {
        return type
                .fields()
                .stream()
                .map(field -> context.forChild(field.descriptor()))
                .flatMap(FieldConstraints::of);
    }

    public <T> T runThrough(ConstraintTranslator<T> constraintTranslator) {
        constraints.forEach(c -> c.accept(constraintTranslator));
        return constraintTranslator.translate();
    }
}
