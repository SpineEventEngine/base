/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.base.environment;

import io.spine.base.EnvironmentType;

/**
 * An environment type that mimics production but receives less traffic and is suitable for testing
 * out new features.
 *
 * <p>This implementations relies on a static {@code boolean} flag for detection.
 */
public final class Staging extends EnvironmentType {

    @Override
    protected boolean enabled() {
        return Singleton.INSTANCE.enabled;
    }

    public static Staging type() {
        return Singleton.INSTANCE.staging;
    }

    /**
     * Brings the underlying system into the staging environment.
     */
    static void enable() {
        Singleton.INSTANCE.enabled = true;
    }

    /**
     * Brings the underlying system out of the staging environment.
     */
    static void disable() {
        Singleton.INSTANCE.enabled = false;
    }

    public enum Singleton {

        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Staging staging = new Staging();
        private boolean enabled;
    }
}
