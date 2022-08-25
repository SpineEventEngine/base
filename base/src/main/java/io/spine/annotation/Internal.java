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
 * Annotates a program element (class, method, package, etc.) which is not a part of a public API,
 * and thus should not be used by people who are not members of the team developing the module
 * containing this program element.
 *
 * <p>If the annotation is used for a constructor, a <strong>field</strong>,
 * a <strong>method</strong>, or a <strong>package</strong>, it means
 * that corresponding element is internal to the Spine Event Engine and as such should
 * not be used programmers using the framework.
 *
 * <p>If the annotation is used for a <strong>type</strong> it means one of the following.
 *
 * <p><strong>First reason.</strong> This type is internal to the Spine Event Engine framework,
 * and is not meant to be used directly by the framework users.</p>
 *
 * <p><strong>Second reason.</strong> The type is internal to a bounded context, artifact of which
 * exposes the type to the outside world (presumably for historical reasons).</p>
 *
 * <p>When so, the type with this annotation can be used only inside the bounded context
 * which declares it.
 *
 * <p>The type must not be used neither for inbound (i.e. being sent to the bounded context
 * which declares this type) nor for outbound communication (i.e. being sent by this bounded
 * context outside). An attempt to use the type otherwise will cause runtime error.</p>
 *
 * @apiNote If you plan to implement an extension which is going to be wired into
 * the framework, you may want to use the internal parts. Please consider consulting with the Spine
 * development team, as the internal APIs do not have the same stability guarantee as public ones.
 *
 * <p>See {@link SPI} annotation if you plan to write an extension of the framework.
 *
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
