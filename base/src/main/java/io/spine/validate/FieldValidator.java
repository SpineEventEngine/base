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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;
import io.spine.option.IfInvalidOption;
import io.spine.option.IfMissingOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Validates messages according to Spine custom Protobuf options and
 * provides constraint violations found.
 *
 * @param <V>
 *         a type of field values
 */
abstract class FieldValidator<V> implements Logging {

    private final FieldValue<V> value;
    private final FieldDeclaration declaration;
    private final ImmutableList<V> values;

    private final List<ConstraintViolation> violations = newLinkedList();

    private final Set<FieldValidatingOption<?, V>> fieldValidatingOptions;

    /**
     * If set the validator would assume that the field is required even
     * if the {@code required} option is not set.
     */
    private final boolean assumeRequired;
    private final IfInvalidOption ifInvalid;

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     * @param assumeRequired
     *         if {@code true} the validator would assume that the field is required even
     *         if this constraint is not set explicitly
     * @param validatingOptions
     *         options that validate a field and are relevant to more concrete subtypes
     */
    protected FieldValidator(FieldValue<V> fieldValue,
                             boolean assumeRequired,
                             Set<FieldValidatingOption<?, V>> validatingOptions) {
        this.value = fieldValue;
        this.declaration = fieldValue.declaration();
        this.values = fieldValue.asList();
        this.ifInvalid = fieldValue.valueOf(OptionsProto.ifInvalid);
        this.assumeRequired = assumeRequired;
        this.fieldValidatingOptions = Sets.union(commonOptions(assumeRequired), validatingOptions);
    }

    /**
     * Checks if the value of the validated field is not set.
     *
     * <p>Works for both repeated/map fields and ordinary single-value fields.
     *
     * @return {@code true} if the field value is not set and {@code false} otherwise
     */
    boolean fieldValueNotSet() {
        boolean valueNotSet =
                values.isEmpty()
                        || (declaration.isNotCollection() && isNotSet(values.get(0)));
        return valueNotSet;
    }

    /**
     * Checks if the specified field value is not set.
     *
     * <p>If the field type is {@link Message}, it must be set to a non-default instance;
     * if it is {@link String} or {@link com.google.protobuf.ByteString ByteString}, it must be
     * set to a non-empty string or array.
     *
     * @param value
     *         a field value to check
     * @return {@code true} if the field is not set, {@code false} otherwise
     */
    protected abstract boolean isNotSet(V value);

    /**
     * Validates messages according to Spine custom protobuf options and returns validation
     * constraint violations found.
     *
     * <p>This method defines the general flow of the field validation. Override
     * {@link #validateOwnRules()} to customize the validation behavior.
     *
     * <p>The flow of the validation is as follows:
     * <ol>
     *     <li>check the field to be set if it is {@code required};
     *     <li>validate the field as an Entity ID if required;
     *     <li>performs the {@linkplain #validateOwnRules() custom type-dependant validation}.
     * </ol>
     *
     * @return a list of found {@linkplain ConstraintViolation constraint violations} is any
     */
    protected final List<ConstraintViolation> validate() {
        if (isRequiredId()) {
            validateEntityId();
        }
        validateOwnRules();
        List<ConstraintViolation> ownViolations = assembleViolations();
        List<ConstraintViolation> optionViolations = optionViolations();
        ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        result.addAll(ownViolations)
              .addAll(optionViolations);
        return result.build();
    }

    final IfInvalidOption ifInvalid() {
        return ifInvalid;
    }

    /**
     * Performs type-specific field validation.
     *
     * <p>Use {@link #addViolation(ConstraintViolation)} method in custom implementations.
     *
     * <p>Do not call this method directly. Use {@link #validate() validate()} instead.
     */
    protected abstract void validateOwnRules();

    private List<ConstraintViolation> assembleViolations() {
        return ImmutableList.<ConstraintViolation>builder()
                .addAll(violations)
                .build();
    }

    private List<ConstraintViolation> optionViolations() {
        List<ConstraintViolation> violations =
                this.fieldValidatingOptions.stream()
                                           .filter(option -> option.shouldValidate(value))
                                           .map(ValidatingOption::constraint)
                                           .flatMap(constraint -> constraint.check(value).stream())
                                           .collect(Collectors.toList());
        return violations;
    }

    /**
     * Validates the current field as it is a required entity ID.
     *
     * <p>The field must not be repeated or not set.
     *
     * @see #isRequiredId()
     */
    protected void validateEntityId() {
        if (declaration.isRepeated()) {
            ConstraintViolation violation = ConstraintViolation
                    .newBuilder()
                    .setMsgFormat("Entity ID field `%s` must not be a repeated field.")
                    .addParam(declaration.descriptor()
                                         .getFullName())
                    .setFieldPath(getFieldPath())
                    .build();
            addViolation(violation);
            return;
        }
        if (fieldValueNotSet()) {
            addViolation(newViolation(value.valueOf(OptionsProto.ifMissing)));
        }
    }

    FieldValue<V> fieldValue() {
        return this.value;
    }

    /**
     * Returns {@code true} if the field has required attribute or validation is strict.
     */
    protected boolean isRequiredField() {
        boolean result = value.valueOf(OptionsProto.required) || assumeRequired;
        return result;
    }

    /** Returns an immutable list of the field values. */
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // is immutable list
    protected ImmutableList<V> getValues() {
        return values;
    }

    /**
     * Adds a validation constraint validation to the collection of violations.
     *
     * @param violation
     *         a violation to add
     */
    protected void addViolation(ConstraintViolation violation) {
        violations.add(violation);
    }

    private ConstraintViolation newViolation(IfMissingOption option) {
        String msg = getErrorMsgFormat(option, option.getMsgFormat());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .setFieldPath(getFieldPath())
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
    static String getErrorMsgFormat(Message option, String customMsg) {
        String defaultMsg = option.getDescriptorForType()
                                  .getOptions()
                                  .getExtension(OptionsProto.defaultMessage);
        String msg = customMsg.isEmpty() ? defaultMsg : customMsg;
        return msg;
    }

    /**
     * Returns {@code true} if the field is a required ID, {@code false} otherwise.
     */
    private boolean isRequiredId() {
        boolean result = declaration.isCommandId() || isRequiredEntityId();
        return result;
    }

    /**
     * Determines whether the field is a required
     * {@linkplain FieldDeclaration#isEntityId() entity ID}.
     *
     * <p>We have a convention, that an entity ID is required by default.
     * The ID is not required only if its declaration is marked with {@code [(required)=false]}.
     *
     * @return {@code true} if the field is a required entity ID, {@code false} otherwise
     */
    private boolean isRequiredEntityId() {
        boolean requiredSetExplicitly = value.option(OptionsProto.required)
                                             .isExplicitlySet();
        boolean notRequired = !value.valueOf(OptionsProto.required) && requiredSetExplicitly;
        return declaration.isEntityId() && !notRequired;
    }

    /**
     * Obtains field context for the validator.
     *
     * @return the field context
     */
    protected FieldContext getFieldContext() {
        return value.context();
    }

    /** Returns a path to the current field. */
    protected FieldPath getFieldPath() {
        return getFieldContext().getFieldPath();
    }

    /** Returns the declaration of the validated field. */
    protected FieldDeclaration field() {
        return declaration;
    }

    private Set<FieldValidatingOption<?, V>> commonOptions(boolean strict) {
        return ImmutableSet.of(Distinct.create(),
                               new Required<>(strict));
    }
}
