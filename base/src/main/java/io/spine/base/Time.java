/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Timestamps.fromMillis;

/**
 * Utilities for working with time information.
 *
 * @author Alexander Yevsyukov
 */
public class Time {

    private static final ThreadLocal<Provider> timeProvider = new ThreadLocal<Provider>() {
        @Override
        protected Provider initialValue() {
            return new SystemTimeProvider();
        }
    };

    /** Prevents instantiation of this utility class. */
    private Time() {
        // Do nothing.
    }

    /**
     * Obtains current time.
     *
     * @return current time
     */
    public static Timestamp getCurrentTime() {
        final Timestamp result = timeProvider.get()
                                             .getCurrentTime();
        return result;
    }

    /**
     * Obtains system time.
     *
     * <p>Unlike {@link #getCurrentTime()} this method <strong>always</strong> uses
     * system time millis.
     *
     * @return current system time
     */
    public static Timestamp systemTime() {
        return fromMillis(System.currentTimeMillis());
    }

    /**
     * Sets provider of the current time.
     *
     * <p>The most common scenario for using this method is test cases of code that deals
     * with current time.
     *
     * @param provider the provider to set
     */
    @Internal
    @VisibleForTesting
    public static void setProvider(Provider provider) {
        timeProvider.set(checkNotNull(provider));
    }

    /**
     * Sets the default current time provider that obtains current time from system millis.
     */
    public static void resetProvider() {
        timeProvider.set(new SystemTimeProvider());
    }

    /**
     * The provider of the current time.
     *
     * <p>Implement this interface and pass the resulting class to
     */
    @Internal
    public interface Provider {
        Timestamp getCurrentTime();
    }

    /**
     * Default implementation of current time provider based on {@link System#currentTimeMillis()}.
     *
     * <p>This is the only place, which should invoke obtaining current time from the system millis.
     */
    private static class SystemTimeProvider implements Provider {
        @Override
        public Timestamp getCurrentTime() {
            final Timestamp result = fromMillis(System.currentTimeMillis());
            return result;
        }
    }
}
