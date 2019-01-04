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

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.testing.Tests;
import io.spine.testing.UtilityClassTest;
import io.spine.type.TypeName;
import io.spine.validate.diags.ConstraintViolations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.protobuf.TypeConverter.toMessage;
import static io.spine.testing.TestValues.newUuidValue;
import static io.spine.validate.Validate.checkBounds;
import static io.spine.validate.Validate.checkDefault;
import static io.spine.validate.Validate.checkNotDefault;
import static io.spine.validate.Validate.checkNotEmptyOrBlank;
import static io.spine.validate.Validate.checkPositive;
import static io.spine.validate.Validate.isDefault;
import static io.spine.validate.Validate.isNotDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validate utility class should")
class ValidateTest extends UtilityClassTest<Validate> {

    ValidateTest() {
        super(Validate.class);
    }

    @Test
    @DisplayName("not consider zero as a positive")
    void check_positive_if_zero() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkPositive(0));
    }

    @Test
    @DisplayName("throw if not a positive")
    void check_positive_if_negative() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkPositive(-1));
    }

    @Test
    @DisplayName("throw if not positive and display a message")
    void check_positive_with_message() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkPositive(-1, "negativeInteger"));
    }

    @Test
    @DisplayName("throw if long value is not positive")
    void throw_exception_if_long_value_is_not_positive() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkPositive(-2L, "negativeLong"));
    }

    @Test
    @DisplayName("verify that message is not in default state")
    void verify_that_message_is_not_in_default_state() {
        Message msg = toMessage("check_if_message_is_not_in_default_state");

        assertTrue(isNotDefault(msg));
        assertFalse(isNotDefault(StringValue.getDefaultInstance()));
    }

    @Test
    @DisplayName("throw if checked value out of bounds")
    void throw_exception_if_checked_value_out_of_bounds() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkBounds(10, "checked value", -5, 9));
    }

    @Test
    @DisplayName("verify that message is in default state")
    void verify_that_message_is_in_default_state() {
        Message nonDefault = newUuidValue();

        assertTrue(isDefault(StringValue.getDefaultInstance()));
        assertFalse(isDefault(nonDefault));
    }

    @Test
    @DisplayName("check that message is in default state")
    void check_if_message_is_in_default() {
        StringValue nonDefault = newUuidValue();
        assertThrows(IllegalStateException.class,
                     () -> checkDefault(nonDefault));
    }

    @Test
    @DisplayName("check that message is in default state with a parametrized error message")
    void check_a_message_is_default_with_parametrized_error_message() {
        StringValue nonDefault = newUuidValue();
        assertThrows(IllegalStateException.class,
                     () -> checkDefault(nonDefault,
                                        "Message value: %s, Type name: %s",
                                        nonDefault,
                                        TypeName.of(nonDefault)));
    }

    @Test
    @DisplayName("return default value on check")
    void return_default_value_on_check() {
        Message defaultValue = StringValue.getDefaultInstance();
        assertEquals(defaultValue, checkDefault(defaultValue));
        assertEquals(defaultValue, checkDefault(defaultValue, "error message"));
    }

    @Test
    @DisplayName("check if message is not in default state")
    void check_if_message_is_in_not_in_default_state_throwing_exception_if_not() {
        assertThrows(IllegalStateException.class,
                     () -> checkNotDefault(StringValue.getDefaultInstance()));
    }

    @Test
    @DisplayName("return non-default value on check")
    void return_non_default_value_on_check() {
        StringValue nonDefault = newUuidValue();
        assertEquals(nonDefault, checkNotDefault(nonDefault));
        assertEquals(nonDefault, checkNotDefault(nonDefault, "with error message"));
    }

    @Test
    @DisplayName("throw if checked string is null")
    void throw_exception_if_checked_string_is_null() {
        assertThrows(NullPointerException.class,
                     () -> checkNotEmptyOrBlank(Tests.nullRef(), ""));
    }

    @Test
    @DisplayName("throw if checked string is empty")
    void throw_exception_if_checked_string_is_empty() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkNotEmptyOrBlank("", ""));
    }

    @Test
    @DisplayName("throw if checked string is blank")
    void throw_exception_if_checked_string_is_blank() {
        assertThrows(IllegalArgumentException.class,
                     () -> checkNotEmptyOrBlank("   ", ""));
    }

    @Test
    @DisplayName("not throw if checked strign is not empty or blank")
    public void do_not_throw_exception_if_checked_string_is_valid() {
        checkNotEmptyOrBlank("valid_string", "");
    }

    @Test
    @DisplayName("format message from constraint violation")
    void format_message_from_constraint_violation() {
        ConstraintViolation violation = ConstraintViolation.newBuilder()
                                                           .setMsgFormat("test %s test %s")
                                                           .addParam("1")
                                                           .addParam("2")
                                                           .build();
        String formatted = ConstraintViolations.toText(violation);

        assertEquals("test 1 test 2", formatted);
    }

    @Test
    @DisplayName("format message using parameters from constraint violation")
    void format_message_using_params_from_constraint_violation() {
        ConstraintViolation violation = ConstraintViolation.newBuilder()
                                                           .addParam("1")
                                                           .addParam("2")
                                                           .build();
        String formatted = ConstraintViolations.toText("abc %s abc %s", violation);

        assertEquals("abc 1 abc 2", formatted);
    }
}
