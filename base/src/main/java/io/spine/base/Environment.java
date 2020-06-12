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
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.reflect.Invokables.invokeParameterlessCtor;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Provides information about the environment (current platform used, etc.).
 *
 * <h1>Environment Type Detection</h1>
 *
 * <p>Current implementation allows to {@linkplain #type() obtain the type} of the current
 * environment, or to check whether current environment type {@linkplain #is(Class) matches
 * another type}.
 * Two environment types exist out of the box:
 *
 * <ul>
 *     <li><em>{@link Tests}</em> is detected if the current call stack has a reference to the unit
 *         testing framework.
 *
 *     <li><em>{@link Production}</em> is set in all other cases.
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
 *         if(environment.is(Tests.class)) {
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
 *                    .register(new Staging())
 *                    .register(new LoadTesting());
 *     }
 *
 *     private final ConnectionPool pool;
 *
 *     private Application() {
 *         Environment environment = Environment.instance();
 *         if (environment.is(Tests.class) {
 *              // Single connection is enough for tests.
 *             this.pool = new ConnectionPoolImpl(PoolCapacity.of(1));
 *         } else {
 *             if(environment.is(LoadTesting.class) {
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
 * <h1>Caching</h1>
 * <p>{@code Environment} caches the {@code EnvironmentType} once its calculated.
 * This means that if one environment type has been found to be active, its instance is saved.
 * If later it becomes logically inactive, e.g. the environment variable that's used to check the
 * environment type changes, {@code Environment} is still going to return the cached value, unless
 * it is {@linkplain #setTo(EnvironmentType) set manually} or {@linkplain #reset() reset
 * explicitly}.
 *
 * For example:
 * <pre>
 *     Environment environment = Environment.instance();
 *     EnvironmentType awsLambda = new AwsLambda();
 *     environment.register(awsLambda);
 *     assertThat(environment.is(AwsLambda.class)).isTrue();
 *
 *     System.clearProperty(AwsLambda.AWS_ENV_VARIABLE);
 *
 *     // Even though `AwsLambda` is not active, we have cached the value, and `is(AwsLambda.class)`
 *     // is `true`.
 *     assertThat(environment.is(AwsLambda.class)).isTrue();
 *
 *     environment.reset();
 *
 *     // When `reset` explicitly, cached value is erased.
 *     assertThat(environment.is(AwsLambda.class)).isFalse();
 * </pre>
 *
 * <p>{@linkplain #setTo(EnvironmentType) explicitly setting} the environment type overrides
 * the cached value.
 *
 * <p><b>When registering custom types, please ensure</b> their mutual exclusivity.
 * If two or more environment types {@linkplain EnvironmentType#enabled() consider themselves
 * enabled} at the same time, the behaviour of {@link #is(Class)}} is undefined.
 *
 * @see EnvironmentType
 * @see Tests
 * @see Production
 */
@SPI
public final class Environment {

    private static final ImmutableList<EnvironmentType> BASE_TYPES =
            ImmutableList.of(new Tests(), new Production());

    private static final Environment INSTANCE = new Environment();

    private ImmutableList<EnvironmentType> knownEnvTypes;
    private @Nullable Class<? extends EnvironmentType> currentEnvType;

    private Environment() {
        this.knownEnvTypes = BASE_TYPES;
    }

    /** Creates a new instance with the copy of the state of the passed environment. */
    private Environment(Environment copy) {
        this.knownEnvTypes = copy.knownEnvTypes;
        this.currentEnvType = copy.currentEnvType;
    }

    /**
     * Remembers the specified environment type, allowing {@linkplain #is(Class) to
     * determine whether it's enabled} later.
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
        if (!knownEnvTypes.contains(environmentType)) {
            knownEnvTypes = ImmutableList
                    .<EnvironmentType>builder()
                    .add(environmentType)
                    .addAll(INSTANCE.knownEnvTypes)
                    .build();
        }
        return this;
    }

    /**
     * Remembers the specified environment type, allowing {@linkplain #is(Class) to
     * determine whether it's enabled} later.
     *
     * <p>The specified {@code type} must have a parameterless constructor.
     * {@linkplain EnvironmentType#enabled() activity} of the specified environment type is going
     * to be checked against an instance created by invoking the parameterless constructor.
     *
     * <p>Otherwise, behaves like {@link #register(EnvironmentType)}.
     *
     * @param type
     *         environment type to register
     * @return this instance of {@code Environment}
     */
    @Internal
    @CanIgnoreReturnValue
    Environment register(Class<? extends EnvironmentType> type) {
        EnvironmentType envTypeInstance = invokeParameterlessCtor(type);
        return register(envTypeInstance);
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
     * <p>Please note that {@code is} follows assigment-compatibility:
     * <pre>
     *     abstract class AppEngine extends EnvironmentType {
     *         ...
     *     }
     *
     *     final class AppEngineStandard extends AppEngine {
     *         ...
     *     }
     *
     *     Environment environment = Environment.instance();
     *
     *     // Assuming we are under App Engine Standard
     *     assertThat(environment.is(AppEngine.class)).isTrue();
     *
     * </pre>
     *
     * @return whether the current environment type matches the specified one
     */
    public boolean is(Class<? extends EnvironmentType> type) {
        Class<? extends EnvironmentType> currentEnv = cachedOrCalculated();
        boolean result = type.isAssignableFrom(currentEnv);
        return result;
    }

    /** Returns the type of the current environment. */
    public Class<? extends EnvironmentType> type() {
        Class<? extends EnvironmentType> currentEnv = cachedOrCalculated();
        return currentEnv;
    }

    /**
     * Verifies if the code currently runs under a unit testing framework.
     *
     * @see Tests
     * @deprecated use {@code Environment.instance().is(Tests.class)}
     */
    @Deprecated
    public boolean isTests() {
        return is(Tests.class);
    }

    /**
     * Verifies if the code runs in the production mode.
     *
     * <p>This method is opposite to {@link #isTests()}.
     *
     * @return {@code true} if the code runs in the production mode, {@code false} otherwise
     * @see Production
     * @deprecated use {@code Environment.instance().is(Production.class)}
     */
    @Deprecated
    public boolean isProduction() {
        return !isTests();
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
     * Sets the current environment type to {@code type.getClass()}. Overrides the current value.
     */
    @VisibleForTesting
    public void setTo(EnvironmentType type) {
        this.currentEnvType = checkNotNull(type).getClass();
    }

    /**
     * Sets the current environment type to the specified one. Overrides the current value.
     */
    @Internal
    @VisibleForTesting
    public void setTo(Class<? extends EnvironmentType> type) {
        checkNotNull(type);
        this.currentEnvType = type;
    }

    /**
     * Turns the test mode on.
     *
     * <p>This method is opposite to {@link #setToProduction()}.
     *
     * @deprecated use {@link #setTo(Class)}
     */
    @Deprecated
    @VisibleForTesting
    public void setToTests() {
        this.currentEnvType = Tests.class;
        Tests.enable();
    }

    /**
     * Turns the production mode on.
     *
     * <p>This method is opposite to {@link #setToTests()}.
     *
     * @deprecated use {@link #setTo(Class)}
     */
    @Deprecated
    @VisibleForTesting
    public void setToProduction() {
        this.currentEnvType = Production.class;
        Tests.clearTestingEnvVariable();
    }

    /**
     * Resets the instance and clears the {@link Tests#ENV_KEY_TESTS} variable.
     *
     * <p>Erases all registered environment types, leaving only {@code Tests} and {@code
     * Production}.
     */
    @VisibleForTesting
    public void reset() {
        this.currentEnvType = null;
        this.knownEnvTypes = BASE_TYPES;
        Tests.clearTestingEnvVariable();
    }

    private Class<? extends EnvironmentType> cachedOrCalculated() {
        Class<? extends EnvironmentType> result = currentEnvType != null
                                                  ? currentEnvType
                                                  : currentType();
        this.currentEnvType = result;
        return result;
    }

    private Class<? extends EnvironmentType> currentType() {
        for (EnvironmentType type : knownEnvTypes) {
            if (type.enabled()) {
                return type.getClass();
            }
        }

        throw newIllegalStateException("`Environment` could not find an active environment type.");
    }
}
