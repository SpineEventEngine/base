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

import com.google.common.truth.IterableSubject
import com.google.common.truth.OptionalSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.StringSubject.CaseInsensitiveStringComparison
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.common.truth.Truth8
import com.google.common.truth.extensions.proto.FieldScope
import com.google.common.truth.extensions.proto.ProtoFluentAssertion
import com.google.common.truth.extensions.proto.ProtoSubject
import com.google.common.truth.extensions.proto.ProtoTruth
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.Message
import com.google.protobuf.TypeRegistry
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

/**
 * Allows to write:
 * ```kotlin
 * assertThat(msg) {
 *     comparingExpectedFieldsOnly() {
 *         isEqualTo(...)
 *     }
 * }
 * ```
 */
public fun <T : Message> assertThat(m: T, assertions: ProtoSubject.() -> Unit) {
    ProtoTruth.assertThat(m).run { assertions() }
}

/**
 * Allows to write:
 * ```kotlin
 * assertThat(object).isInstanceOf<Any>()
 * ```
 */
public inline fun <reified T: Any> Subject.isInstanceOf(): Unit =
    isInstanceOf(T::class.java)

/**
 * @see [ProtoSubject.ignoringFieldAbsence]
 */
public fun ProtoSubject.ignoringFieldAbsence(assertions: ProtoFluentAssertion.() -> Unit) {
    assertions(ignoringFieldAbsence())
}

/**
 * @see [ProtoSubject.comparingExpectedFieldsOnly]
 */
public fun ProtoSubject.comparingExpectedFieldsOnly(assertions: ProtoFluentAssertion.() -> Unit) {
    assertions(comparingExpectedFieldsOnly())
}

/**
 * @see [ProtoSubject.ignoringFieldAbsenceOfFields]
 */
public fun ProtoSubject.ignoringFieldAbsenceOfFields(
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldAbsenceOfFields(firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.ignoringFieldAbsenceOfFields]
 */
public fun ProtoSubject.ignoringFieldAbsenceOfFields(
    fieldNumbers: Iterable<Int>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldAbsenceOfFields(fieldNumbers))
}

/**
 * @see [ProtoSubject.ignoringFieldAbsenceOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringFieldAbsenceOfFieldDescriptors(
    firstFieldDescriptor: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldAbsenceOfFieldDescriptors(firstFieldDescriptor, *rest))
}

/**
 * @see [ProtoSubject.ignoringFieldAbsenceOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringFieldAbsenceOfFieldDescriptors(
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldAbsenceOfFieldDescriptors(fieldDescriptors))
}

/**
 * @see [ProtoSubject.ignoringRepeatedFieldOrder]
 */
public fun ProtoSubject.ignoringRepeatedFieldOrder(assertions: ProtoFluentAssertion.() -> Unit) {
    assertions(ignoringRepeatedFieldOrder())
}

/**
 * @see [ProtoSubject.ignoringRepeatedFieldOrderOfFields]
 */
public fun ProtoSubject.ignoringRepeatedFieldOrderOfFields(
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringRepeatedFieldOrderOfFields(firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.ignoringRepeatedFieldOrderOfFields]
 */
public fun ProtoSubject.ignoringRepeatedFieldOrderOfFields(
    fieldNumbers: Iterable<Int>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringRepeatedFieldOrderOfFields(fieldNumbers))
}

/**
 * @see [ProtoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors(
    firstFieldDescriptor: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringRepeatedFieldOrderOfFieldDescriptors(firstFieldDescriptor, *rest))
}

/**
 * @see [ProtoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringRepeatedFieldOrderOfFieldDescriptors(
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringRepeatedFieldOrderOfFieldDescriptors(fieldDescriptors))
}

/**
 * @see [ProtoSubject.ignoringExtraRepeatedFieldElements]
 */
public fun ProtoSubject.ignoringExtraRepeatedFieldElements(
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringExtraRepeatedFieldElements())
}

/**
 * @see [ProtoSubject.ignoringExtraRepeatedFieldElementsOfFields]
 */
public fun ProtoSubject.ignoringExtraRepeatedFieldElementsOfFields(
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringExtraRepeatedFieldElementsOfFields(firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.ignoringExtraRepeatedFieldElementsOfFields]
 */
public fun ProtoSubject.ignoringExtraRepeatedFieldElementsOfFields(
    fieldNumbers: Iterable<Int>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringExtraRepeatedFieldElementsOfFields(fieldNumbers))
}

/**
 * @see [ProtoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
    first: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringExtraRepeatedFieldElementsOfFieldDescriptors(first, *rest))
}

/**
 * @see [ProtoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors]
 */
public fun ProtoSubject.ignoringExtraRepeatedFieldElementsOfFieldDescriptors(
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringExtraRepeatedFieldElementsOfFieldDescriptors(fieldDescriptors))
}

