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
import com.google.common.collect.UnmodifiableIterator;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;
import io.spine.option.IfInvalidOption;
import io.spine.option.IfMissingOption;
import io.spine.option.OptionsProto;
import io.spine.type.TypeName;
import io.spine.validate.option.Constraint;
import io.spine.validate.option.Distinct;
import io.spine.validate.option.FieldValidatingOption;
import io.spine.validate.option.IfInvalid;
import io.spine.validate.option.IfMissing;
import io.spine.validate.option.Required;
import io.spine.validate.option.ValidatingOptionFactory;
import io.spine.validate.option.ValidatingOptionsLoader;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterators.unmodifiableIterator;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * Validates messages according to Spine custom Protobuf options and
 * provides constraint violations found.
 *
 * @param <V>
 *         a type of field values
 */
@SuppressWarnings("ClassWithTooManyMethods") // OK for this central class.
public abstract class FieldValidator<V> implements Logging {

    private final FieldValue value;
    private final FieldDeclaration declaration;
    private final ImmutableList<V> values;

    private final List<ConstraintViolation> violations = newLinkedList();

    private final UnmodifiableIterator<FieldValidatingOption<?, V>> fieldValidatingOptions;

    /**
     * If set the validator would assume that the field is required even
     * if the {@code required} option is not set.
     */
    private final boolean assumeRequired;

    /**
     * An option holding the text required if the validated {@code Message} is invalid.
     *
     * <p>As soon as this isn't happening for every validated {@code Message}, this field
     * is {@code null} until someone {@linkplain #ifInvalid() requests it}.
     */
    private @MonotonicNonNull IfInvalidOption ifInvalid;

    /**
     * Creates a new validator instance.
     *
     * @param value
     *         the value to validate
     * @param assumeRequired
     *         if {@code true} the validator would assume that the field is required regardless
     *         of the {@code required} Protobuf option value
     */
    protected FieldValidator(FieldValue value, boolean assumeRequired) {
        this.value = value;
        this.declaration = value.declaration();
        this.values = (ImmutableList<V>) value.asList();
        this.assumeRequired = assumeRequired;
        this.fieldValidatingOptions = validatingOptions();
    }

    /**
     * Collects the validation options to use when reading option values.
     *
     * <p>Does not include options from the external constraints.
     *
     * @implNote If the {@code Required} option is assumed to be required, it is always
     *         present in the results. In case no options are defined for the field,
     *         the result remains empty.
     *         This is a performance optimization made to avoid redundant reflective calls
     *         to Protobuf data attempting to read the missing option values.
     */
    @SuppressWarnings("Immutable") // message field values are immutable
    private UnmodifiableIterator<FieldValidatingOption<?, V>> validatingOptions() {
        List<FieldValidatingOption<?, V>> allOptions = new ArrayList<>();

        if (assumeRequired) {
            allOptions.add(Required.create(true));
        }

        if (fieldHasOptions()) {
            if (values.size() > 1) {
                allOptions.add(Distinct.create());
            }

            // Add the option if it wasn't added already.
            if (!assumeRequired) {
                allOptions.add(Required.create(false));
            }

            ImmutableSet<ValidatingOptionFactory> factories =
                    ValidatingOptionsLoader.INSTANCE.implementations();
            for (ValidatingOptionFactory factory : factories) {
                Set<FieldValidatingOption<?, V>> options = createMoreOptions(factory);
                allOptions.addAll(options);
            }
        }
        return unmodifiableIterator(allOptions.iterator());
    }

    private boolean fieldHasOptions() {
        return field().descriptor()
                      .toProto()
                      .hasOptions();
    }

    protected abstract Set<FieldValidatingOption<?, V>> createMoreOptions(
            ValidatingOptionFactory factory);

