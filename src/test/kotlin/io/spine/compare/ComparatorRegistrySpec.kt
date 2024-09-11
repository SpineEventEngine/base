/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.compare

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`ComparatorRegistry` should`")
internal class ComparatorRegistrySpec {

    private val registry = ComparatorRegistry

    @Test
    fun `load the comparators from the providers`() {
        registry.contains(Timestamp::class.java).shouldBeTrue()
        registry.contains(Duration::class.java).shouldBeTrue()
    }

    @Test
    fun `register and check presence of comparators`() {
        val comparator = compareBy<String> { it.length }
        registry.contains<String>().shouldBeFalse()
        registry.register<String>(comparator)
        registry.contains<String>().shouldBeTrue()
    }

    @Test
    fun `override the already registered comparator`() {
        val comparator1 = compareBy<Double> { it }
        val comparator2 = compareBy<Double> { it }
        registry.register<Double>(comparator1)
        registry.register<Double>(comparator2)
        registry.get<Double>() shouldBe comparator2
    }

    @Test
    fun `return a comparator`() {
        val comparator = compareBy<Float> { it }
        shouldThrow<IllegalStateException> { registry.get<Float>() }
        registry.register<Float>(comparator)
        registry.get<Float>() shouldBe comparator
    }

    @Test
    fun `search for a comparator`() {
        val comparator = compareBy<StringBuilder> { it.length }
        registry.find<StringBuilder>().shouldBeNull()
        registry.register<StringBuilder>(comparator)
        registry.find<StringBuilder>() shouldBe comparator
    }
}
