package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors;
import io.spine.base.FieldPath;

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
     * @param descr         a descriptor of the field to validate
     * @param rootFieldPath a path to the root field (if present)
     * @param strict        if {@code true} the validator would assume that the field is required,
     */
    protected EmptyMapFieldValidator(Descriptors.FieldDescriptor descr,
                                     FieldPath rootFieldPath,
                                     boolean strict) {
        super(descr, ImmutableList.<Map<?, ?>>of(), rootFieldPath, strict);
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
