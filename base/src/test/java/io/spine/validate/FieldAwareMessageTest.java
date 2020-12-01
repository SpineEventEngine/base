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

package io.spine.validate;

import io.spine.test.validate.AggregateState;
import io.spine.validate.given.FieldAwareMessageTestEnv;
import io.spine.validate.given.FieldAwareMessageTestEnv.BrokenFieldAware;
import io.spine.validate.given.FieldAwareMessageTestEnv.FieldAwareMsg;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.validate.given.FieldAwareMessageTestEnv.msg;

@DisplayName("`FieldAwareMessage` should")
class FieldAwareMessageTest {

    @Test
    @DisplayName("read values when `readValues` is properly implemented")
    void readValues() {
        AggregateState msg = FieldAwareMessageTestEnv.msg();
        FieldAwareMsg state = new FieldAwareMsg(msg);
        assertThat(state.checkFieldsReachable()).isTrue();
    }

    @Test
    @DisplayName("fail to read values when `readValues` has implementation issues")
    void failToReadValues() {
        AggregateState msg = msg();
        BrokenFieldAware state = new BrokenFieldAware(msg);
        assertIllegalArgument(state::checkFieldsReachable);
    }
}