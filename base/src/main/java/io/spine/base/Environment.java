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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.annotation.SPI;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Provides information about the environment (current platform used, etc.).
 *
 * <h1>Environment Type Detection</h1>
 *
 * <p>Current implementation allows to detect the type of the current environment. The framework
 * brings two environment types out-of-the-box:
 *
 * <ul>
 * <li><em>{@link Tests}</em> is detected if the current call stack has a reference to the unit
 * testing framework.
 *
 * <li><em>{@link Production}</em> is set in all other cases.
 * </ul>
 *
 * <p>The framework users may define their custom settings depending on the current environment
 * type:
 *
 * <pre>
 *
 * public final class Application {
 *
 *     private final EmailSender sender;
 *
 *     private Application() {
 *         Environment environment = Environment.instance();
 *         if(environment.is(Tests.instance())) {
 *             // Do not send out emails if in tests.
 *             this.sender = new MockEmailSender();
 *         } else {
 *             this.sender = EmailSender.withConfig("email_gateway.yml");
 *         }
 *         //...
 *     }
 * }
 * </pre>
 *
 * <h1>Custom environment types</h1>
 *
 * {@code Environment} allows to {@link #register(EnvironmentType) reguster custom types}.
 * In this case the environment detection functionality iterates over all known types, starting
 * with those registered by the framework user:
 *
 * <pre>
 *
 * public final class Application {
 *
 *     static {
 *         Environment.instance()
 *                    .register(StagingEnvironmentType.instance())
 *                    .register(LoadTestingType.instance());
 *     }
 *
 *     private final ConnectionPool pool;
 *
 *     private Application() {
 *         Environment environment = Environment.instance();
 *         if (environment.is(Tests.instance()) {
 *              // Single connection is enough for tests.
 *             this.pool = new ConnectionPoolImpl(PoolCapacity.of(1));
 *         } else {
 *             if(environment.is(LoadTesting.instance()) {
 *                 this.pool =
 *                         new ConnectionPoolImpl(PoolCapacity.fromConfig("load_tests.yml"));
 *             } else {
 *                 this.pool =
 *                         new ConnectionPoolImpl(PoolCapacity.fromConfig("cloud_deployment.yml"));
 *             }
 *         }
 *         //...
 *     }
 * }
 * </pre>
 *
 * <p><b>When registering custom types, please ensure</b> their mutual exclusivity.
 * If two or more environment types {@linkplain EnvironmentType#enabled() consider themselves
 * enabled} at the same time, the behaviour of {@link #is(EnvironmentType)}} is undefined.
 *
 * @see EnvironmentType
 */
@SPI
public final class Environment {

    private static final ImmutableList<EnvironmentType> BASE_TYPES =
            ImmutableList.of(Tests.instance(), Production.instance());

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
     * Remembers the specified environment type, allowing {@linkplain #is(EnvironmentType) to
     * determine whether it's enabled} later.
     *
     * <p>If the specified environment type has already been registered, throws an
     * {@code IllegalStateException}.
     *
     * <p>Note that the default types are still present.
     * When trying to determine which environment type is enabled, the user-defined types are
     * checked first, in the first-registered to last-registered order.
     *
     * @param environmentType
     *         a user-defined environment type
     * @return this instance of {@code Environment}
     * @see Tests
     * @see Production
     */
    @CanIgnoreReturnValue
    public Environment register(EnvironmentType environmentType) {
        checkState(!knownEnvTypes.contains(environmentType),
                   "Attempted to register the same custom env type `%s` twice." +
                           "Please make sure to call `Environment.register(...) only once" +
                           "per environment type.", environmentType.getClass()
                                                                   .getSimpleName());
        knownEnvTypes = ImmutableList
                .<EnvironmentType>builder()
                .add(environmentType)
                .addAll(INSTANCE.knownEnvTypes)
                .build();
        return this;
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
     * Determines whether the current environment is the same as the specified one.
     *
     * <p>If {@linkplain #register(EnvironmentType) custom env types have been defined},
     * goes through them in the latest-registered to earliest-registered order.
     * Then, checks {@link Tests} and {@link Production}.
     *
     * @return the current environment type.
     */
    public boolean is(EnvironmentType type) {
        if (currentEnvType == null) {
            determineCurrentType();
        }
        return currentEnvType.equals(type);
    }

    private void determineCurrentType() {
        for (EnvironmentType type : knownEnvTypes) {
            if (type.enabled()) {
                this.currentEnvType = type;
                return;
            }
        }
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
     * Resets the instance and clears the {@link Tests#ENV_KEY_TESTS} variable.
     */
    @VisibleForTesting
    public void reset() {
        this.currentEnvType = null;
        this.knownEnvTypes = BASE_TYPES;
        Tests.clearTestingEnvVariable();
    }
}
