package io.spine.validate;

import com.google.common.collect.ImmutableList;

import java.util.Map;

/**
 * Performs the validation for a {@code map} field which has no values set.
 *
 * @author Dmytro Dashenkov
 */
final class EmptyMapFieldValidator extends FieldValidator<Map<?, ?>> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldContext the context of the field to validate
     * @param strict       if {@code true} the validator would assume that the field
     *                     is required even if the corresponding option is not set
     */
    EmptyMapFieldValidator(FieldContext fieldContext, boolean strict) {
        super(fieldContext, ImmutableList.<Map<?, ?>>of(), strict);
    }

    @Override
    protected boolean isValueNotSet(Map<?, ?> value) {
        return value.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs no action, as there are no specific rules for an empty map validation.
     */
    @Override
    protected void validateOwnRules() {
        // NoOp
    }
}
