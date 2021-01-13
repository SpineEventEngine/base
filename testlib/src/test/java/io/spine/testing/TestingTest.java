/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.testing;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.FieldMask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Testing.repeat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Tests utility class should")
class TestingTest extends UtilityClassTest<Testing> {

    TestingTest() {
        super(Testing.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldMask.class, FieldMask.getDefaultInstance());
    }

    @Test
    @DisplayName("repeat an action a number of times")
    void repeating() {
        int expected = TestValues.random(10);
        AtomicInteger counter = new AtomicInteger(0);
        repeat(expected, counter::incrementAndGet);

        assertThat(counter.get())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("provide method `halt()` for failing methods that should never be called")
    void haltMethod() {
        assertThrows(AssertionError.class, Testing::halt);
    }
}
