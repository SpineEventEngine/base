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
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.FieldOption;
import io.spine.validate.rule.ValidationRuleOptions;

import java.util.Optional;

/**
 * An option that validates a field.
 *
 * <p>Validating options impose constraint on fields that they are applied to.
 *
 * @param <P>
 *         type of information that this option holds
 */
abstract class FieldValidatingOption<P, F> extends FieldOption<P, F>
        implements ValidatingOption<P, FieldValue<F>> {

    /** Specifies the extension that corresponds to this option. */
    protected FieldValidatingOption(GeneratedExtension<FieldOptions, P> optionExtension) {
        super(optionExtension);
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
    public Optional<P> valueFrom(FieldValue<F> field) {
        FieldContext context = field.context();
        Optional<P> validationForOption = ValidationRuleOptions.getOptionValue(context,
                                                                               optionExtension());
        return validationForOption.isPresent()
               ? validationForOption
               : super.valueFrom(field);
    }

    /** Returns {@code true} if this option exists for the specified field, {@code false} otherwise. */
    boolean shouldValidate(FieldValue<F> fieldValue) {
        return valueFrom(fieldValue).isPresent();
    }
}
