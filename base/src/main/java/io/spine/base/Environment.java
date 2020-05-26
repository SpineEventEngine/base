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
import io.spine.annotation.SPI;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.base.BaseEnvironmentType.PRODUCTION;
import static io.spine.base.BaseEnvironmentType.TESTS;

/**
 * Provides information about the environment (current platform used, etc.).
 *
 * <p>By default, knows only about {@link BaseEnvironmentType}. Library users may extend the list
 * of known environment types by providing their environment {@code enum} to
 * {@linkplain #registerCustom(Class)}.
 *
 * <p><b>When extending, please note</b> that this class does not handle the situations when two
 * or more {@linkplain EnvironmentType environment types} return {@code true} on the
 * {@link EnvironmentType#currentlyOn()}. As such, if two or more user-defined environment types
 * think that they are currently on, <b>the behaviour of {@link #envType()} is undefined.</b>
 */
@SPI
public final class Environment {

    private static final ImmutableList<EnvironmentType> BASE_TYPES =
            ImmutableList.of(TESTS, PRODUCTION);

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
     * Registers all of the constants from the provided {@code EnvironmentType}-implementing
     * {@code enum}.
     *
     * <p>If the specified {@code enum} has already been registered, throws
     * an {@code IllegalStateException}.
     *
     * <p>Note that the {@linkplain BaseEnvironmentType default types} are still present.
     * When trying to {@linkplain #envType() determine which environment type} is currently on,
     * the user defined types are checked first.
     *
     * @param enumClass
     *         an enum class that specifies the environment types
     * @param <E>
     *         a type that defines possible environment types
     */
    public static <E extends Enum & EnvironmentType> void registerCustom(Class<E> enumClass) {
        checkNotNull(enumClass);
        ImmutableList<E> newTypes = ImmutableList.copyOf(enumClass.getEnumConstants());
        checkState(!INSTANCE.knownEnvTypes.containsAll(newTypes),
                   "Attempted to register the same custom env enum `%s` twice." +
                           "Please make sure to call `Environment.registerCustom(...) only once" +
                           "per enum.", enumClass.getSimpleName());
        INSTANCE.knownEnvTypes = ImmutableList
                .<EnvironmentType>builder()
                .addAll(newTypes)
                .addAll(BASE_TYPES)
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
     * <p>If {@linkplain #registerCustom(Class) user has defined custom env types}, goes through
     * them in an undefined order. Then, checks the {@linkplain BaseEnvironmentType base env
     * types}.
     *
     * <p> Note that if all of the {@link EnvironmentType#currentlyOn()} checks have returned
     * {@code false}, this method falls back on {@link BaseEnvironmentType#PRODUCTION}.
     *
     * @return the current environment type.
     */
    public EnvironmentType envType() {
        if (currentEnvType == null) {
            for (EnvironmentType type : knownEnvTypes) {
                if (type.currentlyOn()) {
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
        if (currentEnvType != null) {
            currentEnvType.setTo();
        }
    }

    /**
     * Forces the specified environment type to be the current one.
     */
    @VisibleForTesting
    public void setTo(EnvironmentType type) {
        this.currentEnvType = type;
        currentEnvType.setTo();
    }

    /**
     * Resets the instance and performs {@linkplain EnvironmentType#reset() the
     * environment-specific reset}.
     *
     * <p>Also the resets the {@linkplain #registerCustom(Class) user-defined environment types}.
     */
    @VisibleForTesting
    public void reset() {
        if (currentEnvType != null) {
            currentEnvType.reset();
        }
        this.currentEnvType = null;
        this.knownEnvTypes = BASE_TYPES;
    }
}
