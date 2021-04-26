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

package io.spine.tools.mc.java.gradle;

import com.google.common.collect.ImmutableList;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.query.EntityStateField;
import io.spine.tools.java.protoc.AddFields;
import io.spine.tools.java.protoc.ConfigByType;
import io.spine.tools.java.protoc.EntityStateConfig;
import io.spine.tools.java.protoc.TypePattern;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A configuration of strongly-typed message fields to be generated for certain message types.
 *
 * @see io.spine.base.SubscribableField
 */
public final class Fields extends ModelCompilerConfiguration<AddFields> {

    private EntityStateConfig entityStateConfig = EntityStateConfig.getDefaultInstance();
    private final Map<String, ClassName> byType = new HashMap<>();

    /**
     * Configures strongly-typed fields generation for messages that represent an entity state.
     *
     * <p>Example:
     * <pre>
     * generateFor messages().entityState(), markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables the strongly-typed fields generation for all messages that
     * represent an entity state and marks the generated fields as {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a {@code public} non-{@code final} descendant of {@link EntityStateField} and
     * has a {@code public} constructor accepting a single {@link io.spine.base.Field Field}
     * argument.
     */
    @SuppressWarnings("unused") // Gradle DSL.
    public final void generateFor(EntityStateSelector entityState, ClassName markAs) {
        entityStateConfig = EntityStateConfig
                .newBuilder()
                .setValue(markAs.value())
                .build();
    }

    /**
     * Configures the strongly-typed fields generation for messages declared in files matching
     * a given pattern.
     *
     * <p>Example:
     * <pre>
     * generateFor messages().inFiles(suffix: "messages.proto"), markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables strongly-typed fields generation for all messages residing in
     * files with names ending with {@code messages.proto} and marks the generated fields as
     * {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a {@code public} non-{@code final} descendant of {@link io.spine.base.SubscribableField} and
     * has a {@code public} constructor accepting a single {@link io.spine.base.Field Field}
     * argument.
     *
     * <p>In case the field generation is configured for {@link io.spine.base.EventMessage event}
     * and {@link io.spine.base.RejectionMessage rejection} messages, the passed field type should
     * inherit from {@link io.spine.base.EventMessageField}.
     *
     * <p>The configuration may be applied multiple times to enable code generation for multiple
     * file patterns.
     */
    public final void generateFor(PatternSelector pattern, ClassName markAs) {
        addPattern(pattern, markAs);
    }

    /**
     * Configures the strongly-typed fields generation for a message type with the passed name.
     *
     * <p>Example:
     * <pre>
     * generateFor "custom.message.Type", markAs("com.some.custom.Field")
     * </pre>
     *
     * <p>The statement above enables strongly-typed fields generation for the message type with
     * Proto name {@code custom.message.Type} and marks the generated fields as
     * {@code some.custom.Field}.
     *
     * <p>It is expected by the Spine routines that the type passed to the {@code markAs} method is
     * a {@code public} non-{@code final} descendant of {@link io.spine.base.SubscribableField} and
     * has a {@code public} constructor accepting a single {@link io.spine.base.Field Field}
     * argument.
     *
     * <p>The configuration may be applied multiple times to enable code generation for multiple
     * message types.
     */
    public final void generateFor(String type, ClassName markAs) {
        byType.put(type, markAs);
    }

    /**
     * A syntax sugar method used for a more natural Gradle DSL.
     */
    @SuppressWarnings({"MethodMayBeStatic", "unused"}) // Gradle DSL.
    public final ClassName markAs(String type) {
        return ClassName.of(type);
    }

    @Internal
    @Override
    public AddFields asProtocConfig() {
        AddFields.Builder result = AddFields
                .newBuilder()
                .setEntityStateConfig(entityStateConfig)
                .addAllConfigByType(generatedTypes());
        patternConfigurations()
                .stream()
                .map(ModelCompilerConfiguration::toPatternConfig)
                .forEach(result::addConfigByPattern);
        return result.build();
    }

    private Iterable<ConfigByType> generatedTypes() {
        ImmutableList<ConfigByType> result =
                byType.entrySet()
                      .stream()
                      .map(entry -> configByType(entry.getKey(), entry.getValue()))
                      .collect(toImmutableList());
        return result;
    }

    private static ConfigByType configByType(String type, ClassName markAs) {
        TypePattern pattern = TypePattern
                .newBuilder()
                .setExpectedType(type)
                .build();
        ConfigByType result = ConfigByType
                .newBuilder()
                .setValue(markAs.value())
                .setPattern(pattern)
                .build();
        return result;
    }
}
