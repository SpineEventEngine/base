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

package io.spine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The {@code Generated} annotation is used to mark source code that has been generated.
 *
 * <p>When applied to a package or a type, it means the whole package or a type was generated.
 * It can be also applied to an element of Java code to differentiate generated code
 * from handcrafted.
 *
 * <p>The {code value} element must contain the name of the generator.
 * The recommended convention is to use the fully qualified name of
 * the corresponding component, such as a class.
 * Alternatively, consider using Maven coordinates of the "generator".
 *
 * <p>The {@code timestamp}, which is optional, is the time when the code was generated.
 * It should follow the ISO 8601 standard.
 *
 * <p>The {@code comment} element is the place for comments authors of code generators
 * may want to leave about the generated code.
 *
 * <h3>About the name</h3>
 *
 * <p>The name of this annotation type must be {@code Generated} so that development tools
 * exclude the generated code from test coverage.
 *
 * <p>
 * <a href="https://github.com/jacoco/jacoco/issues/685#issuecomment-392020612">JaCoCo analyzes</a>
 * Java classes for the presence of an annotation which simple name is {@code Generated}.
 * It cannot be {@code GrpcGenerated} or {@code SpineGenerated}.
 * It could be any package, but the simple name must be {@code Generated}.
 * Such a class is automatically excluded from the report.
 *
 * @see Modified
 * @since 2.0.0
 */
@Documented
@Retention(CLASS)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, CONSTRUCTOR, METHOD, FIELD, LOCAL_VARIABLE, PARAMETER})
public @interface Generated {

    /**
     * Provides the name and other optional references (e.g., the version) to the code generator.
     */
    String[] value();

    /**
     * Optional date and time when the code was generated.
     */
    String timestamp() default "";

    /**
     * Optional comments that the code generator may want to include.
     */
    String comments() default "";
}
