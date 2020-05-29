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

package io.spine.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Provides information about the environment (current platform used, etc.).
 *
 * <p><b>When extending, please ensure</b> the mutual exclusivity on your {@code EnvironmentType}s.
 * If two or more environment types {@linkplain EnvironmentType#enabled() consider themselves
 * enabled} at the same time, the behaviour of {@link #currentType()} is undefined.
 */
@SPI
public final class Environment {

    private static final ImmutableList<EnvironmentType> BASE_TYPES =
            ImmutableList.copyOf(BaseEnvironmentType.values());

    private static final Environment INSTANCE = new Environment();

    private ImmutableList<EnvironmentType> knownEnvTypes;
    private @Nullable EnvironmentType currentEnvType;

    private Environment() {
        this.knownEnvTypes = BASE_TYPES;
    }

    /** Creates a new instance with the copy of the state of the passed environment. */
    private Environment(Environment copy) {
        this.knownEnvTypes = copy.knownEnvTypes;
        this.currentEnvType = copy.currentEnvType;
    }

    /**
     * Remembers the specified environment type, allowing {@linkplain #currentType()}
     * to determine whether it's enabled} later.
     *
     * <p>If the specified environment type has already been registered, throws an
     * {@code IllegalStateException}.
     *
     * <p>Note that the {@linkplain BaseEnvironmentType default types} are still present.
     * When trying to {@linkplain #currentType() determine which environment type} is enabled,
     * the user-defined types are checked first.
     *
     * @param environmentType
     *         a user-defined environment type
     */
    @Internal
    public static void register(EnvironmentType environmentType) {
        checkState(!INSTANCE.knownEnvTypes.contains(environmentType),
                   "Attempted to register the same custom env type `%s` twice." +
                           "Please make sure to call `Environment.register(...) only once" +
                           "per environment type.", environmentType.getClass()
                                                                   .getSimpleName());
        INSTANCE.knownEnvTypes = ImmutableList
                .<EnvironmentType>builder()
                .add(environmentType)
                .addAll(INSTANCE.knownEnvTypes)
                .build();
    }

    /** Returns the singleton instance. */
    public static Environment instance() {
        return INSTANCE;
    }

    /**
     * Creates a copy of the instance so that it can be later
     * restored via {@link #restoreFrom(Environment)} by cleanup in tests.
     */
    @VisibleForTesting
    public Environment createCopy() {
        return new Environment(this);
    }

    /**
     * Determines the current environment type.
     *
     * <p>If {@linkplain #register(EnvironmentType) custom env types have been defined},
     * goes through them in an undefined order. Then, checks the {@linkplain BaseEnvironmentType
     * base env types}.
     *
     * <p>Note that if all of the {@link EnvironmentType#enabled()} checks have returned
     * {@code false}, this method falls back on {@link BaseEnvironmentType#PRODUCTION}.
     *
     * @return the current environment type.
     */
    public EnvironmentType currentType() {
        if (currentEnvType == null) {
            for (EnvironmentType type : knownEnvTypes) {
                if (type.enabled()) {
                    this.currentEnvType = type;
                    return this.currentEnvType;
                }
            }
        }
        return currentEnvType;
    }

    /**
     * Restores the state from the instance created by {@link #createCopy()}.
     *
     * <p>Call this method when cleaning up tests that modify {@code Environment}.
     */
    @VisibleForTesting
    public void restoreFrom(Environment copy) {
        // Make sure this matches the set of fields copied in the copy constructor.
        this.knownEnvTypes = copy.knownEnvTypes;
        this.currentEnvType = copy.currentEnvType;
    }

    /**
     * Forces the specified environment type to be the current one.
     */
    @VisibleForTesting
    public void setTo(EnvironmentType type) {
        this.currentEnvType = checkNotNull(type);
    }

    /**
     * Resets the instance.
     */
    @VisibleForTesting
    public void reset() {
        this.currentEnvType = null;
        this.knownEnvTypes = BASE_TYPES;
    }
}
