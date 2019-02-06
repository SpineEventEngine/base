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

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.Truth8;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Timestamp;
import io.spine.test.code.proto.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FieldReference should")
class FieldRefTest {

    private ImmutableList<FieldRef> references;

    /**
     * Creates field reference form test environment message defined in
     * {@code test/proto/spine/test/code/proto/field_reference_test.proto}.
     */
    @BeforeEach
    void setUp() {
        FieldDescriptorProto personNameField = UserInfo.getDescriptor()
                                                       .toProto()
                                                       .getField(0);
        references = FieldRef.allFrom(personNameField);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private FieldRef ref(int index) {
        return references.get(index);
    }

    @Test
    void performNullCheck() {
        new NullPointerTester().testAllPublicStaticMethods(FieldRef.class);
    }

    @Nested
    @DisplayName("recognize")
    class Recognize {

        @Test
        @DisplayName("internal reference")
        void internalRef() {
            assertPositive(new FieldRef("another_field")::isInner);
        }

        @Test
        @DisplayName("context reference")
        void contextRef() {
            assertPositive(new FieldRef("context.timestamp")::isContext);
        }

        void assertPositive(Supplier<Boolean> quality) {
            assertQualify(quality).isTrue();
        }

        private BooleanSubject assertQualify(Supplier<Boolean> quality) {
            return assertThat(quality.get());
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
            assertThat(ref(2).isContext()).isTrue();
        }
    }

    @Nested
    @DisplayName("reject")
    class Arguments {

        @DisplayName("empty or blank value")
        @Test
        void emptyOrBlank() {
            assertRejects("");

            assertRejects("  ");
        }

        @DisplayName("value with missing parent field")
        @Test
        void emptyTypeRef() {
            assertRejects(".field_name");
        }

        @DisplayName("value with missing interim reference")
        @Test
        void emptyInterimTypeRef() {
            assertRejects("some. .field_name");
        }

        void assertRejects(Executable executable) {
            assertThrows(IllegalArgumentException.class, executable);
        }

        void assertRejects(String fieldReference) {
            assertRejects(() -> new FieldRef(fieldReference));
        }
    }

    @Nested
    @DisplayName("obtain field descriptor from a message descriptor")
    class FindFieldDescriptor {

        private final FieldRef nonQualifiedTypedRef =
                new FieldRef("Timestamp.seconds");

        private final FieldRef onlyNameRef =
                new FieldRef("seconds");

        private final FieldRef invalidTypedRef =
                new FieldRef("LocalTime.seconds");

        @Test
        @DisplayName("via typed reference")
        void typedRef() {
            assertFound(nonQualifiedTypedRef);
        }

        @Test
        @DisplayName("via name-only reference")
        void nameOnlyRef() {
            assertFound(onlyNameRef);
        }

        @Test
        @DisplayName("rejecting a message which type name does not match")
        void rejectWrongType() {
            assertThrows(IllegalArgumentException.class,
                         () -> invalidTypedRef.find(Timestamp.getDescriptor()));
        }

        private void assertFound(FieldRef ref) {
            Optional<Descriptors.FieldDescriptor> fd = ref.find(Timestamp.getDescriptor());
            Truth8.assertThat(fd).isPresent();
        }
    }

    @Nested
    @DisplayName("tell if a type matches")
    class TypeMatch {

        @Test
        @DisplayName("for direct type reference")
        void directTypeRef() {
            assertThat(new FieldRef("Timestamp.seconds")
                               .matchesType(Timestamp.getDescriptor()))
                    .isTrue();
        }
    }

    @Test
    @DisplayName("serialize")
    void serialize() {
        reserializeAndAssert(new FieldRef("google.protobuf.Timestamp.seconds"));
    }
}
