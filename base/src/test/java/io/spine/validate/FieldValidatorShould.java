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

    protected abstract FieldValidator<V>
    getValidatorForValidatedRequiredField(ImmutableList<V> values);

    protected abstract FieldValidator<V> getValidatorForRequiredField(ImmutableList<V> values);

    protected abstract FieldValidator<V> getValidatorForValidatedField(ImmutableList<V> values);

    protected abstract FieldValidator<V> getValidatorForUncheckedField(ImmutableList<V> values);


    protected abstract V newValue();

    protected abstract V defaultValue();

    @Test
    public void validate_repeated_fields_if_specified() {
        final FieldValidator<V> validator = getValidatorForValidatedRequiredField(of(newValue(),
                                                                                     defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertNotEmpty(violations);
    }

    @Test
    public void skip_repeated_fields_if_not_specified() {
        final FieldValidator<V> validator = getValidatorForUncheckedField(of(defaultValue(),
                                                                             defaultValue(),
                                                                             defaultValue()));
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_fields_if_not_required() {
        final FieldValidator<V> validator = getValidatorForUncheckedField(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    @Test
    public void skip_empty_repeated_validated_not_required_fields() {
        final FieldValidator<V> validator = getValidatorForValidatedField(ImmutableList.<V>of());
        final List<ConstraintViolation> violations = validator.validate();
        assertEmpty(violations);
    }

    private static <T> void assertEmpty(Collection<T> emptyIterable) {
        assertThat(emptyIterable, Matchers.<T>empty());
    }
}
