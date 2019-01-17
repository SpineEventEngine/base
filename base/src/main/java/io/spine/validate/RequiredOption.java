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
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.spine.option.IfMissingOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.function.Predicate;

/**
 * An option that makes a field {@code required}.
 *
 * <p>If a {@code required} field is missing, an error is produced.
 */
public class RequiredOption extends FieldValidatingOption<Boolean> {

    private final Predicate<FieldValue> isNotSet;
    private final Predicate<FieldValue> isOptionPresent;

    /**
     * Creates a new instance of this option.
     *
     * @param isNotSet
     *         a function that defines whether a field value is set or not
     * @param isOptionPresent
     *         a function that defines whether this option is present
     */
    private RequiredOption(Predicate<FieldValue> isNotSet,
                           Predicate<FieldValue> isOptionPresent) {
        this.isNotSet = isNotSet;
        this.isOptionPresent = isOptionPresent;
    }

    static RequiredOption create(Predicate<FieldValue> isNotSet,
                                 boolean strict) {
        Predicate<FieldValue> isOptionPresent = strict ? value -> true : RequiredOption::optionValue;
        return new RequiredOption(isNotSet, isOptionPresent);
    }

    /**
     * @inheritDoc <p>Any field can be {@code required}.
     */
    @Override
    boolean applicableTo(Descriptors.FieldDescriptor field) {
        return true;
    }

    @Override
    boolean optionPresentFor(FieldValue value) {
        return this.isOptionPresent.test(value);
    }

    /**
     * Any field can be {@code required}, so this method is never called.
     */
    @Override
    ValidationException onInapplicable(Descriptors.FieldDescriptor field) {
        return nop();
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue value) {
        if (isNotSet.test(value)) {
            return requiredViolated(value);
        }
        return ImmutableList.of();
    }

    private static List<ConstraintViolation> requiredViolated(FieldValue value) {
        IfMissing ifMissing = new IfMissing();
        return ImmutableList.of(newViolation(ifMissing.getValueFor(value), value));
    }

    private static ConstraintViolation newViolation(IfMissingOption option, FieldValue value) {
        String msg = getErrorMsgFormat(option, option.getMsgFormat());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .setFieldPath(value.context().getFieldPath())
                .build();
        return violation;
    }

    /**
     * Returns a validation error message (a custom one (if present) or the default one).
     *
     * @param option
     *         a validation option used to get the default message
     * @param customMsg
     *         a user-defined error message
     */
    private static String getErrorMsgFormat(Message option, String customMsg) {
        String defaultMsg = option.getDescriptorForType()
                                  .getOptions()
                                  .getExtension(OptionsProto.defaultMessage);
        String msg = customMsg.isEmpty() ? defaultMsg : customMsg;
        return msg;
    }

    private static Boolean optionValue(FieldValue fieldValue) {
        return fieldValue.valueOf(OptionsProto.required);
    }

    @Override
    public Boolean getValueFor(FieldValue fieldValue) {
        return optionValue(fieldValue);
    }

    private <T> T nop() {
        return null;
    }
}
