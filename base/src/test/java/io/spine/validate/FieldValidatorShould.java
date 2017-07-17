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

    protected abstract FieldValidator<V> validatedRequiredRepeatedField(List<V> values);

    protected abstract FieldValidator<V> requiredRepeatedField(List<V> values);

    protected abstract FieldValidator<V> validatedRepeatedField(List<V> values);

    protected abstract FieldValidator<V> uncheckedRepeatedField(List<V> values);

    protected abstract FieldValidator<V> mapField();

    protected abstract V newValue();

    protected abstract V defaultValue();

    @Test
    public void validate_repeated_fields_if_specified() {
        final FieldValidator<V> validator = validatedRequiredRepeatedField(of(newValue(),
                                                                              defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertNotEmpty(violations);
    }

    @Test
    public void skip_repeated_fields_if_not_specified() {
        final FieldValidator<V> validator = uncheckedRepeatedField(of(defaultValue(),
                                                                      defaultValue(),
                                                                      defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_fields_if_not_required() {
        final FieldValidator<V> validator = uncheckedRepeatedField(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_validated_not_required_fields() {
        final FieldValidator<V> validator = validatedRepeatedField(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void not_validate_elements_of_repeated_field() {
        final FieldValidator<V> validator = requiredRepeatedField(of(defaultValue(),
                                                                     defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void flag_repeated_fields() {
        final FieldValidator<?> validator = uncheckedRepeatedField(Collections.<V>emptyList());
        assertTrue(validator.isRepeatedOrMap());
    }

    @Test
    public void flag_map_fields() {
        final FieldValidator<?> validator = mapField();
        assertTrue(validator.isRepeatedOrMap());
    }

    private static <T> void assertEmpty(Collection<T> emptyIterable) {
        assertThat(emptyIterable, Matchers.<T>empty());
    }
}
