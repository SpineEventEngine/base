package io.spine.validate;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static io.spine.test.Verify.assertNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;

/**
 * @author Dmytro Dashenkov
 */
public abstract class FieldValidatorShould<V> {

    protected abstract FieldValidator<V> getValidatorForValidatedField(ImmutableList<V> values);

    protected abstract FieldValidator<V> getValidatorForUncheckedField(ImmutableList<V> values);

    protected abstract V newValue();

    protected abstract V defaultValue();

    @Test
    public void validate_repeated_fields_if_specified() {
        final FieldValidator<V> validator = getValidatorForValidatedField(of(newValue(),
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
        assertThat(violations, emptyCollectionOf(ConstraintViolation.class));
    }
}
