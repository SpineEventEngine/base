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

package io.spine.testing

import com.google.common.testing.NullPointerTester
import com.google.common.truth.IterableSubject
import com.google.common.truth.OptionalSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.StringSubject.CaseInsensitiveStringComparison
import com.google.common.truth.Truth
import com.google.common.truth.Truth8
import java.util.*

/**
 * Extension functions for [Google Truth](https://truth.dev/).
 *
 * It is expected that these extensions would be implemented by Truth someday.
 * Until then, we would use those implemented below.
 *
 * @see <a href="https://github.com/google/truth/milestone/12">
 *     "First post-1.0 feature push" milestone at Truth GitHub</a>
 * @see <a href="https://github.com/google/truth/issues/536">
 *     Issue in Truth GitHub</a>
 * @see <a href="https://github.com/google/truth/issues/572">
 *     Another issue</a>
 */
@Suppress("unused") // is used for KDoc on this file.
private const val ABOUT = ""

/**
 * Allows to use generic parameter of the function instead of `MyType::class.java` as the first
 * parameter type.
 */
public inline fun <reified T : Any> NullPointerTester.setDefault(value : T) : NullPointerTester =
    setDefault(T::class.java, value)

/**
 * Allows to write:
 *
 * ```kotlin
 *  assertThat("foo bar") {
 *      contains("foo")
 *      contains("bar")
 *  }
 * ```
 */
public fun assertThat(string: String, assertions: StringSubject.() -> Unit) {
    Truth.assertThat(string).run { assertions() }
}

/**
 * Allows to write:
 *
 * ```kotlin
 * assertThat("foo bar").ignoringCase() {
 *    contains("BAR")
 *    contains("FoO")
 * }
 * ```
 */
public fun StringSubject.ignoringCase(assertions: CaseInsensitiveStringComparison.() -> Unit) {
    assertions(ignoringCase())
}

/**
 * Allows to write:
 *
 * ```kotlin
 * assertThat(listOf(1, 2, 3) {
 *     contains(1)
 *     contains(3)
 *     doesNotContain(4)
 * }
 * ```
 */
public fun <T> assertThat(iterable: Iterable<T>, assertions: IterableSubject.() -> Unit) {
    Truth.assertThat(iterable).run { assertions() }
}

/**
 * Allows to write:
 *
 * ```kotlin
 * assertThat(Optional.of("something") {
 *     hasValue("something")
 * }
 * ```
 */
public fun <T> assertThat(optional: Optional<T>, assertions: OptionalSubject.() -> Unit) {
    Truth8.assertThat(optional).run { assertions() }
}
