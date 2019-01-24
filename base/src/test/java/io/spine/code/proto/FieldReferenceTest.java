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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.test.code.proto.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FieldReference should")
class FieldReferenceTest {

    private ImmutableList<FieldReference> references;

    /**
     * Creates field reference form test environment message defined in
     * {@code test/proto/spine/test/code/proto/field_reference_test.proto}.
     */
    @BeforeEach
    void setUp() {
        FieldDescriptorProto personNameField = UserInfo.getDescriptor()
                                                       .toProto()
                                                       .getField(0);
        references = FieldReference.allFrom(personNameField);
    }

    private FieldReference ref(int index) {
        return references.get(index);
    }

    @Test
    void performNullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(FieldReference.class);
    }

    @Test
    @DisplayName("tell if a string represents all types")
    void isWildcardUtility() {
        assertThat(FieldReference.isWildcard("*"))
                .isTrue();
    }

    @Nested
    @DisplayName("recognize")
    class Recognize {

        @Test
        @DisplayName("wildcard reference")
        void wildcardRef() {
            assertPositive(new FieldReference("*.user_id")::isWildcard);
            assertNegative(new FieldReference("UserCreated.user_id")::isWildcard);
        }

        @Test
        @DisplayName("internal reference")
        void internalRef() {
            assertPositive(new FieldReference("another_field")::isInner);
            assertNegative(new FieldReference("AnotherMessage.another_field")::isInner);
        }

        @Test
        @DisplayName("context reference")
        void contextRef() {
            assertPositive(new FieldReference("context.timestamp")::isContext);
            assertNegative(new FieldReference("AnotherMessage.context")::isContext);
        }

        void assertPositive(Supplier<Boolean> quality) {
            assertThat(quality.get()).isTrue();
        }

        void assertNegative(Supplier<Boolean> quality) {
            assertThat(quality.get()).isFalse();
        }
    }

    @Test
    @DisplayName("obtain type name")
    void typeName() {
        assertThat(new FieldReference("ReferencedType.some_field").typeName())
                .isEqualTo("ReferencedType");
    }

    @Nested
    @DisplayName("obtain type reference")
    class TypeRef {

        @Test
        @DisplayName("as wildcard")
        void wildcardType() {
            assertThat(ref(0).typeName()).isEqualTo("*");
        }

        @Test
        @DisplayName("as type name")
        void typeName() {
            assertThat(ref(1).typeName()).isEqualTo("DocumentUpdated");
        }
    }

    @Nested
    @DisplayName("obtain instances from a field descriptor")
    class RefsFromDescriptor {

        /** Tests that the number of alternatives matches those specified in the `by` option. */
        @Test
        @DisplayName("getting all alternatives")
        void alternatives() {
            assertThat(references).hasSize(3);
        }

        /** Tests that alternatives are correctly initialized. */
        @Test
        @DisplayName("constructing references of appropriate types")
        void wildcard() {
            assertThat(ref(0).isWildcard()).isTrue();
            assertThat(ref(1).typeName()).isEqualTo("DocumentUpdated");
            assertThat(ref(2).isContext()).isTrue();
        }
    }

    @Nested
    @DisplayName("reject")
    class Arguments {

        @DisplayName("empty or blank type reference")
        @Test
        void emptyTypeArg() {
            assertThrows(IllegalArgumentException.class, () -> FieldReference.isWildcard(""));
            assertThrows(IllegalArgumentException.class, () -> FieldReference.isWildcard(" "));
        }

        /**
         * Tests that a wildcard field reference cannot be given in a suffix form such as
         * {@code (by) = "*Event.user_id"}. Currently only single symbol {@code '*'} is allowed for
         * wildcard field references.
         */
        @DisplayName("suffix form of whilecard type reference")
        @Test
        void suffixForm() {
            assertThrows(IllegalArgumentException.class, () -> FieldReference.isWildcard("*Event"));
        }
    }
}