/**
 * @see [ProtoSubject.usingDoubleTolerance]
 */
public fun ProtoSubject.usingDoubleTolerance(
    tolerance: Double,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingDoubleTolerance(tolerance))
}

/**
 * @see [ProtoSubject.usingDoubleToleranceForFields]
 */
public fun ProtoSubject.usingDoubleToleranceForFields(
    tolerance: Double,
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingDoubleToleranceForFields(tolerance, firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.usingDoubleToleranceForFields]
 */
public fun ProtoSubject.usingDoubleToleranceForFields(
    tolerance: Double,
    fieldNumbers: Iterable<Int>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingDoubleToleranceForFields(tolerance, fieldNumbers))
}

/**
 * @see [ProtoSubject.usingDoubleToleranceForFieldDescriptors]
 */
public fun ProtoSubject.usingDoubleToleranceForFieldDescriptors(
    tolerance: Double,
    firstFieldDescriptor: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingDoubleToleranceForFieldDescriptors(tolerance, firstFieldDescriptor, *rest))
}

/**
 * @see [ProtoSubject.usingDoubleToleranceForFieldDescriptors]
 */
public fun ProtoSubject.usingDoubleToleranceForFieldDescriptors(
    tolerance: Double,
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingDoubleToleranceForFieldDescriptors(tolerance, fieldDescriptors))
}

/**
 * @see [ProtoSubject.usingFloatTolerance]
 */
public fun ProtoSubject.usingFloatTolerance(
    tolerance: Float,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingFloatTolerance(tolerance))
}

/**
 * @see ProtoSubject.usingFloatToleranceForFields
 */
public fun ProtoSubject.usingFloatToleranceForFields(
    tolerance: Float,
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingFloatToleranceForFields(tolerance, firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.usingFloatToleranceForFields]
 */
public fun ProtoSubject.usingFloatToleranceForFields(
    tolerance: Float, fieldNumbers: Iterable<Int>, assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingFloatToleranceForFields(tolerance, fieldNumbers))
}

/**
 * @see [ProtoSubject.usingFloatToleranceForFieldDescriptors]
 */
public fun ProtoSubject.usingFloatToleranceForFieldDescriptors(
    tolerance: Float,
    firstFieldDescriptor: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingFloatToleranceForFieldDescriptors(tolerance, firstFieldDescriptor, *rest))
}

/**
 * @see [ProtoSubject.usingFloatToleranceForFieldDescriptors]
 */
public fun ProtoSubject.usingFloatToleranceForFieldDescriptors(
    tolerance: Float,
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(usingFloatToleranceForFieldDescriptors(tolerance, fieldDescriptors))
}

/**
 * @see [ProtoSubject.withPartialScope]
 */
public fun ProtoSubject.withPartialScope(
    fieldScope: FieldScope,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(withPartialScope(fieldScope))
}

/**
 * @see [ProtoSubject.ignoringFields]
 */
public fun ProtoSubject.ignoringFields(
    firstFieldNumber: Int,
    vararg rest: Int,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFields(firstFieldNumber, *rest))
}

/**
 * @see [ProtoSubject.ignoringFields]
 */
public fun ProtoSubject.ignoringFields(
    fieldNumbers: Iterable<Int>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFields(fieldNumbers))
}

/**
 * @see [ProtoSubject.ignoringFieldDescriptors]
 */
public fun ProtoSubject.ignoringFieldDescriptors(
    firstFieldDescriptor: FieldDescriptor,
    vararg rest: FieldDescriptor,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldDescriptors(firstFieldDescriptor, *rest))
}

/**
 * @see [ProtoSubject.ignoringFieldDescriptors]
 */
public fun ProtoSubject.ignoringFieldDescriptors(
    fieldDescriptors: Iterable<FieldDescriptor>,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldDescriptors(fieldDescriptors))
}

/**
 * @see [ProtoSubject.ignoringFieldScope]
 */
public fun ProtoSubject.ignoringFieldScope(
    fieldScope: FieldScope,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(ignoringFieldScope(fieldScope))
}

/**
 * @see [ProtoSubject.reportingMismatchesOnly]
 */
public fun ProtoSubject.reportingMismatchesOnly(assertions: ProtoFluentAssertion.() -> Unit) {
    assertions(reportingMismatchesOnly())
}

/**
 * @see [ProtoSubject.unpackingAnyUsing]
 */
public fun ProtoSubject.unpackingAnyUsing(
    typeRegistry: TypeRegistry,
    extensionRegistry: ExtensionRegistry,
    assertions: ProtoFluentAssertion.() -> Unit
) {
    assertions(unpackingAnyUsing(typeRegistry, extensionRegistry))
}
