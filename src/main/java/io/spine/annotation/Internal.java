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

package io.spine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a program element (class, method, package, etc.) as internal to the Spine Event Engine,
 * indicating it is NOT part of the public API and hence not intended for use outside
 * the module's development team.
 *
 * <p>The usage scenarios for the annotation vary depending on the element it is applied to.
 *
 * <h3>If applied to a constructor, field, method, or package</h3>
 *
 * <p>The annotated program element is not intended for usage by the framework users
 *
 * <h3>If applied to a type</h3>
 *
 * <p>The annotation applied to a type could mean either:
 * <ul>
 *     <li><strong>Option 1:</strong> The type is internal to the Spine Engine framework,
 *         hence should not be directly used by the framework users.
 *
 *     <li><strong>Option 2:</strong> The type is specific to a bounded context in which it may be
 *         exposed externally because of technical or historical reasons.
 *         Attempting to use the type for inbound or outbound communication will
 *         trigger runtime errors.
 * </ul>
 *
 * <p>See also {@link SPI} for annotations related to framework extensions.
 *
 * @apiNote Implementing an extension to wire into the framework might tempt you
 *          to use these internal parts.
 *          However, be aware that they might have less stability than public APIs.
 *          Consultation with the Spine development team is strongly recommended in these cases.
 * @see SPI
 */
@Internal
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.TYPE})
@Documented
public @interface Internal {
}
