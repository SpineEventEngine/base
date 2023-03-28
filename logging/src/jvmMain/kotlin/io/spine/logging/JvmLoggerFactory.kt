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

import com.google.common.base.Throwables
import com.google.common.flogger.FluentLogger
import com.google.common.flogger.backend.LoggerBackend
import com.google.common.flogger.backend.Platform
import java.lang.Exception
import java.lang.reflect.Constructor
import kotlin.reflect.KClass

public actual object LoggerFactory: ClassValue<JvmLogger>() {

    private val logger = FluentLogger.forEnclosingClass()
    private val constructor: Constructor<FluentLogger> = ctor()

    @JvmStatic
    @JvmName("getLogger")
    public actual fun getLogger(cls: KClass<*>): Logger<*> {
        return get(cls.java)
    }

    override fun computeValue(cls: Class<*>): JvmLogger {
        return createForClass(cls)
    }

    @JvmStatic
    @JvmName("getFluentLogger")
    internal fun getFluentLogger(cls: Class<*>): FluentLogger {
        return get(cls).impl
    }

    private fun createForClass(cls: Class<*>): JvmLogger {
        val impl = createFluentLogger(cls)
        return JvmLogger(impl)
    }

    private fun ctor(): Constructor<FluentLogger> {
        val loggerClass = FluentLogger::class.java
        val loggerBackendClass = LoggerBackend::class.java
        return try {
            val constructor = loggerClass.getDeclaredConstructor(loggerBackendClass)
            constructor.isAccessible = true
            constructor
        } catch (e: NoSuchMethodException) {
            logger.atSevere().withCause(e).log(
                "Unable to find constructor `${loggerClass.name}(${loggerBackendClass.name})`."
            )
            throw illegalStateWithCauseOf(e)
        }
    }

    private fun createFluentLogger(cls: Class<*>): FluentLogger {
        val backend = Platform.getBackend(cls.name)
        return try {
            constructor.newInstance(backend)
        } catch (e: Exception) {
            logger.atSevere().withCause(e).log("Unable to create logger.")
            throw illegalStateWithCauseOf(e)
        }
    }
}

private fun illegalStateWithCauseOf(throwable: Throwable): IllegalStateException {
    val rootCause = Throwables.getRootCause(throwable)
    throw IllegalStateException(rootCause)
}
