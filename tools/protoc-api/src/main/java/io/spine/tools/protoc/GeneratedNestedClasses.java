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

package io.spine.tools.protoc;

import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.protoc.ProtocTaskConfigs.queryableConfig;
import static io.spine.tools.protoc.ProtocTaskConfigs.subscribableConfig;

/**
 * A configuration of nested classes to be generated for Java message classes.
 */
public final class GeneratedNestedClasses extends GeneratedConfigurations<AddNestedClasses> {

    /**
     * A config which specifies which nested classes are generated for the queryable messages.
     */
    private QueryableConfig queryableConfig = QueryableConfig.getDefaultInstance();

    /**
     * A config which specified which nested classes are generated for the subscribable messages.
     */
    private SubscribableConfig subscribableConfig  = SubscribableConfig.getDefaultInstance();

    /**
     * Configures nested class generation for messages declared in files matching a given pattern.
     *
     * <p>Sample usage is:
     * <pre>
     * applyFactory "io.spine.code.CustomNestedClassFactory", messages().inFiles(suffix: "events.proto")
     * </pre>
     *
     * <p>The statement above configures all message types which are declared in proto files whose
     * name end with {@code events.proto} to use the {@code CustomNestedClassFactory} for nested
     * class code generation. It is expected that {@code CustomNestedClassFactory} is an
     * implementation of the {@link io.spine.tools.protoc.nested.NestedClassFactory} interface.
     *
     * <p>NOTE: in order for the framework components to function properly, the implementation
     * of {@code NestedClassFactory} should always be public and have a no-argument public
     * constructor.
     */
    public final void applyFactory(@FullyQualifiedName String factory, PatternSelector selector) {
        checkNotNull(factory);
        checkNotNull(selector);
        addPattern(selector, ClassName.of(factory));
    }

    /**
     * Configures entity column generation for queryable entities.
     *
     * <p>Sample usage is:
     * <pre>
     * applyFactory "io.spine.code.CustomColumnFactory", messages().queryable()
     * </pre>
     *
     * <p>The statement above will apply the {@code io.spine.code.CustomColumnFactory} nested class
     * factory to all messages that are entity states and have entity columns.
     *
     * <p>By default, Spine uses this configuration to generate strongly-typed column enumerations
     * for all queryable message types.
     *
     * <p>NOTE: it is expected, that the {@code CustomColumnFactory} is an implementation of
     * {@link io.spine.tools.protoc.nested.NestedClassFactory}, and also is public and has a
     * no-argument public constructor.
     */
    public final void applyFactory(@FullyQualifiedName String factory, QueryableMessage selector) {
        checkNotNull(selector);
        queryableConfig = queryableConfig(ClassName.of(factory));
    }


    /**
     * Configures field generation for subscribable messages.
     *
     * <p>The subscribable messages include {@link io.spine.base.EventMessage event messages},
     * {@link io.spine.base.EntityState entity states} and several other types.
     *
     * <p>Sample usage is:
     * <pre>
     * applyFactory "io.spine.code.CustomFieldFactory", messages().subscribable()
     * </pre>
     *
     * <p>The statement above will apply the {@code io.spine.code.CustomFieldFactory} nested class
     * factory to all messages that can be subscription targets and also several others, as
     * required by the Spine routines.
     *
     * <p>By default, this configuration is used to generate strongly-typed field enumerations
     * for all types that qualify as subscribable message types.
     *
     * <p>NOTE: it is expected, that the {@code CustomFieldFactory} is an implementation of
     * {@link io.spine.tools.protoc.nested.NestedClassFactory}, and also is public and has a
     * no-argument public constructor.
     */
    public final void
    applyFactory(@FullyQualifiedName String factory, SubscribableMessage selector) {
        checkNotNull(selector);
        subscribableConfig = subscribableConfig(ClassName.of(factory));
    }

    @Internal
    @Override
    public AddNestedClasses asProtocConfig() {
        AddNestedClasses.Builder result = AddNestedClasses
                .newBuilder()
                .setQueryableFactory(queryableConfig)
                .setSubscribableFactory(subscribableConfig);
        patternConfigurations()
                .stream()
                .map(GeneratedConfigurations::toPatternConfig)
                .forEach(result::addFactoryByPattern);
        return result.build();
    }
}
