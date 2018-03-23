/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.test;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertTrue;

public class TestValuesShould {

    @Test
    public void have_utility_ctor() {
        assertHasPrivateParameterlessCtor(TestValues.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester()
                .testAllPublicStaticMethods(TestValues.class);
    }

    @Test
    public void provide_random_non_negative_number() {
        assertTrue(TestValues.random(100) >= 0);
    }

    @Test
    public void provide_randome_number_in_range() {
        final int value = TestValues.random(-100, 100);
        assertTrue(value >= -100);
        assertTrue(value <= 100);
    }
}
