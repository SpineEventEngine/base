/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.net;

import com.google.common.truth.BooleanSubject;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.net.EmailAddresses.isValid;
import static io.spine.testing.Assertions.assertIllegalArgument;

@DisplayName("EmailAddresses utility class should")
class EmailAddressesTest extends UtilityClassTest<EmailAddresses> {

    EmailAddressesTest() {
        super(EmailAddresses.class);
    }

    @Test
    @DisplayName("validate character sequence")
    void validate() {
        assertValid("user@site.com");
        assertValid("a@b.com");
        assertValid("a@b-c.com");

        assertInvalid("@site.org");
        assertInvalid("user@");
        assertInvalid("user @ site.com");
    }

    private static void assertValid(String sequence) {
        assertEmail(sequence).isTrue();
    }

    private static void assertInvalid(String sequence) {
        assertEmail(sequence).isFalse();
    }

    private static BooleanSubject assertEmail(String email) {
        return assertThat(isValid(email));
    }

    @Test
    @DisplayName("create new instance")
    void create() {
        String email = "jdoe@spine.org";

        EmailAddress emailAddress = EmailAddresses.valueOf(email);

        assertThat(emailAddress.getValue())
                .isEqualTo(email);
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void invalidEmail() {
        assertIllegalArgument(() -> EmailAddresses.valueOf("fiz baz"));
    }
}
