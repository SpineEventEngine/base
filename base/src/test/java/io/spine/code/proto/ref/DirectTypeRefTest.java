/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.code.proto.ref;

import com.google.common.truth.Truth8;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt32Value;
import io.spine.code.proto.PackageName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;

@DisplayName("Direct type reference should")
class DirectTypeRefTest {

    @Nested
    @DisplayName("return Optional.empty() for")
    class ParsingToEmpty {

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEmpty("");
        }

        @Test
        @DisplayName("wildcard reference")
        void allTypes() {
            assertEmpty("*");
        }

        @Test
        @DisplayName("wildcard package reference")
        void wildcardRef() {
            assertEmpty("*");
            assertEmpty("spine.test.*");
        }

        void assertEmpty(String value) {
            Truth8.assertThat(DirectTypeRef.parse(value))
                  .isEmpty();
        }
    }

    @Test
    @DisplayName("obtain package name from the reference")
    void packageName() {
        String expected = "spine.test.SomeType";
        Optional<TypeRef> ref = DirectTypeRef.parse(expected);
        Truth8.assertThat(ref)
              .isPresent();

        DirectTypeRef direct = ref.map(r -> (DirectTypeRef) r)
                                  .get();
        assertThat(direct.value())
                .isEqualTo(expected);
        Truth8.assertThat(direct.packageName())
              .hasValue(PackageName.of("spine.test"));
    }

    @Nested
    @DisplayName("tell if a message matches the reference")
    class RefMatch {

        @Test
        @DisplayName("for a reference with package")
        void packageRef() {
            TypeRef ref = ref("google.protobuf.Any");
            assertThat(ref.test(Any.getDescriptor()))
                    .isTrue();
        }

        @Test
        @DisplayName("for a simple type reference")
        void simpleRef() {
            TypeRef ref = ref("Timestamp");
            assertThat(ref.test(Timestamp.getDescriptor()))
                    .isTrue();
        }

        @Test
        @DisplayName("rejecting message of a different type")
        void anotherType() {
            TypeRef ref = ref(Int32Value.getDescriptor()
                                        .getFullName());
            assertThat(ref.test(UInt32Value.getDescriptor()))
                    .isFalse();
        }

        @Test
        @DisplayName("rejecting a message with the same simple type, but in another package")
        void anotherPackage() {
            TypeRef ref = ref("spine.test.FloatValue");
            assertThat(ref.test(FloatValue.getDescriptor()))
                    .isFalse();
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent" /* We check via Truth8. */)
    DirectTypeRef ref(String value) {
        Optional<TypeRef> ref = DirectTypeRef.parse(value);
        Truth8.assertThat(ref).isPresent();
        return (DirectTypeRef) ref.get();
    }

    @Test
    @DisplayName("obtain simple file name from a reference")
    void simpleName() {
        Descriptor type = Int32Value.getDescriptor();
        DirectTypeRef ref = ref(type.getFullName());
        assertThat(ref.simpleTypeName())
                .isEqualTo(type.getName());
    }

    @Nested
    @DisplayName("create a new reference with another package")
    class NewRef {

        @Test
        @DisplayName("for a simple name reference without a package")
        void noPackageNotNestedRef() {
            String typeName = "Something";
            DirectTypeRef ref = ref(typeName);
            assertThat(ref.hasPackage()).isFalse();

            PackageName newPackage = PackageName.of("somewhere.else");
            DirectTypeRef relocated = ref.withPackage(newPackage);

            Truth8.assertThat(relocated.packageName())
                    .hasValue(newPackage);
            assertThat(relocated.nestedTypeName())
                    .isEqualTo(typeName);
            assertThat(relocated.simpleTypeName())
                    .isEqualTo(typeName);
        }

        @Test
        @DisplayName("for a nested type without a package")
        void noPackageNested() {
            String typeName = "Something.Nested";
            DirectTypeRef ref = ref(typeName);
            assertThat(ref.hasPackage()).isFalse();

            PackageName newPackage = PackageName.of("we.moved");
            DirectTypeRef relocated = ref.withPackage(newPackage);

            Truth8.assertThat(relocated.packageName())
                  .hasValue(newPackage);
            assertThat(relocated.nestedTypeName())
                    .isEqualTo(typeName);
        }
    }

    @Test
    @DisplayName("not change its package if already has one")
    void notChangePackage() {
        String expected = "spine.test.SomeType";
        Optional<TypeRef> ref = DirectTypeRef.parse(expected);
        Truth8.assertThat(ref)
              .isPresent();

        DirectTypeRef direct = ref.map(r -> (DirectTypeRef) r)
                                  .get();
        PackageName newPackageName = PackageName.of("new.package");

        DirectTypeRef newRef = direct.withPackage(newPackageName);
        assertThat(newRef.value())
                .isEqualTo(expected);
        Truth8.assertThat(newRef.packageName())
              .hasValue(PackageName.of("spine.test"));
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        reserializeAndAssert(ref("google.protobuf.Any"));
        reserializeAndAssert(ref("Any"));
    }
}
