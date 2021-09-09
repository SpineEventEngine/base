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

package io.spine.tools.comparables.test

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class `Comparable messages should` {

    @Test
    fun `order up by the first priority field`() {
        val prettyCat = Cat.newBuilder()
            .setHowPretty(8)
            .setSpotty(true)
            .build()
        val veryPrettyCat = Cat.newBuilder()
            .setHowPretty(10)
            .build()
        assertThat(prettyCat)
            .isLessThan(veryPrettyCat)
    }

    @Test
    fun `order up by the second priority field`() {
        val cat = Cat.newBuilder()
            .setHowPretty(8)
            .build()
        val spottyCat = Cat.newBuilder()
            .setHowPretty(8)
            .setSpotty(true)
            .build()
        assertThat(cat)
            .isLessThan(spottyCat)
    }

    @Test
    fun `order up and establish equivalence`() {
        val cat = Cat.newBuilder()
            .setHowPretty(8)
            .setSpotty(true)
            .build()
        val equallyPrettyCat = Cat.newBuilder()
            .setHowPretty(8)
            .setSpotty(true)
            .build()
        assertThat(cat)
            .isEquivalentAccordingToCompareTo(equallyPrettyCat)
    }

    @Test
    fun `throw NPE when comparing to a 'null' value`() {
        val cat = Cat.newBuilder()
            .setHowPretty(10)
            .setSpotty(true)
            .build()
        assertThrows<NullPointerException> { cat.compareTo(null) }
    }
}
