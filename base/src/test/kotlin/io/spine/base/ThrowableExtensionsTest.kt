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

package io.spine.base

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.Parser
import com.google.protobuf.UnknownFieldSet
import io.spine.base.RejectionMessage
import io.spine.base.RejectionThrowable
import java.io.OutputStream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Extensions for `Throwable` should")
internal class ThrowableExtensionsTest {

    @Nested
    @DisplayName("tell if it was caused by a rejection")
    inner class CausedBy {

        private var throwable: Throwable = RuntimeException()

        @BeforeEach
        fun createThrowable() {
            throwable = RuntimeException()
        }

        @Test
        fun `if true`() {
            throwable.initCause(StubRejectionThrowable())

            assertThat(throwable.causedByRejection()).isTrue()
        }

        @Test
        fun `if false`() {
            throwable.initCause(RuntimeException())

            assertThat(throwable.causedByRejection()).isFalse()
        }
    }

    /**
     * Stub type of [RejectionThrowable] which serves only as a type.
     */
    private class StubRejectionThrowable: RejectionThrowable(StubRejectionMessage())

    /**
     * Stub type of [RejectionMessage] which serves only as a type.
     */
    private class StubRejectionMessage: RejectionMessage {
        override fun getDefaultInstanceForType(): Message {
            notImplemented()
        }

        override fun isInitialized(): Boolean {
            notImplemented()
        }

        override fun writeTo(output: CodedOutputStream?) {
            notImplemented()
        }

        override fun writeTo(output: OutputStream?) {
            notImplemented()
        }

        override fun getSerializedSize(): Int {
            notImplemented()
        }

        override fun getParserForType(): Parser<out Message> {
            notImplemented()
        }

        override fun toByteString(): ByteString {
            notImplemented()
        }

        override fun toByteArray(): ByteArray {
            notImplemented()
        }

        override fun writeDelimitedTo(output: OutputStream?) {
            notImplemented()
        }

        override fun newBuilderForType(): Message.Builder {
            notImplemented()
        }

        override fun toBuilder(): Message.Builder {
            notImplemented()
        }

        override fun findInitializationErrors(): MutableList<String> {
            notImplemented()
        }

        override fun getInitializationErrorString(): String {
            notImplemented()
        }

        override fun getDescriptorForType(): Descriptors.Descriptor {
            notImplemented()
        }

        override fun getAllFields(): MutableMap<Descriptors.FieldDescriptor, Any> {
            notImplemented()
        }

        override fun hasOneof(oneof: Descriptors.OneofDescriptor?): Boolean {
            notImplemented()
        }

        override fun getOneofFieldDescriptor(oneof: Descriptors.OneofDescriptor?): Descriptors.FieldDescriptor {
            notImplemented()
        }

        override fun hasField(field: Descriptors.FieldDescriptor?): Boolean {
            notImplemented()
        }

        override fun getField(field: Descriptors.FieldDescriptor?): Any {
            notImplemented()
        }

        override fun getRepeatedFieldCount(field: Descriptors.FieldDescriptor?): Int {
            notImplemented()
        }

        override fun getRepeatedField(field: Descriptors.FieldDescriptor?, index: Int): Any {
            notImplemented()
        }

        override fun getUnknownFields(): UnknownFieldSet {
            notImplemented()
        }

        private fun notImplemented(): Nothing {
            TODO("Stub type")
        }
    }
}
