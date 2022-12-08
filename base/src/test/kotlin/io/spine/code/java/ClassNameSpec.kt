/*
 * Copyright 2022, TeamDev. All rights reserved.
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
package io.spine.code.java

import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.spine.code.java.SimpleClassName.OR_BUILDER_SUFFIX
import io.spine.test.type.Uri
import io.spine.testing.Assertions
import io.spine.testing.Assertions.assertIllegalArgument
import io.spine.testing.nullPointerTester
import io.spine.testing.setDefault
import io.spine.testing.testAllPublicStaticMethods
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("`ClassName` should")
internal class ClassNameSpec {

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = ["", "    "])
    fun `reject empty and blank values`(value: String) {
        assertIllegalArgument { ClassName.of(value) }
    }

    @Test
    fun `handle nulls gracefully`() {
        val descriptor = StringValue.getDescriptor()
        nullPointerTester {
            setDefault<SimpleClassName>(SimpleClassName.ofMessage(descriptor))
            setDefault<PackageName>(PackageName.resolve(descriptor.file.toProto()))
        }.testAllPublicStaticMethods<ClassName>()
    }

    @Test
    fun `provide binary name and canonical name`() {
        val cls = Uri.Protocol::class.java
        val className = ClassName.of(cls)

        className.binaryName() shouldBe cls.name
        className.canonicalName() shouldBe cls.canonicalName
    }

    @Test
    fun `throw ISE when parsing an invalid name`() {
        val className = ClassName.of("NotQualifiedName")
        Assertions.assertIllegalState { className.packageName() }
    }

    @Test
    fun `obtain a package of a class`() {
        String::class.java.className.packageName() shouldBe String::class.java.nameOfPackage
    }

    @Test
    fun `obtain an 'OrBuilder' name for message classes`() {
        Timestamp::class.java.className.orBuilder().canonicalName() shouldEndWith OR_BUILDER_SUFFIX
    }
}
