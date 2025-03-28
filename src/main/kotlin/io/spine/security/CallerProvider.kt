/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.security

import java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE
import java.lang.StackWalker.StackFrame
import java.util.stream.Stream

/**
 * Provides information about the class calling a method.
 */
internal object CallerProvider {

    private val stackWalker: StackWalker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)

    /**
     * Obtains the class of the object which calls the method from which
     * this method is being called.
     */
    fun callerClass(): Class<*> {
        return stackWalker.walk { frames ->
            frames.getCallingClass(skipFrames = 2)
        }
    }

    /**
     * Obtains the class preceding in the call chain the class which calls
     * the method from which this method is being called.
     */
    fun previousCallerClass(): Class<*> {
        return stackWalker.walk { frames ->
            frames.getCallingClass(skipFrames = 3)
        }
    }

    private fun Stream<StackFrame>.getCallingClass(skipFrames: Long) =
        skip(skipFrames)
            .findFirst()
            .map { frame -> frame.declaringClass }
            .get() // We're safe because the stacktrace will be deeper than 3.
}
