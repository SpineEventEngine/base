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

import io.kotest.matchers.shouldBe
import io.spine.given.type.ExplicitBetaType
import io.spine.given.type.ExplicitExperimentalType
import io.spine.given.type.ExplicitInternalType
import io.spine.given.type.ExplicitNonBetaType
import io.spine.given.type.ExplicitNonExperimentalType
import io.spine.given.type.ExplicitNonInternalType
import io.spine.given.type.ExplicitNonSpiType
import io.spine.given.type.ExplicitSpiType
import io.spine.given.type.ImplicitBetaType
import io.spine.given.type.ImplicitExperimentalType
import io.spine.given.type.ImplicitInternalType
import io.spine.given.type.ImplicitSpiType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Descriptor` extensions from `io.spine.type` should")
internal class DescriptorExtsSpec {

    @Nested
    @DisplayName("tell that a message type is marked as")
    inner class ExplicitOption {

        @Test
        fun beta() {
            ExplicitBetaType.getDescriptor().isBeta() shouldBe true
            ExplicitNonBetaType.getDescriptor().isBeta() shouldBe false
            ImplicitBetaType.getDescriptor().isBeta() shouldBe false
        }

        @Test
        fun experimental() {
            ExplicitExperimentalType.getDescriptor().isExperimental() shouldBe true
            ExplicitNonExperimentalType.getDescriptor().isExperimental() shouldBe false
            ImplicitExperimentalType.getDescriptor().isExperimental() shouldBe false
        }

        @Test
        fun internal() {
            ExplicitInternalType.getDescriptor().isInternal() shouldBe true
            ImplicitInternalType.getDescriptor().isInternal() shouldBe false
            ExplicitNonInternalType.getDescriptor().isInternal() shouldBe false
        }

        @Test
        fun spi() {
            ExplicitSpiType.getDescriptor().isSpi() shouldBe true
            ImplicitSpiType.getDescriptor().isSpi() shouldBe false
            ExplicitNonSpiType.getDescriptor().isSpi() shouldBe false
        }
    }
}
