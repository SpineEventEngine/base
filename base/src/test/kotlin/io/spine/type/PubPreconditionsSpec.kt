/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.type

import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import io.spine.annotation.Internal
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ExplicitNonInternalType
import io.spine.given.type.ImplicitInternalType
import io.spine.testing.StubMessage
import java.lang.IllegalArgumentException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`PubPreconditions` should")
internal class PubPreconditionsSpec {

    @Test
    fun `require a type to be internal`() {

        fun assertThrowsOn(msg: Message) = assertThrows<IllegalArgumentException> {
            requireInternal(msg)
        }

        assertThrowsOn(ExplicitNonInternalType.getDefaultInstance())
        assertThrowsOn(Timestamp.getDefaultInstance())

        fun assertDoesNotThrow(msg: Message) = assertDoesNotThrow {
            requireInternal(msg)
        }

        assertDoesNotThrow(ExplicitInternalType.getDefaultInstance())
        assertDoesNotThrow(ImplicitInternalType.getDefaultInstance())
        assertDoesNotThrow(StubInternalMsg())
    }
}

@Internal
private class StubInternalMsg: StubMessage()
