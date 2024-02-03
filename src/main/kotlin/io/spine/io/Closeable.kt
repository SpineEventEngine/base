/*
 * Copyright 2024, TeamDev. All rights reserved.
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
package io.spine.io

/**
 * Base interface for objects that may hold resources that need to be released
 * at the end of the object lifecycle.
 *
 * A class will benefit from implementing *this* interface instead of
 * [AutoCloseable] if it needs to see if the instance is [open][isOpen]
 * prior to making other calls.
 *
 * @see isOpen
 * @see checkOpen
 */
public interface Closeable : AutoCloseable {

    /**
     * Tells if the object is still open.
     *
     * Implementations must return `false` after [close] is invoked.
     */
    public val isOpen: Boolean

    /**
     * Closes the object.
     *
     * Overrides to remove the checked exception from the signature.
     */
    public override fun close()

    /**
     * Ensures that the object [isOpen].
     *
     * @throws IllegalStateException otherwise
     */
    @Throws(IllegalStateException::class)
    public fun checkOpen() {
        check(isOpen) { "`$this` is already closed." }
    }

    /**
     * Performs the release of the resources held by this object only if it is still open.
     *
     * Otherwise, does nothing.
     */
    public fun closeIfOpen() {
        if (isOpen) {
            close()
        }
    }
}
