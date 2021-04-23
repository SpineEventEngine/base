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

package io.spine.tools.mc.java.protoc.field;

import com.google.common.testing.NullPointerTester;
import io.spine.base.SubscribableField;
import io.spine.tools.java.code.FieldFactory;
import io.spine.tools.java.protoc.ConfigByType;
import io.spine.tools.java.protoc.TypePattern;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.protoc.plugin.nested.TaskView;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;

@DisplayName("`GenerateFieldsByType` task should")
final class GenerateFieldsByTypeTest {

    private final FieldFactory factory = new FieldFactory();

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester()
                .testAllPublicInstanceMethods(newTask());
    }

    @Test
    @DisplayName("throw `IAE` if `TypePattern` is not set")
    @SuppressWarnings("CheckReturnValue") // The method called to throw an exception.
    void rejectEmptyFilePattern() {
        assertIllegalArgument(() -> newTask(emptyConfig()));
    }

    @Test
    @DisplayName("reject empty field type name")
    @SuppressWarnings("CheckReturnValue") // The method called to throw an exception.
    void rejectEmptyFieldTypeName() {
        String emptyName = "";
        assertIllegalArgument(() -> newTask(emptyName));
    }

    @Test
    @DisplayName("reject effectively empty field type name")
    @SuppressWarnings("CheckReturnValue") // The method called to throw an exception.
    void rejectEffectivelyEmptyFactoryName() {
        String effectivelyEmptyName = "   ";
        assertIllegalArgument(() -> newTask(effectivelyEmptyName));
    }

    @Nested
    @DisplayName("generate empty result if")
    class GenerateEmptyResult {

        @Test
        @DisplayName("message does not match pattern")
        void messageDoesNotMatchPattern() {
            ConfigByType config = config(pattern("some.non.matching.Type"));
            Collection<CompilerOutput> output = newTask(config).generateFor(testType());
            assertThat(output).isEmpty();
        }
    }

    @Test
    @DisplayName("generate fields if the message matches the pattern")
    void generateNewMethods() {
        Collection<CompilerOutput> output = newTask().generateFor(testType());
        assertThat(output).isNotEmpty();
    }

    private GenerateFieldsByType newTask() {
        return newTask(config());
    }

    private GenerateFieldsByType newTask(String fieldSupertype) {
        return newTask(config(fieldSupertype));
    }

    private GenerateFieldsByType newTask(ConfigByType config) {
        return new GenerateFieldsByType(config, factory);
    }

    private static ConfigByType emptyConfig() {
        return ConfigByType.newBuilder()
                           .build();
    }

    private static ConfigByType config() {
        TypePattern pattern = pattern();
        return config(pattern);
    }

    private static ConfigByType config(String fieldSupertype) {
        TypePattern pattern = pattern();
        return config(fieldSupertype, pattern);
    }

    private static ConfigByType config(TypePattern pattern) {
        return config(SubscribableField.class.getCanonicalName(), pattern);
    }

    private static ConfigByType config(String fieldSupertype, TypePattern pattern) {
        ConfigByType result = ConfigByType
                .newBuilder()
                .setValue(fieldSupertype)
                .setPattern(pattern)
                .build();
        return result;
    }

    private static TypePattern pattern() {
        return pattern(TaskView.getDescriptor().getFullName());
    }

    private static TypePattern pattern(String expectedType) {
        TypePattern result = TypePattern
                .newBuilder()
                .setExpectedType(expectedType)
                .build();
        return result;
    }

    private static MessageType testType() {
        return new MessageType(TaskView.getDescriptor());
    }
}
