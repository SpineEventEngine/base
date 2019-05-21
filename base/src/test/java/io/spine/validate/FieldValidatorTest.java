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
import io.spine.code.proto.FieldDeclaration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link io.spine.validate.FieldValidator}.
 */
public abstract class FieldValidatorTest<V> {

    /**
     * Creates a {@link io.spine.validate.FieldValidator} for a repeated required field, which child
     * items are also
     * validated separately.
     *
     * <p>The proto description of this field looks as follows:
     * {@code repeated T field_name = 42 [(required) = true, (validate) = true];}, where {@code T} is
     * the type parameter of the tested {@link io.spine.validate.FieldValidator}.
     *
     * @param values
     *         the list of the field values
     * @return new instance of {@link io.spine.validate.FieldValidator}
     */
    protected abstract FieldValidator<V> validatedRequiredRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated required field, which child items are not
     * validated separately.
     *
     * <p>The proto description of this field looks as follows:
     * {@code repeated T field_name = 42 [(required) = true;}, where {@code T} is the type parameter
     * of the tested {@link FieldValidator}.
     *
     * @param values
     *         the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> requiredRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated validated but not required field.
     *
     * <p>The proto description of this field looks as follows:
     * {@code repeated T field_name = 42 [(validate) = true];}, where {@code T} is the type parameter
     * of the tested {@link FieldValidator}.
     *
     * @param values
     *         the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> validatedRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated non-required non-validated field.
     *
     * <p>The proto description of this field looks as follows:
     * {@code repeated T field_name = 42;}, where {@code T} is the type parameter of the tested
     * {@link FieldValidator}.
     *
     * @param values
     *         the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> uncheckedRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a map field.
     *
     * <p>The resulting validator should be empty (i.e. contain no values).
     *
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> emptyMapFieldValidator();

    /**
     * Generates a new non-default valid value of the type of the validated field.
     */
    protected abstract V newValue();

    /**
     * Generates the default value of the type of the validated field.
     */
    protected abstract V defaultValue();

    @Test
    @DisplayName("validate repeated fields if specified")
    void validate_repeated_fields_if_specified() {
        FieldValidator<V> validator = validatedRequiredRepeatedFieldValidator(
                ImmutableList.of(newValue(), defaultValue())
        );
        List<ConstraintViolation> violations = validator.validate();

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("skip repeated fields if not specified")
    void skip_repeated_fields_if_not_specified() {
        FieldValidator<V> validator = uncheckedRepeatedFieldValidator(
                ImmutableList.of(defaultValue(), defaultValue(), defaultValue())
        );
        List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    @DisplayName("skip empty repeated fields if not required")
    void skip_empty_repeated_fields_if_not_required() {
        FieldValidator<V> validator = uncheckedRepeatedFieldValidator(ImmutableList.of());
        List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    @DisplayName("skip empty repeated validated not required fields")
    void skip_empty_repeated_validated_not_required_fields() {
        FieldValidator<V> validator = validatedRepeatedFieldValidator(ImmutableList.of());
        List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    @DisplayName("not validate elements of repeated fields")
    void not_validate_elements_of_repeated_field() {
        FieldValidator<V> validator = requiredRepeatedFieldValidator(
                ImmutableList.of(defaultValue(), defaultValue())
        );
        List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    @DisplayName("flag repeated fields")
    void flag_repeated_fields() {
        FieldValidator<?> validator = uncheckedRepeatedFieldValidator(ImmutableList.of());
        assertNotScalar(validator);
    }

    @Test
    @DisplayName("flag map fields")
    void flag_map_fields() {
        FieldValidator<?> validator = emptyMapFieldValidator();
        assertNotScalar(validator);
    }

    private static void assertNotScalar(FieldValidator<?> validator) {
        FieldDeclaration declaration = validator.field();
        assertTrue(declaration.isCollection());
    }

    private static <T> void assertEmpty(Collection<T> emptyIterable) {
        assertThat(emptyIterable, Matchers.empty());
    }
}
