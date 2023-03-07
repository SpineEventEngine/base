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
package io.spine.util

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import io.kotest.matchers.shouldBe
import io.spine.testing.Assertions.assertIllegalArgument
import io.spine.testing.Assertions.assertIllegalState
import io.spine.testing.Assertions.assertNpe
import io.spine.testing.TestValues.longRandom
import io.spine.testing.TestValues.newUuidValue
import io.spine.testing.TestValues.randomString
import io.spine.testing.UtilityClassTest
import io.spine.util.Preconditions2.checkBounds
import io.spine.util.Preconditions2.checkNonNegative
import io.spine.util.Preconditions2.checkNotDefaultArg
import io.spine.util.Preconditions2.checkNotDefaultState
import io.spine.util.Preconditions2.checkNotEmptyOrBlank
import io.spine.util.Preconditions2.checkPositive
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@DisplayName("`Preconditions2` utility class should")
internal class Preconditions2Test : UtilityClassTest<Preconditions2>(Preconditions2::class.java) {

    /**
     * Abstract base for test suite of a precondition function.
     */
    sealed class TestSuite<T>(
        val consumer: (T) -> Unit,
        val consumerWithMessage: (T, String, params: Array<Any>) -> Unit
    ) {

        /**
         * Asserts that the given [consumer] throws [IllegalArgumentException].
         */
        protected fun assertThrowsOn(value: T) {
            val exception = assertThrows<IllegalArgumentException> {
                consumer(value)
            }
            assertThat(exception)
                .hasMessageThat()
                .contains(value.toString())
        }

        /**
         * Asserts that the given [consumer] throws [IllegalArgumentException]
         * and the exception message contains the given [errorMessage].
         */
        protected fun assertThrowsWithMessage(arg: T, errorMessage: String) {
            val exception = assertThrows<IllegalArgumentException> {
                consumerWithMessage(arg, errorMessage, arrayOf(arg as Any))
            }
            assertThat(exception).hasMessageThat()
                .contains(errorMessage)
        }
    }

    @Nested
    internal inner class `check that a 'String' is` :
        TestSuite<String>(Preconditions2::checkNotEmptyOrBlank, { arg, fmt, params ->
            checkNotEmptyOrBlank(arg, fmt, params)
        }) {

        @Test
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun `a 'null'`() {
            assertNpe { checkNotEmptyOrBlank(null) }
            val errorTemplateBase = randomString()
            val errorArg = arrayOf(randomString(), randomString())
            val errorTemplate = "$errorTemplateBase%s %s"

            val exception = assertThrows<NullPointerException>{
                checkNotEmptyOrBlank(null, errorTemplate, errorArg[0], errorArg[1])
            }

            val assertExceptionMessage = assertThat(exception).hasMessageThat()
            with(assertExceptionMessage) {
                contains(errorTemplateBase)
                contains(errorArg[0])
                contains(errorArg[1])
            }
        }

        @Test
        fun empty() {
            assertThrowsOn("")
            assertThrowsWithMessage("", randomString())
        }

        @Test
        fun blank() {
            assertThrowsOn(" ")
            assertThrowsWithMessage(" ", randomString())
            assertThrowsOn("  ")
            assertThrowsWithMessage("  ", randomString())
            assertThrowsOn("   ")
            assertThrowsWithMessage("   ", randomString())
        }
    }


    @Nested
    internal inner class `check that a value is positive` :
        TestSuite<Long>(Preconditions2::checkPositive,{ arg, fmt, params ->
            checkPositive(arg, fmt, params)
        }) {

        @Test
        fun `rejecting zero`() {
            assertThrowsOn(0)
            assertThrowsWithMessage(0, randomString())
        }

        @Test
        fun `rejecting negative values`() {
            assertThrowsOn(-1)
            assertThrowsWithMessage(-100, randomString())
        }

        @Test
        fun `accepting positive value and returning it`() {
            val expected = longRandom(1, 100000)
            assertThat(checkPositive(expected))
                .isEqualTo(expected)
        }
    }

    @Nested
    internal inner class `check that a value is positive or zero` :
        TestSuite<Long>(Preconditions2::checkNonNegative, { arg, fmt, params ->
            checkNonNegative(arg, fmt, params)
        }) {

        @Test
        fun `accepting zero`() {
            assertDoesNotThrow { consumer(0) }
        }

        @Test
        fun `rejecting negative values`() {
            assertThrowsOn(-1)
            assertThrowsWithMessage(-100, randomString())
        }

        @Test
        fun `accepting non-negative value and returning it`() {
            val expected = longRandom(0, 100000)
            assertThat(checkPositive(expected))
                .isEqualTo(expected)
        }
    }

    @Test
    fun `throw if checked value out of bounds`() {
        assertIllegalArgument { checkBounds(10, "checked value", -5, 9) }
    }

    @Nested
    @DisplayName("check that a message is not in the default state")
    internal inner class NotDefaultMessage {
        private val defaultValue: Message = StringValue.getDefaultInstance()
        private var customErrorMessage: String? = null

        @BeforeEach
        fun createCustomErrorMessage() {
            customErrorMessage = randomString()
        }

        @Test
        fun `throwing 'IllegalStateException' for state transition checks`() {
            assertIllegalState { checkNotDefaultState(defaultValue) }
            val exception = assertThrows<IllegalStateException> {
                checkNotDefaultState(defaultValue, customErrorMessage)
            }
            assertThat(exception).hasMessageThat()
                .contains(customErrorMessage)
        }

        @Test
        fun `throwing 'IllegalArgumentException' for argument checks`() {
            assertIllegalArgument { checkNotDefaultArg(defaultValue) }
            val exception = assertThrows<IllegalArgumentException>{
                checkNotDefaultArg(defaultValue, customErrorMessage) }
            assertThat(exception)
                .hasMessageThat()
                .contains(customErrorMessage)
        }

        @Test
        fun `return non-default value on check`() {
            val nonDefault = newUuidValue()
            checkNotDefaultArg(nonDefault) shouldBe nonDefault
            checkNotDefaultArg(nonDefault, customErrorMessage) shouldBe nonDefault
            checkNotDefaultState(nonDefault) shouldBe nonDefault
            checkNotDefaultState(nonDefault, customErrorMessage) shouldBe nonDefault
        }
    }
}
