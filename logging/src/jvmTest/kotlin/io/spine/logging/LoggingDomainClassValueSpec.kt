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

package io.spine.logging

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.spine.logging.given.domain.AnnotatedClass
import io.spine.logging.given.domain.IndirectlyAnnotatedClass
import io.spine.logging.given.domain.nested.NonAnnotatedNestedPackageClass
import kotlin.reflect.KClass
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`LoggingDomainClassValue` should")
class LoggingDomainClassValueSpec {

    private fun lDomainOf(cls: KClass<*>) = LoggingDomainClassValue.get(cls)

    @Test
    fun `obtain a domain from an annotated Kotlin class`() {
        lDomainOf(DirectlyAnnotated::class).name shouldBe "Direct"
    }

    @Test
    fun `obtain noOp domain from a not annotated class which does not have annotated package`() {
        lDomainOf(NotAnnotated::class) shouldBeSameInstanceAs LoggingDomain.noOp
    }

    @Test
    fun `obtain a domain from 'JvmLoggingDomain' annotation of a class`() {
        lDomainOf(AnnotatedClass::class).name shouldBe "OnClass"
    }

    @Test
    fun `obtain a domain from 'JvmLoggingDomain' annotation of a direct package`() {
        lDomainOf(IndirectlyAnnotatedClass::class).name shouldBe "OnPackage"
    }

    @Test
    fun `obtain a domain from 'JvmLoggingDomain' annotation of an upper package`() {
        lDomainOf(NonAnnotatedNestedPackageClass::class).name shouldBe "OnPackage"
    }

    @Test
    fun `obtain a domain from a nested inner class`() {
        lDomainOf(
            OuterAnnotated.NonAnnotated.InterimAnnotated.DeepestNonAnnotated::class
        ).name shouldBe "Interim"
    }

    @Test
    fun `obtain a domain from a nested not inner class`() {
        lDomainOf(
            OuterAnnotated.NonAnnotated.NonInnerNonAnnotated::class
        ).name shouldBe "Outer"
    }
}

@LoggingDomain("Direct")
private class DirectlyAnnotated

private class NotAnnotated

@LoggingDomain("Outer")
private class OuterAnnotated {

    class NonAnnotated {

        @LoggingDomain("Interim")
        inner class InterimAnnotated {
            inner class DeepestNonAnnotated
        }

        class NonInnerNonAnnotated
    }
}
