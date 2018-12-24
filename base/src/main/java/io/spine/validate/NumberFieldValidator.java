/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.option.DecimalMaxOption;
import io.spine.option.DecimalMinOption;
import io.spine.option.DigitsOption;
import io.spine.option.MaxOption;
import io.spine.option.MinOption;
import io.spine.option.OptionsProto;

import java.util.regex.Pattern;

import static io.spine.protobuf.TypeConverter.toAny;

/**
 * Validates fields of number types (protobuf: int32, double, etc).
 *
 * @param <V>
 *         the type of the field value
 */
abstract class NumberFieldValidator<V extends Number & Comparable<V>> extends FieldValidator<V> {

    private static final Pattern PATTERN_DOT = Pattern.compile("\\.");

    private final DecimalMinOption minDecimalOpt;
    private final boolean isMinDecimalInclusive;

    private final DecimalMaxOption maxDecimalOpt;
    private final boolean isMaxDecimalInclusive;

    private final MinOption minOption;
    private final MaxOption maxOption;

    private final DigitsOption digitsOption;

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     */
    NumberFieldValidator(FieldValue fieldValue) {
        super(fieldValue, false);
        this.minDecimalOpt = fieldValue.valueOf(OptionsProto.decimalMin);
        this.isMinDecimalInclusive = minDecimalOpt.getInclusive();
        this.maxDecimalOpt = fieldValue.valueOf(OptionsProto.decimalMax);
        this.isMaxDecimalInclusive = maxDecimalOpt.getInclusive();
        this.minOption = fieldValue.valueOf(OptionsProto.min);
        this.maxOption = fieldValue.valueOf(OptionsProto.max);
        this.digitsOption = fieldValue.valueOf(OptionsProto.digits);
    }

    /** Converts a string representation to a number. */
    protected abstract V toNumber(String value);

    /** Returns an absolute value of the number. */
    protected abstract V getAbs(V number);

    /**
     * Wraps a value to a corresponding message wrapper
     * ({@link com.google.protobuf.DoubleValue DoubleValue},
     * {@link com.google.protobuf.Int32Value Int32Value}, etc) and {@link Any}.
     */
    Any wrap(V value) {
        Any result = toAny(value);
        return result;
    }

    @Override
    protected void validateOwnRules() {
        for (V value : getValues()) {
            validateRangeOptions(value);
            validateDigitsOption(value);
        }
    }

    /**
     * Returns {@code false}.
     *
     * <p>Numeric fields cannot be required, since enforcing their presence is impossible.
     */
    @Override
    boolean canBeRequired() {
        return false;
    }

    /**
     * Returns {@code false}.
     *
     * <p>There's no way to define whether a Protobuf numeric field is {@code 0} or not set.
     */
    @Override
    protected boolean isNotSet(V value) {
        return false;
    }

    private void validateRangeOptions(V value) {
        if (notFitToDecimalMin(value)) {
            addViolation(
                    newDecimalViolation(value, minDecimalOpt, minDecimalOpt.getMsgFormat(),
                                        minDecimalOpt.getInclusive(), minDecimalOpt.getValue()));
        }
        if (notFitToDecimalMax(value)) {
            addViolation(
                    newDecimalViolation(value, maxDecimalOpt, maxDecimalOpt.getMsgFormat(),
                                        maxDecimalOpt.getInclusive(), maxDecimalOpt.getValue()));
        }
        if (notFitToMin(value)) {
            addViolation(newMinOrMaxViolation(value, minOption, minOption.getMsgFormat(),
                                              minOption.getValue()));
        }
        if (notFitToMax(value)) {
            addViolation(newMinOrMaxViolation(value, maxOption, maxOption.getMsgFormat(),
                                              maxOption.getValue()));
        }
    }

    private boolean notFitToDecimalMin(V value) {
        String minAsString = minDecimalOpt.getValue();
        if (minAsString.isEmpty()) {
            return false;
        }
        V min = toNumber(minAsString);
        int comparisonResult = value.compareTo(min);
        boolean fits = isMinDecimalInclusive
                       ? comparisonResult >= 0
                       : comparisonResult > 0;
        boolean notFit = !fits;
        return notFit;
    }

    private boolean notFitToDecimalMax(V value) {
        String maxAsString = maxDecimalOpt.getValue();
        if (maxAsString.isEmpty()) {
            return false;
        }
        V max = toNumber(maxAsString);
        boolean fits = isMaxDecimalInclusive
                       ? value.compareTo(max) <= 0
                       : value.compareTo(max) < 0;
        boolean notFit = !fits;
        return notFit;
    }

    private boolean notFitToMin(V value) {
        String minAsString = minOption.getValue();
        if (minAsString.isEmpty()) {
            return false;
        }
        V min = toNumber(minAsString);
        boolean isGreaterThanOrEqualToMin = value.compareTo(min) >= 0;
        boolean notFits = !isGreaterThanOrEqualToMin;
        return notFits;
    }

    private boolean notFitToMax(V value) {
        String maxAsString = maxOption.getValue();
        if (maxAsString.isEmpty()) {
            return false;
        }
        V max = toNumber(maxAsString);
        boolean isLessThanOrEqualToMax = value.compareTo(max) <= 0;
        boolean notFit = !isLessThanOrEqualToMax;
        return notFit;
    }

    private void validateDigitsOption(V value) {
        int intDigitsMax = digitsOption.getIntegerMax();
        int fractionDigitsMax = digitsOption.getFractionMax();
        if (intDigitsMax < 1 || fractionDigitsMax < 1) {
            return;
        }
        V abs = getAbs(value);
        String[] parts = PATTERN_DOT.split(String.valueOf(abs));
        int intDigitsCount = parts[0].length();
        int fractionDigitsCount = parts[1].length();
        boolean isInvalid = (intDigitsCount > intDigitsMax) ||
                (fractionDigitsCount > fractionDigitsMax);
        if (isInvalid) {
            addViolation(newDigitsViolation(value));
        }
    }

    private ConstraintViolation newDecimalViolation(V value,
                                                    Message option,
                                                    String customMsg,
                                                    boolean isInclusive,
                                                    String minOrMax) {
        String msg = getErrorMsgFormat(option, customMsg);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(isInclusive ? "or equal to " : "")
                .addParam(minOrMax)
                .setFieldPath(getFieldPath())
                .setFieldValue(wrap(value))
                .build();
        return violation;
    }

    private ConstraintViolation newMinOrMaxViolation(V value, Message option,
                                                     String customMsg, String minOrMax) {
        String msg = getErrorMsgFormat(option, customMsg);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(minOrMax)
                .setFieldPath(getFieldPath())
                .setFieldValue(wrap(value))
                .build();
        return violation;
    }

    private ConstraintViolation newDigitsViolation(V value) {
        String msg = getErrorMsgFormat(digitsOption, digitsOption.getMsgFormat());
        String intMax = String.valueOf(digitsOption.getIntegerMax());
        String fractionMax = String.valueOf(digitsOption.getFractionMax());
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(intMax)
                .addParam(fractionMax)
                .setFieldPath(getFieldPath())
                .setFieldValue(wrap(value))
                .build();
        return violation;
    }
}
