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

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Marks a program element (class, method, etc.) as internal to a module or a bounded context,
 * indicating it is NOT part of the public API and hence not intended for use outside
 * the module's development team.
 *
 * The usage scenarios for the annotation vary depending on the element it is applied to.
 *
 * ### If applied to a constructor, field, or a method
 *
 * The annotated program element is not intended for usage by the Spine SDK users.
 *
 * ### If applied to a type
 *
 * The annotation applied to a type could mean either:
 *
 * - **Option 1**: The type is internal to the Spine SDK,
 *   hence should not be directly used by the framework users.
 *
 * - **Option 2**: The type is specific to a bounded context in which it may be
 *   exposed externally because of technical or historical reasons.
 *   Attempting to use the type for inbound or outbound communication will
 *   trigger runtime errors.
 *
 * See also [SPI] for annotations related to framework extensions.
 *
 * ### API Note
 *
 * Implementing an extension to wire into the framework might tempt you to use these internal parts.
 * However, be aware that they might have less stability than public APIs.
 * Consultation with the Spine development team is strongly recommended in these cases.
 * 
 * @see SPI
 */
@Retention(RUNTIME)
@Target(
    ANNOTATION_CLASS,
    CLASS,
    CONSTRUCTOR,
    FIELD,
    FILE,
    FUNCTION,
    PROPERTY
)
@MustBeDocumented
public annotation class Internal
