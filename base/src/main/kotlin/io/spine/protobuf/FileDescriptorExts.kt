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

@file:JvmName("FileDescriptors")

package io.spine.protobuf

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import io.spine.code.java.ClassName

/**
 * A [binary][ClassName.binaryName] name of the outer Java class associated with this file.
 */
public val FileDescriptor.outerClassName: String
    get() {
        val outerClass = ClassName.outerClass(this)
        return outerClass.binaryName()
    }

/**
 * An outer Java class associated with this proto file, if such a class already exists.
 * Otherwise, returns `null`.
 */
public val FileDescriptor.outerClass: Class<*>?
    get() {
        return try {
            val classLoader = javaClass.classLoader
            val outerClass = classLoader.loadClass(outerClassName)
            outerClass
        } catch (ignored: ClassNotFoundException) {
            null
        }
    }

/**
 * Reflectively calls the static `registerAllExtensions(..)` method, such as
 * [io.spine.option.OptionsProto.registerAllExtensions], on the [outerClass] generated for
 * this Protobuf file with the custom options declared.
 *
 * @throws IllegalStateException
 *          if the outer class for this proto file does not exist
 */
public fun FileDescriptor.registerAllExtensions(registry: ExtensionRegistry) {
    check(outerClass != null) {
        "The outer class `$outerClassName` for the file `$name` does not exist."
    }
    val method = outerClass!!.getDeclaredMethod(
        "registerAllExtensions", ExtensionRegistry::class.java
    )
    method.invoke(null, registry)
}
