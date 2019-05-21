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

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.logging.Logging;
import io.spine.option.OptionsProto;
import io.spine.validate.FieldValue;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.BYTE_STRING;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING;

/**
 * An option that makes a field {@code required}.
 *
 * <p>If a {@code required} field is missing, an error is produced.
 */
public class Required<T> extends FieldValidatingOption<Boolean, T> implements Logging {

    private static final ImmutableSet<JavaType> CAN_BE_REQUIRED = ImmutableSet.of(
            MESSAGE, ENUM, STRING, BYTE_STRING
    );

    private final IfMissing ifMissing = new IfMissing();

    /**
     * Creates a new instance of this option.
     */
    Required() {
        super(OptionsProto.required);
    }

    /**
     * Creates a new instance of the {@code Required} option.
     *
     * <p>If the specified parameter is {@code true}, a returned option always assumes a field to
     * be {@code required}, regardless of the field value.
     * If the specified parameter is {@code false}, a returned option checks the actual value.
     *
     * @param strict
     *         specifies if a field is assumed to be a required one regardless of the actual
     *         Protobuf option value
     * @param <T>
     *         type of value that the returned option is applied to
     * @return a new instance of the {@code Required} option
     */
    public static <T> Required<T> create(boolean strict) {
        return strict
               ? new AlwaysRequired<>()
               : new Required<>();
    }

    private boolean notAssumingRequired(FieldDescriptor field) {
        return valueFrom(field).orElse(false);
    }

    @Override
    public boolean shouldValidate(FieldDescriptor value) {
        return notAssumingRequired(value);
    }

    /**
     * Produces warnings if the {@code required} option was applied incorrectly.
     *
     * <p>Examples of incorrect application include attempting to apply the option to a numeric
     * field.
     *
     * @param field
     *         a value that the option is applied to
     */
    void checkUsage(FieldDescriptor field) {
        ifMissing.valueFrom(field)
                 .ifPresent(ifMissingOption -> _warn(
                         "(if_missing) option is set without (required) = true"
                 ));
        checkCanBeRequired(field);
    }

    private void checkCanBeRequired(FieldDescriptor field) {
        JavaType type = field.getJavaType();
        if (!CAN_BE_REQUIRED.contains(type)) {
            String typeName = field.getType().name();
            _warn("Fields of type {} should not be declared as (required) ({}.{}).",
                  typeName,
                  field.getContainingType().getFullName(),
                  field.getName());
        }
    }

    @Override
    public Constraint<FieldValue<T>> constraintFor(FieldValue<T> fieldValue) {
        checkUsage(fieldValue.descriptor());
        return new RequiredConstraint<>(CAN_BE_REQUIRED);
    }
}
