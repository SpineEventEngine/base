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

package io.spine.annotation

import io.kotest.matchers.shouldBe
import java.lang.annotation.RetentionPolicy.RUNTIME
import java.lang.annotation.RetentionPolicy.SOURCE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * This test suite tests `RetentionPolicy` of the annotations in
 * the `io.spine.annotation` package.
 */
@DisplayName("`io.spine.annotation` package should")
internal class AnnotationsSpec {

    @Test
    fun `have 'Beta' annotation`() {
        Beta::class.java.retention() shouldBe SOURCE
    }

    @Test
    fun `have 'Experimental' annotation`() {
        Experimental::class.java.retention() shouldBe SOURCE
    }

    @Test
    fun `have 'GeneratedMixin' annotation`() {
        GeneratedMixin::class.java.retention() shouldBe SOURCE
    }

    @Test
    fun `have 'Internal' annotation`() {
        Internal::class.java.retention() shouldBe RUNTIME
    }

    @Test
    fun `have 'SPI' annotation`() {
        SPI::class.java.retention() shouldBe SOURCE
    }
}

private fun <T: Annotation> Class<in T>.retention() =
    getAnnotation(java.lang.annotation.Retention::class.java).value

