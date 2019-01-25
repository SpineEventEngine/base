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

import io.spine.option.DigitsOption;
import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * An option that imposes a constraint on a numeric field, checking whether the amount
 * of digits in both whole and decimal parts of the numeric field exceed the specified maximum.
 *
 * @param <N>
 *         Numeric values that this option is applicable to
 */
public class Digits<N extends Number> extends FieldValidatingOption<DigitsOption, N> {

    private Digits() {
        super(OptionsProto.digits);
    }

    /**
     * Creates a new instance of this option.
     *
     * @param <V>
     *         type of value that a field marked with this option has
     * @return new instance of this option
     */
    public static <V extends Number> Digits<V> create() {
        return new Digits<>();
    }

    @Override
    Constraint<FieldValue<N>> constraint() {
        return new DigitsConstraint<>();
    }

    @Override
    public Optional<DigitsOption> valueFrom(FieldValue<N> bearer) {
        DigitsOption option = bearer.valueOf(optionExtension());
        boolean isDefault = option.getFractionMax() == 0 && option.getIntegerMax() == 0;
        return isDefault ? Optional.empty() : Optional.of(option);
    }
}
