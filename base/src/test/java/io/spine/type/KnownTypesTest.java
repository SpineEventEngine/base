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

package io.spine.type;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Error;
import io.spine.code.java.ClassName;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import io.spine.test.types.KnownTask;
import io.spine.test.types.KnownTaskId;
import io.spine.test.types.KnownTaskName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertUnknownType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link io.spine.type.KnownTypes}.
 */
@DisplayName("`KnownTypes` should")
class KnownTypesTest {

    private final KnownTypes knownTypes = KnownTypes.instance();

    @Test
    @DisplayName("obtain type URLs of known proto types")
    void typeUrls() {
        var typeUrls = knownTypes.allUrls();

        assertFalse(typeUrls.isEmpty());
    }

    @Nested
    @DisplayName("contain types")
    class ContainTypes {

        @Test
        @DisplayName("defined by Spine framework")
        void containsSpineTypes() {
            assertContainsClass(EntityOption.class);
            assertContainsClass(Error.class);
            assertContainsClass(IfMissingOption.class);
        }

        @Test
        @DisplayName("from Google Protobuf")
        void containsProtobufTypes() {
            assertContainsClass(Any.class);
            assertContainsClass(Timestamp.class);
            assertContainsClass(Duration.class);
            assertContainsClass(Empty.class);
        }

        private void assertContainsClass(Class<? extends Message> msgClass) {
            var typeUrl = TypeUrl.of(msgClass);
            var className = knownTypes.classNameOf(typeUrl);

            assertThat(className)
                    .isEqualTo(ClassName.of(msgClass));
        }

        @Test
        @DisplayName("nested into other proto types")
        void containNestedProtoTypes() {
            var typeUrl = TypeUrl.from(EntityOption.Kind.getDescriptor());
            var className = knownTypes.classNameOf(typeUrl);

            assertThat(className)
                    .isEqualTo(ClassName.of(EntityOption.Kind.class));
        }
    }

    @Test
    @DisplayName("find type URL by type name")
    void findTypeUrlByName() {
        var typeUrlExpected = TypeUrl.from(StringValue.getDescriptor());

        var typeUrlActual = knownTypes.find(typeUrlExpected.toTypeName())
                                      .map(Type::url);
        assertTrue(typeUrlActual.isPresent());
        assertEquals(typeUrlExpected, typeUrlActual.get());
    }

    @Test
    @DisplayName("obtain all types under a given package")
    void typesFromPackage() {
        var taskId = TypeUrl.from(KnownTaskId.getDescriptor());
        var taskName = TypeUrl.from(KnownTaskName.getDescriptor());
        var task = TypeUrl.from(KnownTask.getDescriptor());

        var packageName = "spine.test.types";

        var packageTypes = knownTypes.allFromPackage(packageName);

        assertThat(packageTypes)
                .containsAtLeast(taskId, taskName, task);
    }

    @Test
    @DisplayName("return empty set of types for unknown package")
    void emptyTypeSetForUnknownPackage() {
        var packageName = "com.foo.invalid.package";
        Set<?> emptyTypesCollection = knownTypes.allFromPackage(packageName);
        assertNotNull(emptyTypesCollection);
        assertTrue(emptyTypesCollection.isEmpty());
    }

    @Test
    @DisplayName("do not return types by package prefix")
    void noTypesByPrefix() {
        var prefix = "spine.test.ty"; // "spine.test.types" is a valid package

        Collection<TypeUrl> packageTypes = knownTypes.allFromPackage(prefix);
        assertTrue(packageTypes.isEmpty());
    }

    @Test
    @DisplayName("throw UnknownTypeException for requesting info on an unknown type")
    void throwOnUnknownType() {
        var unexpectedUrl = TypeUrl.parse("prefix/unexpected.type");
        assertUnknownType(() -> knownTypes.classNameOf(unexpectedUrl));
    }

    @Test
    @DisplayName("print known type URLs in alphabetical order")
    void printingTypeUrls() {
        var list = knownTypes.printAllTypes();

        var assertOutput = assertThat(list);
        assertOutput.isNotEmpty();

        var anyUrl = TypeUrl.from(Any.getDescriptor()).value();
        var timestampUrl = TypeUrl.from(Timestamp.getDescriptor()).value();
        var durationUrl = TypeUrl.from(Duration.getDescriptor()).value();

        assertOutput.contains(anyUrl);
        assertOutput.contains(timestampUrl);
        assertOutput.contains(durationUrl);

        var anyIndex = list.indexOf(anyUrl);
        var durationIndex = list.indexOf(durationUrl);
        var timestampIndex = list.indexOf(timestampUrl);

        assertTrue(anyIndex < timestampIndex);
        assertTrue(durationIndex < timestampIndex);
    }
}
