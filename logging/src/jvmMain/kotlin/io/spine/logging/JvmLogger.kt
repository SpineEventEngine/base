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
import com.google.common.flogger.LogSites
import kotlin.reflect.KClass
import java.util.logging.Level as JLevel

/**
 * Implements [Logger] using [FluentLogger] as the underlying implementation.
 */
public class JvmLogger(
    cls: KClass<*>,
    internal val impl: FluentLogger
) : Logger<JvmLogger.Api>(cls) {

    /**
     * The non-wildcard logging API for the [JvmLogger].
     */
    public interface Api: LoggingApi<Api>

    override fun createApi(level: Level): Api {
        val implApi = impl.at(level.toJavaLogging()).withInjectedLogSite(
            LogSites.callerOf(Logger::class.java)
        )
        return Impl(implApi)
    }
}

/**
 * Implements [LoggingApi] wrapping [FluentLogger.Api].
 */
private class Impl(private val floggerApi: FluentLogger.Api): JvmLogger.Api {
    override fun withCause(cause: Throwable): JvmLogger.Api {
        floggerApi.withCause(cause)
        return this
    }

    override fun isEnabled(): Boolean = floggerApi.isEnabled

    override fun log() = floggerApi.log()

    override fun log(message: () -> String) {
        if (isEnabled()) {
            floggerApi.log(message.invoke())
        }
    }
}

/**
 * Maps [Level] values to its Java logging counterparts.
 */
private fun Level.toJavaLogging(): JLevel {
    val result = when (this) {
        Level.DEBUG -> JLevel.FINE
        Level.INFO -> JLevel.INFO
        Level.WARNING -> JLevel.WARNING
        Level.ERROR -> JLevel.SEVERE
        else -> {
            error("The level `${this}` cannot be matched to Java counterpart.")
        }
    }
    return result
}
