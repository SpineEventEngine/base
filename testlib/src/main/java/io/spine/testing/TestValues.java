/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.protobuf.StringValue;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility factories for test values.
 */
public final class TestValues {

    /** Prevents instantiation of this utility class. */
    private TestValues() {
    }

    /**
     * Generates a string value based on {@linkplain java.util.UUID#randomUUID() generated UUID}.
     */
    public static String randomString() {
        return UUID.randomUUID()
                   .toString();
    }

    /**
     * Generates a {@code StringValue} with generated UUID.
     */
    public static StringValue newUuidValue() {
        String id = randomString();
        return StringValue.newBuilder()
                          .setValue(id)
                          .build();
    }

    /**
     * Generates a random integer in the range [0, max).
     */
    public static int random(int max) {
        return random(0, max);
    }

    /**
     * Generates a random integer in the range [min, max).
     */
    public static int random(int min, int max) {
        int randomNum = ThreadLocalRandom.current().nextInt(min, max);
        return randomNum;
    }

    /**
     * Generates a random long value in the range [min, max).
     */
    public static long longRandom(long min, long max) {
        long randomNum = ThreadLocalRandom.current().nextLong(min, max);
        return randomNum;
    }

    /**
     * Returns {@code null}.
     *
     * <p>Use it when it is needed to pass {@code null} to a method in tests so that no
     * warnings suppression is needed.
     *
     * @apiNote The method doesn't take {@code Class<T>} to keep
     *         the test API small and convenient
     */
    @SuppressWarnings("TypeParameterUnusedInFormals" /* See api note. */)
    public static <T> T nullRef() {
        T nullRef = null;
        return nullRef;
    }
}
