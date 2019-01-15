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

import com.google.common.truth.IterableSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Timestamp;
import io.spine.net.Uri;
import io.spine.net.Url;
import io.spine.option.EntityOption;
import io.spine.option.GoesOption;
import io.spine.option.MinOption;
import io.spine.test.code.proto.rejections.TestRejections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MessageType should")
class MessageTypeTest {

    /**
     * Negates the passed predicate.
     *
     * @apiNote Provided for brevity of tests, avoiding avoiding using {@code Predicates.not()}
     * from Guava, util similar method is provided by Java 11.
     */
    static <T> Predicate<T> not(Predicate<T> yes) {
        return yes.negate();
    }

    @Nested
    @DisplayName("Tell if message is")
    class Tell {

        /**
         * Tests a certain boolean method of {@code MessageType} created on the passed descriptor.
         */
        void assertQuality(Predicate<MessageType> method, Descriptor descriptor) {
            MessageType type = MessageType.of(descriptor);
            boolean result = method.test(type);
            assertTrue(result);
        }

        @DisplayName("nested")
        @Test
        void nested() {
            assertQuality(MessageType::isNested, Uri.Protocol.getDescriptor());
            assertQuality(not(MessageType::isNested), Url.getDescriptor());
        }

        @DisplayName("top-level")
        @Test
        void topLevel() {
            assertQuality(MessageType::isTopLevel, Url.getDescriptor());
            assertQuality(not(MessageType::isTopLevel), Uri.Protocol.getDescriptor());
        }

        @DisplayName("a rejection")
        @Test
        void rejection() {
            assertQuality(MessageType::isRejection,
                          TestRejections.MttSampleRejection.getDescriptor()
            );
        }

        @Nested
        @DisplayName("a non-Google or a Spine options type")
        class Custom {

            @Test
            @DisplayName("positively for a custom type")
            void custom() {
                assertQuality(MessageType::isCustom, Url.getDescriptor());
            }

            @Test
            @DisplayName("negatively for Google type")
            void google() {
                assertNotCustom(Timestamp.getDescriptor());
            }

            @Test
            @DisplayName("negatively for Spine options type")
            void options() {
                assertNotCustom(GoesOption.getDescriptor());
                assertNotCustom(EntityOption.getDescriptor());
                assertNotCustom(MinOption.getDescriptor());
            }

            void assertNotCustom(Descriptor descriptor) {
                assertQuality(not(MessageType::isCustom), descriptor);
            }
        }
    }

    @Nested
    @DisplayName("Obtain path for")
    class Path {

        @Test
        @DisplayName("top-level message")
        void topLevel() {
            assertPath(Url.getDescriptor());
        }

        @CanIgnoreReturnValue
        private IterableSubject assertPath(Descriptor descriptor) {
            MessageType type = MessageType.of(descriptor);
            LocationPath path = type.path();

            IterableSubject assertPath = assertThat(path.toList());
            assertPath.contains(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER);
            assertPath.contains(descriptor.getIndex());

            return assertPath;
        }

        @Test
        @DisplayName("second-level message")
        void secondLevel() {
            IterableSubject assertPath = assertPath(Uri.Protocol.getDescriptor());
            assertPath.contains(Uri.getDescriptor().getIndex());
        }
    }
}
