/*
 * Copyright 2022, TeamDev. All rights reserved.
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

@file:JvmName("UtilExtensions")

package io.spine.protobuf

import com.google.protobuf.FieldMask
import com.google.protobuf.Message
import com.google.protobuf.util.FieldMaskUtil.fromFieldNumbers
import com.google.protobuf.util.FieldMaskUtil.isValid

/**
 * Constructs a [FieldMask] from the passed field numbers.
 *
 * @throws IllegalArgumentException
 *          if any of the fields are invalid for the message.
 */
public inline fun <reified T : Message> fromFieldNumbers(vararg fieldNumbers: Int): FieldMask =
    fromFieldNumbers<T>(fieldNumbers.toList())

/**
 * Constructs a [FieldMask] from the passed field numbers.
 *
 * @throws IllegalArgumentException
 *          if any of the fields are invalid for the message.
 */
public inline fun <reified T : Message> fromFieldNumbers(fieldNumbers: Iterable<Int>): FieldMask =
    fromFieldNumbers(T::class.java, fieldNumbers)

/**
 * Checks whether paths in a given fields mask are valid.
 */
public inline fun <reified T : Message> isValid(fieldMask: FieldMask): Boolean =
    isValid(T::class.java, fieldMask)

/**
 * Checks whether a given field path is valid.
 */
public inline fun <reified T : Message> isValid(path: String): Boolean =
    isValid(T::class.java, path)
