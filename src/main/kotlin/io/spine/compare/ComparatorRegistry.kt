/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.compare

import java.util.*
import org.jetbrains.annotations.VisibleForTesting

/**
 * The registry of comparators to their types.
 */
public object ComparatorRegistry {

    private val map = mutableMapOf<Class<*>, Comparator<*>>()

    init {
        applyProviders()
    }

    // `public` for Java users, Kotlin user should use reified counterparts.
    public fun <T> register(clazz: Class<T>, comparator: Comparator<T>) {
        map[clazz] = comparator
    }

    @Suppress("UNCHECKED_CAST") // Type safety is enforced by `register()` method.
    public fun <T> get(clazz: Class<T>): Comparator<T> {
        check(contains(clazz))
        return map[clazz]!! as Comparator<T>
    }

    @Suppress("UNCHECKED_CAST") // Type safety is enforced by `register()` method.
    public fun <T> find(clazz: Class<T>): Comparator<T>? = map[clazz] as Comparator<T>?

    public fun contains(clazz: Class<*>): Boolean = map.containsKey(clazz)

    private fun applyProviders() {
        ServiceLoader.load(ComparatorProvider::class.java)
            .forEach { it.provideIn(this) }
    }
}

public inline fun <reified T : Any> ComparatorRegistry.contains(): Boolean = contains(T::class.java)

public inline fun <reified T : Any> ComparatorRegistry.find(): Comparator<T>? = find(T::class.java)

public inline fun <reified T : Any> ComparatorRegistry.get(): Comparator<T> = get(T::class.java)

public inline fun <reified T> ComparatorRegistry.register(comparator: Comparator<T>): Unit =
    register(T::class.java, comparator)
