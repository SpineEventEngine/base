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

@file:JvmName("FieldDescriptorExtensions")

package io.spine.code.proto

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE
import com.google.protobuf.Descriptors.FieldDescriptor

/**
 * Checks the Protobuf field and determines it is repeated field or not.
 *
 * Although `map` fields technically count as `repeated`, this method will
 * return `false` for them.
 */
fun FieldDescriptorProto.isRepeated(): Boolean =
    !isMap() && label == LABEL_REPEATED

/**
 * Checks the Protobuf field and determines it is repeated field or not.
 *
 * Although `map` fields technically [count][FieldDescriptor.isRepeated] as `repeated`,
 * this method will return `false` for them.
 */
fun FieldDescriptor.isRepeatedIndeed(): Boolean =
    toProto().isRepeated()

/**
 * Checks the Protobuf field and determines it is map field or not.
 *
 * If a field is a map it is repeated message with the specific type.
 */
fun FieldDescriptorProto.isMap(): Boolean {
    if (label != LABEL_REPEATED) {
        return false
    }
    if (type != TYPE_MESSAGE) {
        return false
    }
    return typeName.endsWith('.'.toString() + entryName())
}

/**
 * Checks the Protobuf field and determines it is map field or not.
 *
 * If a field is a map it is repeated message with the specific type.
 */
fun FieldDescriptor.isMap(): Boolean =
    toProto().isMap()

/**
 * Constructs the entry name for the map field.
 *
 * For example, proto field with name 'word_dictionary' has 'wordDictionary' json name.
 * Every map field has corresponding entry type.
 * For 'word_dictionary' it would be 'WordDictionaryEntry'
 */
fun FieldDescriptorProto.entryName(): String {
    val fieldName = FieldName.of(this)
    return fieldName.toCamelCase() + "Entry"
}

/**
 * Constructs the entry name for the map field.
 *
 * For example, proto field with name 'word_dictionary' has 'wordDictionary' json name.
 * Every map field has corresponding entry type.
 * For 'word_dictionary' it would be 'WordDictionaryEntry'
 */
fun FieldDescriptor.entryName(): String =
    toProto().entryName()

/**
 * Checks the Protobuf field and determines it is message type or not.
 */
fun FieldDescriptorProto.isMessage(): Boolean =
    type == TYPE_MESSAGE

/**
 * Checks the Protobuf field and determines it is message type or not.
 */
fun FieldDescriptor.isMessage(): Boolean =
    toProto().isMessage()

/**
 * Obtains the key descriptor for the `map` field.
 *
 * The receiving type is [FieldDescriptor] because the map key is the
 * field of the `...Entry` `message` type.
 *
 * @return the key descriptor for the specified map field
 * @throws IllegalStateException
 *         if the specified field is not a map field
 */
fun FieldDescriptor.keyDescriptor(): FieldDescriptor {
    check(isMap()) {
        "Unable to get a key descriptor for the non-map field `$name`."
    }
    return messageType.findFieldByName("key")
}

/**
 * Obtains the value descriptor for the `map` field.
 *
 * The receiving type is [FieldDescriptor] because the map key is the
 * field of the `...Entry` `message` type.
 *
 * @return the value descriptor for the specified map field
 * @throws IllegalStateException
 *         if the specified field is not a map field
 */
fun FieldDescriptor.valueDescriptor(): FieldDescriptor {
    check(isMap()) {
        "Unable to get a value descriptor for the non-map field `$name`."
    }
    return messageType.findFieldByName("value")
}
