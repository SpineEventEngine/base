/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.Descriptors
import com.google.protobuf.ExtensionRegistryLite
import com.google.protobuf.Message
import com.google.protobuf.MessageLite
import com.google.protobuf.Parser
import com.google.protobuf.UnknownFieldSet
import io.spine.base.SomeMsg.SomeBuilder
import io.spine.protobuf.ValidatingBuilder
import java.io.InputStream
import java.io.OutputStream

@Suppress("FINAL_UPPER_BOUND")
//TODO:2020-12-27:alexander.yevsyukov: Update code
// generation to create `events.kt` classes for each package with generated events.
inline fun <reified E: Error> event(block: Error.Builder.() -> Unit): E {
    val builder = ValidatableMessage.builderOf(E::class.java)
    block.invoke(builder)
    return builder.vBuild() as E
}

inline fun <B: ValidatingBuilder<M>, M: Message> B.with(block: B.() -> Unit): M {
    block.invoke(this)
    return vBuild()
}

private fun test() {
    val err = event<Error> {
        type = ""
        message = ""
    }

    val error = Error.newBuilder().with {
        type = ""
        message = ""
    }
}


private class SomeMsg: ValidatableMessage<SomeBuilder, SomeMsg> {

    class SomeBuilder: ValidatingBuilder<SomeMsg> {
        override fun getDefaultInstanceForType(): Message {
            TODO("Not yet implemented")
        }

        override fun isInitialized(): Boolean {
            TODO("Not yet implemented")
        }

        override fun clone(): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun clear(): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun build(): SomeMsg {
            TODO("Not yet implemented")
        }

        override fun buildPartial(): SomeMsg {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(other: Message?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(input: CodedInputStream?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(
            input: CodedInputStream?,
            extensionRegistry: ExtensionRegistryLite?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(data: ByteString?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(
            data: ByteString?,
            extensionRegistry: ExtensionRegistryLite?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(data: ByteArray?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(data: ByteArray?, off: Int, len: Int): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(
            data: ByteArray?,
            extensionRegistry: ExtensionRegistryLite?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(
            data: ByteArray?,
            off: Int,
            len: Int,
            extensionRegistry: ExtensionRegistryLite?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(input: InputStream?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(
            input: InputStream?,
            extensionRegistry: ExtensionRegistryLite?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeFrom(other: MessageLite?): MessageLite.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeDelimitedFrom(input: InputStream?): Boolean {
            TODO("Not yet implemented")
        }

        override fun mergeDelimitedFrom(
            input: InputStream?,
            extensionRegistry: ExtensionRegistryLite?
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun findInitializationErrors(): MutableList<String> {
            TODO("Not yet implemented")
        }

        override fun getInitializationErrorString(): String {
            TODO("Not yet implemented")
        }

        override fun getDescriptorForType(): Descriptors.Descriptor {
            TODO("Not yet implemented")
        }

        override fun getAllFields(): MutableMap<Descriptors.FieldDescriptor, Any> {
            TODO("Not yet implemented")
        }

        override fun hasOneof(oneof: Descriptors.OneofDescriptor?): Boolean {
            TODO("Not yet implemented")
        }

        override fun getOneofFieldDescriptor(oneof: Descriptors.OneofDescriptor?): Descriptors.FieldDescriptor {
            TODO("Not yet implemented")
        }

        override fun hasField(field: Descriptors.FieldDescriptor?): Boolean {
            TODO("Not yet implemented")
        }

        override fun getField(field: Descriptors.FieldDescriptor?): Any {
            TODO("Not yet implemented")
        }

        override fun getRepeatedFieldCount(field: Descriptors.FieldDescriptor?): Int {
            TODO("Not yet implemented")
        }

        override fun getRepeatedField(field: Descriptors.FieldDescriptor?, index: Int): Any {
            TODO("Not yet implemented")
        }

        override fun getUnknownFields(): UnknownFieldSet {
            TODO("Not yet implemented")
        }

        override fun newBuilderForField(field: Descriptors.FieldDescriptor?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun getFieldBuilder(field: Descriptors.FieldDescriptor?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun getRepeatedFieldBuilder(
            field: Descriptors.FieldDescriptor?,
            index: Int
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun setField(field: Descriptors.FieldDescriptor?, value: Any?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun clearField(field: Descriptors.FieldDescriptor?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun clearOneof(oneof: Descriptors.OneofDescriptor?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun setRepeatedField(
            field: Descriptors.FieldDescriptor?,
            index: Int,
            value: Any?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun addRepeatedField(
            field: Descriptors.FieldDescriptor?,
            value: Any?
        ): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun setUnknownFields(unknownFields: UnknownFieldSet?): Message.Builder {
            TODO("Not yet implemented")
        }

        override fun mergeUnknownFields(unknownFields: UnknownFieldSet?): Message.Builder {
            TODO("Not yet implemented")
        }

    }

    override fun getDefaultInstanceForType(): Message {
        TODO("Not yet implemented")
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

    override fun writeTo(output: CodedOutputStream?) {
        TODO("Not yet implemented")
    }

    override fun writeTo(output: OutputStream?) {
        TODO("Not yet implemented")
    }

    override fun getSerializedSize(): Int {
        TODO("Not yet implemented")
    }

    override fun getParserForType(): Parser<out Message> {
        TODO("Not yet implemented")
    }

    override fun toByteString(): ByteString {
        TODO("Not yet implemented")
    }

    override fun toByteArray(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun writeDelimitedTo(output: OutputStream?) {
        TODO("Not yet implemented")
    }

    override fun newBuilderForType(): Message.Builder {
        TODO("Not yet implemented")
    }

    override fun toBuilder(): Message.Builder {
        TODO("Not yet implemented")
    }

    override fun findInitializationErrors(): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun getInitializationErrorString(): String {
        TODO("Not yet implemented")
    }

    override fun getDescriptorForType(): Descriptors.Descriptor {
        TODO("Not yet implemented")
    }

    override fun getAllFields(): MutableMap<Descriptors.FieldDescriptor, Any> {
        TODO("Not yet implemented")
    }

    override fun hasOneof(oneof: Descriptors.OneofDescriptor?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getOneofFieldDescriptor(oneof: Descriptors.OneofDescriptor?): Descriptors.FieldDescriptor {
        TODO("Not yet implemented")
    }

    override fun hasField(field: Descriptors.FieldDescriptor?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getField(field: Descriptors.FieldDescriptor?): Any {
        TODO("Not yet implemented")
    }

    override fun getRepeatedFieldCount(field: Descriptors.FieldDescriptor?): Int {
        TODO("Not yet implemented")
    }

    override fun getRepeatedField(field: Descriptors.FieldDescriptor?, index: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getUnknownFields(): UnknownFieldSet {
        TODO("Not yet implemented")
    }
}
