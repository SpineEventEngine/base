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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.logging.Logging;
import io.spine.option.OptionsProto;

import java.util.function.Predicate;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.BYTE_STRING;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING;

/**
 * An option that makes a field {@code required}.
 *
 * <p>If a {@code required} field is missing, an error is produced.
 */
final class Required<T> extends FieldValidatingOption<Boolean, T> implements Logging {

    private static final ImmutableSet<JavaType> CAN_BE_REQUIRED = ImmutableSet.of(
            MESSAGE, ENUM, STRING, BYTE_STRING
    );

    private final Predicate<FieldValue<T>> isOptionPresent;
    private final IfMissing ifMissing = new IfMissing();

    /**
     * Creates a new instance of this option.
     *
     * @param isOptionPresent
     *         a function that defines whether this option is present
     */
    Required(boolean isStrict) {
        super(OptionsProto.required);
        this.isOptionPresent = isStrict
                               ? value -> true
                               : this::notAssumingRequired;
    }

    private Boolean notAssumingRequired(FieldValue<T> fieldValue) {
        return optionValue(fieldValue).value();
    }

    @Override
    boolean shouldValidate(FieldValue<T> value) {
        ifMissing.valueFrom(value)
                 .ifPresent(ifMissingOption -> _warn(
                         "'if_missing' option is set without '(required) = true'"));
        checkCanBeRequired(value);
        return this.isOptionPresent.test(value);
    }

    private void checkCanBeRequired(FieldValue<?> value) {
        JavaType type = value.declaration()
                             .javaType();
        if (!CAN_BE_REQUIRED.contains(type)) {
            String typeName = value.declaration()
                                   .typeName();
            _warn("Fields of type {} should not be declared as `(required)`.", typeName);
        }
    }

    @Override
    boolean isDefault(FieldValue<T> value) {
        return !optionValue(value).isExplicitlySet();
    }

    @Override
    Constraint<FieldValue<T>> constraint() {
        return new RequiredConstraint<>(CAN_BE_REQUIRED);
    }
}
