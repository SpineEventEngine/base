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

import com.google.common.flogger.FluentLogger
import com.google.common.flogger.LogContext
import com.google.common.flogger.backend.LogData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.spine.logging.given.LoggingObject
import io.spine.testing.logging.LogTruth
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("`Logging` interface should")
internal class LoggingSpec {

    @Test
    fun `assume 'FINE' for debug`() {
        Logging.debugLevel() shouldBe Level.FINE
    }

    @Test
    fun `assume 'SEVERE' for errors`() {
        Logging.errorLevel() shouldBe Level.SEVERE
    }

    @Test
    fun `obtain same 'FluentLogger' instance`() {
        val obj: Logging = LoggingObject()
        val logger = obj.logger()

        obj.logger() shouldBeSameInstanceAs logger
        LoggingObject().logger() shouldBeSameInstanceAs logger
    }

    @Test
    fun `provide a static API to obtain a logger for a class`() {
        val logger = Logging.loggerFor(LoggingObject::class.java)
        logger shouldNotBe null
        logger shouldBeSameInstanceAs LoggingObject().logger()
    }

    @Nested
    @DisplayName("expose shortcut methods")
    internal inner class Shortcuts {

        private lateinit var obj: Logging
        private lateinit var julLogger: Logger

        @BeforeEach
        fun createLoggingObject() {
            obj = LoggingObject()
            julLogger = Logger.getLogger(obj.javaClass.name)
        }

        @Test
        fun `for standard logging levels`() {
            assertApi({ obj._config() }, Level.CONFIG)
            assertApi({ obj._fine() }, Level.FINE)
            assertApi({ obj._finer() }, Level.FINER)
            assertApi({ obj._finest() }, Level.FINEST)
            assertApi({ obj._info() }, Level.INFO)
            assertApi({ obj._severe() }, Level.SEVERE)
            assertApi({ obj._warn() }, Level.WARNING)
        }

        @Test
        fun `for popular aliases`() {
            assertApi({ obj._debug() }, Level.FINE)
            assertApi({ obj._trace() }, Level.FINEST)
            assertApi({ obj._error() }, Level.SEVERE)
        }

        private fun assertApi(method: Supplier<FluentLogger.Api>, expectedLevel: Level) {
            julLogger.level = expectedLevel
            val api// See: https://github.com/SpineEventEngine/base/issues/612
                    = method.get()
            LogTruth.assertThat(api).isInstanceOf(LogContext::class.java)
            (api as LogData).level shouldBe expectedLevel
        }
    }
}
