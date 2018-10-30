/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.truth.IterableSubject;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Error;
import io.spine.code.proto.Type;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import io.spine.test.types.KnownTask;
import io.spine.test.types.KnownTaskId;
import io.spine.test.types.KnownTaskName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link io.spine.type.KnownTypes}.
 */
@DisplayName("KnownTypes should")
class KnownTypesTest {

    private final KnownTypes knownTypes = KnownTypes.instance();

    @Test
    @DisplayName("obtain type URLs of known proto types")
    void typeUrls() {
        Set<TypeUrl> typeUrls = knownTypes.getAllUrls();

        assertFalse(typeUrls.isEmpty());
    }

    @Test
    @DisplayName("contain types defined by Spine framework")
    void containsSpineTypes() {
        assertContainsClass(EntityOption.class);
        assertContainsClass(Error.class);
        assertContainsClass(IfMissingOption.class);
    }

    @Test
    @DisplayName("contain types from Google Protobuf")
    void containsProtobufTypes() {
        assertContainsClass(Any.class);
        assertContainsClass(Timestamp.class);
        assertContainsClass(Duration.class);
        assertContainsClass(Empty.class);
    }

    private void assertContainsClass(Class<? extends Message> msgClass) {
        TypeUrl typeUrl = TypeUrl.of(msgClass);
        ClassName className = knownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(msgClass), className);
    }

    @Test
    @DisplayName("contain nested proto types")
    void containNestedProtoTypes() {
        TypeUrl typeUrl = TypeUrl.from(EntityOption.Kind.getDescriptor());
        ClassName className = knownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(EntityOption.Kind.class), className);
    }

    @Test
    @DisplayName("find type URL by type name")
    void findTypeUrlByName() {
        TypeUrl typeUrlExpected = TypeUrl.from(StringValue.getDescriptor());

        Optional<TypeUrl> typeUrlActual = knownTypes.find(typeUrlExpected.toName())
                                                    .map(Type::url);
        assertTrue(typeUrlActual.isPresent());
        assertEquals(typeUrlExpected, typeUrlActual.get());
    }

    @Test
    @DisplayName("obtain all types under a given package")
    void typesFromPackage() {
        TypeUrl taskId = TypeUrl.from(KnownTaskId.getDescriptor());
        TypeUrl taskName = TypeUrl.from(KnownTaskName.getDescriptor());
        TypeUrl task = TypeUrl.from(KnownTask.getDescriptor());

        String packageName = "spine.test.types";

        Set<TypeUrl> packageTypes = knownTypes.getAllFromPackage(packageName);

        IterableSubject assertTypes = assertThat(packageTypes);
        assertTypes.hasSize(3);
        assertTypes.containsAllIn(of(taskId, taskName, task));
    }

    @Test
    @DisplayName("return empty set of types for unknown package")
    void emptyTypeSetForUnknownPackage() {
        String packageName = "com.foo.invalid.package";
        Set<?> emptyTypesCollection = knownTypes.getAllFromPackage(packageName);
        assertNotNull(emptyTypesCollection);
        assertTrue(emptyTypesCollection.isEmpty());
    }

    @Test
    @DisplayName("do not return types by package prefix")
    void noTypesByPrefix() {
        String prefix = "spine.test.ty"; // "spine.test.types" is a valid package

        Collection<TypeUrl> packageTypes = knownTypes.getAllFromPackage(prefix);
        assertTrue(packageTypes.isEmpty());
    }

    @Test
    @DisplayName("throw UnknownTypeException for requesting info on an unknown type")
    void throwOnUnknownType() {
        TypeUrl unexpectedUrl = TypeUrl.parse("prefix/unexpected.type");
        assertThrows(
                UnknownTypeException.class,
                () -> knownTypes.getClassName(unexpectedUrl)
        );
    }
}
