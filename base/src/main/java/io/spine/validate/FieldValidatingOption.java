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

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.FieldOption;

import java.util.Optional;

import static io.spine.validate.rule.ValidationRuleOptions.getOptionValue;
import static java.lang.String.format;

/**
 * An option that validates a field.
 *
 * <p>Validating options impose constraint on fields that they are applied to.
 *
 * @param <T>
 *         type of value held by this option
 * @param <F>
 *         type of field that this option is applied to
 */
abstract class FieldValidatingOption<T, F>
        extends FieldOption<T>
        implements ValidatingOption<T, FieldDescriptor, FieldValue<F>> {

    /** Specifies the extension that corresponds to this option. */
    protected FieldValidatingOption(GeneratedExtension<FieldOptions, T> optionExtension) {
        super(optionExtension);
    }

    /**
     * Returns an value of the option.
     *
     * @apiNote Should only be called by subclasses in circumstances that assume presence of
     *         the option. For all other cases refer to {@link this#valueFrom(FieldDescriptor)}.
     */
    T optionValue(FieldValue<F> value) throws IllegalStateException {
        FieldDescriptor field = value.declaration()
                                     .descriptor();
        Optional<T> option = valueFrom(field);
        return option.orElseThrow(() -> {
            FieldDescriptor descriptor = extension().getDescriptor();

            String fieldName = value.declaration()
                                    .name()
                                    .value();
            String containingTypeName = descriptor.getContainingType()
                                                  .getName();
            return illegalState(fieldName, containingTypeName);
        });
    }

    private IllegalStateException illegalState(String fieldName, String containingTypeName) {
        String optionName = extension().getDescriptor()
                                       .getName();
        String message = format("Could not get value of option %s from field %s in message %s.",
                                optionName,
                                fieldName,
                                containingTypeName);
        return new IllegalStateException(message);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Apart from the value of the field option, checks whether any messages with {@code
     * validation_for} options exist that override the option value of the specified field.
     *
     * @param field
     *         a field that bears the option value
     * @return either an empty {@code Optional}, if no option value was found for the specified
     *         field,
     *         or an {@code Optional} containing found value
     */
    @Override
    public Optional<T> valueFrom(FieldDescriptor field) {
        FieldContext context = FieldContext.create(field);
        Optional<T> validationForOption = getOptionValue(context, extension());
        return validationForOption.isPresent()
               ? validationForOption
               : super.valueFrom(field);
    }

    /**
     * Returns {@code true} if this option exists for the specified field, {@code false} otherwise.
     *
     * @param field
     *         the type of the field
     */
    boolean shouldValidate(FieldDescriptor field) {
        return valueFrom(field).isPresent();
    }
}
