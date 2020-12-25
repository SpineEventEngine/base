/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.testing;

import java.lang.reflect.Constructor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Utilities for testing.
 */
public final class Testing {

    /** Prevent instantiation of this utility class. */
    private Testing() {
    }

    /**
     * Calls the passed constructor include it into the coverage.
     *
     * <p>Some of the coding conventions may encourage throwing {@link AssertionError}
     * to prevent the instantiation of the target class, if it is designed as a utility class.
     * This method catches all the exceptions which may be thrown by the constructor.
     */
    @SuppressWarnings("OverlyBroadCatchBlock") // see Javadoc
    static void callConstructor(Constructor<?> constructor) {
        boolean accessible = constructor.isAccessible();
        if (!accessible) {
            constructor.setAccessible(true);
        }
        try {
            constructor.newInstance();
        } catch (Exception ignored) {
            // Do nothing.
        } finally {
            if (!accessible) {
                constructor.setAccessible(false);
            }
        }
    }

    /**
     * Repeats the passed action the {@code count} number of times.
     */
    public static void repeat(int count, Runnable action) {
        checkNotNull(action);
        for (int i = 0; i < count; i++) {
             action.run();
        }
    }

    /**
     * Reports that a calling method should never be called by throwing {@link AssertionError}.
     *
     * @throws AssertionError always
     */
    public static void halt() throws AssertionError {
        fail("This method should never be called.");
    }
}
