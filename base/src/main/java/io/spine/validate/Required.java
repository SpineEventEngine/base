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

import java.util.Optional;
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
public final class Required<T> extends FieldValidatingOption<Boolean, T> implements Logging {

    private static final ImmutableSet<JavaType> CAN_BE_REQUIRED = ImmutableSet.of(
            MESSAGE, ENUM, STRING, BYTE_STRING
    );

    private final Predicate<FieldValue<?>> isOptionPresent;
    private final IfMissing ifMissing = new IfMissing();

    /**
     * Creates a new instance of this option.
     *
     * @param isOptionPresent
     *         a function that defines whether this option is present
     */
    private Required(Predicate<FieldValue<?>> isOptionPresent) {
        super(OptionsProto.required);
        this.isOptionPresent = isOptionPresent;
    }

    /**
     * Creates a new instance of this validating option.
     *
     * <p>Depending on the value of the {@code strict} parameter, created option either checks
     * the option value of the field (if {@code strict} is {@code false}), or assumes it to be
     * {@code required} by default (if {@code strict} is {@code true}).
     *
     * @param strict
     *         whether it should be assumed that the field is {@code required} by default
     * @return a new instance of this validating option
     */
    static <T> Required<T> create(boolean strict) {
        Predicate<FieldValue<?>> isOptionPresent = strict ? value -> true : Required::optionValue;
        return new Required<>(isOptionPresent);
    }

    @Override
    boolean optionPresentAt(FieldValue<T> value) {
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

    private static Boolean optionValue(FieldValue<?> fieldValue) {
        return fieldValue.valueOf(OptionsProto.required);

    }

    @Override
    public Optional<Boolean> valueFrom(FieldValue<T> fieldValue) {
        return Optional.of(optionValue(fieldValue));
    }

    @Override
    Constraint<FieldValue<T>> constraint() {
        return new RequiredConstraint<>();
    }
}
