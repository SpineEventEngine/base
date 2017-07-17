package io.spine.validate;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.test.Verify.assertNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
public abstract class FieldValidatorShould<V> {

    /**
     * Creates a {@link FieldValidator} for a repeated required validated field.
     *
     * <p>The proto description of this field looks as follows:
     * {repeated T field_name = 42 [(required) = true, (valid) = true];}, where {@code T} is
     * the type parameter of the tested {@link FieldValidator}.
     *
     * @param values the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> validatedRequiredRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated required but not validated field.
     *
     * <p>The proto description of this field looks as follows:
     * {repeated T field_name = 42 [(required) = true;}, where {@code T} is the type parameter of
     * the tested {@link FieldValidator}.
     *
     * @param values the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> requiredRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated validated but not required field.
     *
     * <p>The proto description of this field looks as follows:
     * {repeated T field_name = 42 [(valid) = true];}, where {@code T} is the type parameter of
     * the tested {@link FieldValidator}.
     *
     * @param values the list of the field values
     * @return new instance of {@link FieldValidator}
     */
    protected abstract FieldValidator<V> validatedRepeatedFieldValidator(List<V> values);

    /**
     * Creates a {@link FieldValidator} for a repeated non-required non-validated field.
     *
     * <p>The proto description of this field looks as follows:
     * {repeated T field_name = 42;}, where {@code T} is the type parameter of the tested
     * {@link FieldValidator}.
     *
     * @param values the list of the field values
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
    protected abstract FieldValidator<V> mapFieldValidator();

    /**
     * Generates a new non-default valid value of the type of the validated field.
     */
    protected abstract V newValue();

    /**
     * Generates the default value of the type of the validated field.
     */
    protected abstract V defaultValue();

    @Test
    public void validate_repeated_fields_if_specified() {
        final FieldValidator<V> validator = validatedRequiredRepeatedFieldValidator(of(newValue(),
                                                                                       defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertNotEmpty(violations);
    }

    @Test
    public void skip_repeated_fields_if_not_specified() {
        final FieldValidator<V> validator = uncheckedRepeatedFieldValidator(of(defaultValue(),
                                                                               defaultValue(),
                                                                               defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_fields_if_not_required() {
        final FieldValidator<V> validator = uncheckedRepeatedFieldValidator(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_validated_not_required_fields() {
        final FieldValidator<V> validator = validatedRepeatedFieldValidator(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void not_validate_elements_of_repeated_field() {
        final FieldValidator<V> validator = requiredRepeatedFieldValidator(of(defaultValue(),
                                                                              defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void flag_repeated_fields() {
        final FieldValidator<?> validator = uncheckedRepeatedFieldValidator(Collections.<V>emptyList());
        assertTrue(validator.isRepeatedOrMap());
    }

    @Test
    public void flag_map_fields() {
        final FieldValidator<?> validator = mapFieldValidator();
        assertTrue(validator.isRepeatedOrMap());
    }

    private static <T> void assertEmpty(Collection<T> emptyIterable) {
        assertThat(emptyIterable, Matchers.<T>empty());
    }
}
