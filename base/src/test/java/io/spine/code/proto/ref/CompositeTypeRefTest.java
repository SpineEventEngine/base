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

import com.google.common.testing.EqualsTester;
import com.google.common.truth.StringSubject;
import com.google.protobuf.Any;
import io.spine.base.FieldFilter;
import io.spine.base.FieldPath;
import io.spine.code.proto.PackageName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompositeTypeRef should")
class CompositeTypeRefTest {

    @Nested
    @DisplayName("not allow")
    class Arguments {

        @Test
        @DisplayName("only one type reference in the value")
        void twoOrMoreRefs() {
            assertRejects("single.type.reference.*");
        }

        @Test
        @DisplayName("wildcard type reference")
        void wildcardRef() {
            assertRejects("MyType,my.packageref.*,*");
        }

        @Test
        @DisplayName("empty reference")
        void emptyRef() {
            assertRejects("");
            assertRejects("SomeType,,package.ref.*");
            assertRejects(",moref.*,AnotherType");
            assertRejects("first.*,mytype.Second,");
        }

        void assertRejects(String value) {
            assertThrows(IllegalArgumentException.class,
                         () -> CompositeTypeRef.doParse(value));
        }
    }

    @Test
    @DisplayName("recognize message type")
    void recognizeType() {
        CompositeTypeRef ref = CompositeTypeRef.doParse(
                "google.protobuf.*," +
                "spine.base.FieldFilter"
        );

        assertThat(ref.test(Any.getDescriptor()))
                .isTrue();
        assertThat(ref.test(FieldFilter.getDescriptor()))
                .isTrue();
    }

    @Test
    @DisplayName("filter out non-matching message types")
    void filterOut() {
        CompositeTypeRef ref = CompositeTypeRef.doParse(
                "google.protobuf.StringValue," +
                "google.protobuf.Timestamp," +
                "spine.base.FieldFilter"
        );

        assertThat(ref.test(Any.getDescriptor()))
                .isFalse();
        assertThat(ref.test(FieldPath.getDescriptor()))
                .isFalse();
    }

    @Test
    @DisplayName("provide comma-separated value")
    void valueWithCommas() {
        CompositeTypeRef ref = CompositeTypeRef.doParse("Some,Value,reference.*");
        assertThat(ref.value())
                .contains(",");
    }

    @Test
    @DisplayName("have toString() with the value enclosed in brackets")
    void stringOut() {
        String expected = "google.protobuf.*,spine.base.*,AndSomethingElse";
        CompositeTypeRef ref = CompositeTypeRef.doParse(expected);
        StringSubject assertValue = assertThat(ref.toString());
        assertValue.contains(expected);
        assertValue.startsWith("[");
        assertValue.endsWith("]");
    }

    @Test
    @DisplayName("add package qualifier to its elements")
    void addPackageQualifier() {
        String packageRef = "google.protobuf.*";
        String directRef = "SomeDirectType";
        String initial = packageRef + CompositeTypeRef.SEPARATOR + directRef;
        CompositeTypeRef ref = CompositeTypeRef.doParse(initial);
        String packageName = "io.spine.some.package";
        String expectedRef = packageName + PackageName.delimiter() + directRef;
        String expected = '[' + packageRef + CompositeTypeRef.SEPARATOR + expectedRef + ']';

        TypeRef newRef = ref.withPackage(PackageName.of(packageName));
        assertThat(newRef.toString())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("support equality by its content")
    void equality() {
        new EqualsTester()
                .addEqualityGroup(
                        CompositeTypeRef.doParse("google.protobuf.*,spine.base.FieldFilter"),
                        CompositeTypeRef.doParse("google.protobuf.*,spine.base.FieldFilter"))
                .addEqualityGroup(
                        CompositeTypeRef.doParse("google.protobuf.StringValue,google.protobuf.Timestamp"),
                        CompositeTypeRef.doParse("google.protobuf.StringValue,google.protobuf.Timestamp"))
                .testEquals();
    }
}
