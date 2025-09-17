/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.annotation

/**
 * This class shows using the [Generated] annotation with single line in Kotlin.
 */
@Suppress("EmptyClassBlock", "unused")
@Generated("Single line")
private class GeneratedWithSingleLine

/**
 * This class shows using the [Generated] annotation with
 * multi-line [Generated.value] argument in Kotlin.
 */
@Suppress("EmptyClassBlock", "unused")
@Generated("Line 1", "Line 2", "Line 3", timestamp = "12:45", comments = "Some comments")
private class GeneratedMultipleLines

/**
 * This class shows using the [Generated] annotation with
 * multi-line [Generated.value] argument in Kotlin when
 * the [value][Generated.value] parameter is named.
 */
@Suppress("EmptyClassBlock", "unused")
@Generated(value = ["Line 1", "Line 2", "Line 3"], timestamp = "12:45", comments = "Some comments")
private class GeneratedMultipleLinesInArray
