package io.spine.validate;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.test.Verify.assertNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Dmytro Dashenkov
 */
public abstract class FieldValidatorShould<V> {

    protected abstract FieldValidator<V> validatedRequiredRepeatedField(ImmutableList<V> values);

    protected abstract FieldValidator<V> requiredRepeatedField(ImmutableList<V> values);

    protected abstract FieldValidator<V> validatedRepeatedField(ImmutableList<V> values);

    protected abstract FieldValidator<V> uncheckedRepeatedField(ImmutableList<V> values);


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

    private static <T> void assertEmpty(Collection<T> emptyIterable) {
        assertThat(emptyIterable, Matchers.<T>empty());
    }
}
