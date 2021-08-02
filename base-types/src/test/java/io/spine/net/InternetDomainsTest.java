/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import static io.spine.net.InternetDomains.isValid;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("InternetDomains utility class should")
class InternetDomainsTest extends UtilityClassTest<InternetDomains> {

    InternetDomainsTest() {
        super(InternetDomains.class);
    }

    @Test
    @DisplayName("validate char sequence")
    void provideMatchingPattern() {
        assertValid("spine.io");

        assertValid("teamdev.com");
        assertValid("a.com");
        assertValid("boeng747.aero");

        assertInvalid("192.168.0.1");
        assertInvalid(".com");
        assertInvalid("com");
    }

    private static void assertValid(String sequence) {
        assertDomain(sequence).isTrue();
    }

    private static void assertInvalid(String sequence) {
        assertDomain(sequence).isFalse();
    }

    private static BooleanSubject assertDomain(String email) {
        return assertThat(isValid(email));
    }

    @Test
    @DisplayName("create new instance")
    void create() {
        String domainName = "example.org";

        assertEquals(domainName, InternetDomains.valueOf(domainName).getValue());
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void rejectInvalidName() {
        assertIllegalArgument(() -> InternetDomains.valueOf("1.0"));
    }
}
