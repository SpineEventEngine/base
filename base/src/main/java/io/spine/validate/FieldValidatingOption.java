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
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldOption;

import java.util.Optional;

import static io.spine.validate.ExternalConstraintOptions.getOptionValue;
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
public abstract class FieldValidatingOption<T, F>
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
     *         the option. For all other cases please refer to
     *         {@link #valueFrom(com.google.protobuf.Descriptors.FieldDescriptor, FieldContext)}.
     */
    protected T optionValue(FieldValue<F> value) throws IllegalStateException {
        FieldDescriptor field = value.declaration()
                                     .descriptor();
        FieldContext context = value.context();
        Optional<T> option = valueFrom(field, context);
        return option.orElseThrow(() -> {
            FieldDescriptor descriptor = extension().getDescriptor();

            String fieldName = value.declaration()
                                    .name()
                                    .value();
            String containingTypeName = descriptor.getContainingType()
                                                  .getName();
            return couldNotGetOptionValueFrom(fieldName, containingTypeName);
        });
    }

    private IllegalStateException couldNotGetOptionValueFrom(String fieldName,
                                                             String containingTypeName) {
        String optionName = extension().getDescriptor()
                                       .getName();
        String message = format("Could not get value of option %s from field %s in message %s.",
                                optionName,
                                fieldName,
                                containingTypeName);
        return new IllegalStateException(message);
    }

    /**
     * Takes the value of the option from the given descriptor, given the specified context.
     *
     * @param field
     *         descriptor of the field
     * @param context
     *         context of the field
     * @return an {@code Optional} with an option value, if such exists, otherwise an empty
     *         {@code Optional}
     * @apiNote Use this in favour of {@link
     *         FieldOption#optionsFrom(com.google.protobuf.Descriptors.FieldDescriptor)
     *         optionsFrom(FieldDescriptor)} when {@code FieldContext} matters, e.g. when handling
     *         {@code (validation_for)} options.
     */
    public Optional<T> valueFrom(FieldDescriptor field, FieldContext context) {
        Optional<T> value = getOptionValue(context, extension());
        return value.isPresent()
               ? value
               : valueFrom(field);
    }

    /**
     * Returns {@code true} if this option exists for the specified field, {@code false} otherwise.
     *
     * @param field
     *         the type of the field
     */
    protected boolean shouldValidate(FieldDescriptor field) {
        return valueFrom(field).isPresent();
    }
}