    /**
     * Checks if the value of the validated field is not set.
     *
     * <p>Works for both repeated/map fields and ordinary single-value fields.
     *
     * @return {@code true} if the field value is not set and {@code false} otherwise
     */
    protected final boolean fieldValueNotSet() {
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
     * <p>The flow of the validation is as follows:
     * <ol>
     *     <li>check the field to be set if it is {@code required};
     *     <li>validate the field as an Entity ID if required;
     *     <li>perform type-specific validation according to validation options.
     * </ol>
     *
     * @return a list of found {@linkplain ConstraintViolation constraint violations} if any
     */
    public ImmutableList<ConstraintViolation> validate() {
        if (isRequiredId()) {
            validateEntityId();
        }
        ImmutableList.Builder<ConstraintViolation> result = ImmutableList.builder();
        result.addAll(violations);

        while (fieldValidatingOptions.hasNext()) {
            FieldValidatingOption<?, V> option = fieldValidatingOptions.next();
            if (option.shouldValidate(value)) {
                Constraint constraint = option.constraintFor(value);
                ImmutableList<ConstraintViolation> violations = ImmutableList.of(); // constraint.check(value);
                result.addAll(violations);
            }
        }
        return result.build();
    }

    protected final IfInvalidOption ifInvalid() {
        if (ifInvalid == null) {
            ifInvalid = ifInvalid(descriptor(value));
        }
        return ifInvalid;
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
                    .setFieldPath(fieldPath())
                    .build();
            addViolation(violation);
            return;
        }
        if (fieldValueNotSet()) {
            IfMissingOption ifMissing = ifMissing();
            addViolation(newViolation(ifMissing));
        }
    }

    protected FieldValue fieldValue() {
        return value;
    }

    /**
     * Returns {@code true} if the field has required attribute or validation is strict.
     */
    @SuppressWarnings("Immutable") // message field values are immutable
    protected boolean isRequiredField() {
        Required<V> requiredOption = Required.create(assumeRequired);
        boolean required = requiredOption.valueFrom(descriptor())
                                         .orElse(assumeRequired);
        return required;
    }

    /** Returns an immutable list of the field values. */
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // is immutable list
    protected ImmutableList<V> values() {
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
        String msg = errorMsgFormat(option, option.getMsgFormat());
        TypeName typeName = value.declaration()
                                 .declaringType()
                                 .name();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .setTypeName(typeName.value())
                .setFieldPath(fieldPath())
                .build();
        return violation;
    }

    /**
     * Returns a validation error message which may have formatting placeholders
     *
     * <p>A custom message is returned if it is present in the option. Otherwise,
     * default message is returned.
     *
     * @param option
     *         a validation option used to get the default message
     * @param customMsg
     *         a user-defined error message
     */
    public static String errorMsgFormat(Message option, String customMsg) {
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
    @SuppressWarnings("Immutable") // message field values are immutable
    private boolean isRequiredEntityId() {
        Required<V> requiredOption = Required.create(assumeRequired);
        Optional<Boolean> requiredOptionValue = requiredOption.valueFrom(descriptor());
        boolean notRequired = requiredOptionValue.isPresent() && !requiredOptionValue.get();
        return declaration.isEntityId() && !notRequired;
    }

    private static IfInvalidOption ifInvalid(FieldDescriptor descriptor) {
        IfInvalid ifInvalid = new IfInvalid();
        IfInvalidOption result = ifInvalid.valueOrDefault(descriptor);
        return result;
    }

    private IfMissingOption ifMissing() {
        IfMissing ifMissing = new IfMissing();
        return ifMissing.valueOrDefault(descriptor());
    }

    private static <V> FieldDescriptor descriptor(FieldValue value) {
        return value.declaration()
                    .descriptor();
    }

    protected final FieldDescriptor descriptor() {
        return descriptor(value);
    }

    /**
     * Obtains field context for the validator.
     *
     * @return the field context
     */
    protected final FieldContext fieldContext() {
        return value.context();
    }

    /** Returns a path to the current field. */
    final FieldPath fieldPath() {
        return fieldContext().fieldPath();
    }

    /** Returns the declaration of the validated field. */
    protected final FieldDeclaration field() {
        return declaration;
    }
}
