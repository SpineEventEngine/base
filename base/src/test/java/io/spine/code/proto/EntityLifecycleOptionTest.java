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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Message;
import io.spine.code.proto.ref.TypeRef;
import io.spine.option.OptionsProto;
import io.spine.test.code.proto.PmWithDirectOptions;
import io.spine.test.code.proto.PmWithEmptyOptions;
import io.spine.test.code.proto.PmWithOption;
import io.spine.test.code.proto.PmWithoutOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EntityLifecycleOption should")
class EntityLifecycleOptionTest {

    private static final TypeRef PM_ARCHIVED =
            TypeRef.parse("spine.test.code.proto.PmArchived");
    private static final TypeRef PM_DELETED =
            TypeRef.parse("spine.test.code.proto.PmDeleted");

    private EntityLifecycleOption option;

    @BeforeEach
    void setUp() {
        option = new EntityLifecycleOption();
    }

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void passNullToleranceCheck() {
        new NullPointerTester().testAllPublicInstanceMethods(option);
    }

    @Test
    @DisplayName("have `lifecycle` extension")
    void haveLifecycleExtension() {
        assertThat(option.extension())
                .isEqualTo(OptionsProto.lifecycle);
    }

    @Test
    @DisplayName("check if message type has lifecycle option specified")
    void checkIfHasLifecycle() {
        MessageType pmWithOption = typeOf(PmWithOption.class);
        assertThat(option.hasLifecycle(pmWithOption)).isTrue();

        MessageType pmWithoutOption = typeOf(PmWithoutOption.class);
        assertThat(option.hasLifecycle(pmWithoutOption)).isFalse();
    }

    @Test
    @DisplayName("retrieve type reference from `archive_upon` option")
    void retrieveArchiveUpon() {
        MessageType pmWithOption = typeOf(PmWithOption.class);
        Optional<TypeRef> ref = option.archiveUpon(pmWithOption);
        assertTrue(ref.isPresent());
        assertThat(ref.get()).isEqualTo(PM_ARCHIVED);
    }

    @Nested
    @DisplayName("retrieve empty Optional for `archive_upon`")
    class RetrieveEmptyArchiveUpon {

        @Test
        @DisplayName("if the option is not specified")
        void ifNotSpecified() {
            MessageType pmWithEmpty = typeOf(PmWithoutOption.class);
            Optional<TypeRef> ref = option.archiveUpon(pmWithEmpty);
            assertThat(ref.isPresent()).isFalse();
        }

        @Test
        @DisplayName("if the option contains empty string")
        void ifContainsEmptyString() {
            MessageType pmWithEmpty = typeOf(PmWithEmptyOptions.class);
            Optional<TypeRef> ref = option.archiveUpon(pmWithEmpty);
            assertThat(ref.isPresent()).isFalse();
        }
    }

    @Test
    @DisplayName("retrieve type reference from `delete_upon` option")
    void retrieveDeleteUpon() {
        MessageType pmWithOption = typeOf(PmWithOption.class);
        Optional<TypeRef> ref = option.deleteUpon(pmWithOption);
        assertTrue(ref.isPresent());
        assertThat(ref.get()).isEqualTo(PM_DELETED);
    }

    @Nested
    @DisplayName("retrieve empty Optional for `delete_upon`")
    class RetrieveEmptyDeleteUpon {

        @Test
        @DisplayName("if the option is not specified")
        void ifNotSpecified() {
            MessageType pmWithEmpty = typeOf(PmWithoutOption.class);
            Optional<TypeRef> ref = option.deleteUpon(pmWithEmpty);
            assertThat(ref.isPresent()).isFalse();
        }

        @Test
        @DisplayName("if the option contains empty string")
        void ifContainsEmptyString() {
            MessageType pmWithEmpty = typeOf(PmWithEmptyOptions.class);
            Optional<TypeRef> ref = option.deleteUpon(pmWithEmpty);
            assertThat(ref.isPresent()).isFalse();
        }
    }

    @Test
    @DisplayName("append package name to directly referenced events")
    void appendPackageName() {
        MessageType pmWithOption = typeOf(PmWithDirectOptions.class);
        Optional<TypeRef> archiveUpon = option.archiveUpon(pmWithOption);
        assertTrue(archiveUpon.isPresent());
        assertThat(archiveUpon.get()).isEqualTo(PM_ARCHIVED);

        Optional<TypeRef> deleteUpon = option.deleteUpon(pmWithOption);
        assertTrue(deleteUpon.isPresent());
        assertThat(deleteUpon.get()).isEqualTo(PM_DELETED);
    }

    private static MessageType typeOf(Class<? extends Message> messageClass) {
        return new MessageType(messageClass);
    }
}
