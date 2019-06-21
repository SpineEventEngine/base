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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.option.GoesOption;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.FieldValue;
import io.spine.validate.MessageValue;

import java.util.Optional;

import static io.spine.protobuf.TypeConverter.toAny;
import static io.spine.validate.FieldValidator.errorMsgFormat;

/**
 * A constraint which checks whether a field is set only if the specific related field is also set.
 *
 * @param <T>
 *         type of the field value being checked
 */
@Immutable
public class GoesConstraint<@ImmutableTypeParameter T> implements Constraint<FieldValue<T>> {

    private final MessageValue messageValue;
    private final GoesOption option;

    /**
     * Creates a constraint for the supplied {@code message} with a specified {@code goes} option.
     */
    GoesConstraint(MessageValue messageValue, GoesOption option) {
        this.messageValue = messageValue;
        this.option = option;
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<T> value) {
        ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();
        Optional<FieldDeclaration> withField = getWithField(messageValue, option);
        if (!withField.isPresent()) {
            ConstraintViolation fieldNotFound = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat("Field named `%s` is not found.")
                    .addParam(option.getWith())
                    .setFieldPath(value.context()
                                       .fieldPath())
                    .setFieldValue(toAny(value.singleValue()))
                    .build();
            violations.add(fieldNotFound);
        } else {
            FieldDeclaration declaration = withField.get();
            Optional<FieldValue<?>> withFieldValue = messageValue.valueOf(declaration.descriptor());
            if (withFieldValue.isPresent()
                    && withFieldValue.get()
                                     .isDefault()
                    && !value.isDefault()) {

                String msgFormat = errorMsgFormat(option, option.getMsgFormat());
                ConstraintViolation withFieldNotSet = ConstraintViolation
                        .newBuilder()
                        .setMsgFormat(msgFormat)
                        .addParam(value.declaration()
                                       .name()
                                       .value())
                        .addParam(option.getWith())
                        .setFieldPath(value.context()
                                           .fieldPath())
                        .setFieldValue(toAny(value.singleValue()))
                        .build();
                violations.add(withFieldNotSet);
            }
        }
        return violations.build();
    }

    private static Optional<FieldDeclaration>
    getWithField(MessageValue messageValue, GoesOption goesOption) {
        FieldName withField = FieldName.of(goesOption.getWith());
        for (FieldDeclaration field : messageValue.declaration()
                                                  .fields()) {
            if (withField.equals(field.name())) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }
}
