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
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an interface which is implemented in the generated code adding desired behavior
 * using default methods declared in the interface.
 *
 * <h3>Motivation</h3>
 *
 * <p>This annotation allows to document the intent of the interface. It also allows to instruct
 * IDEs to consider annotated interfaces as implemented before the code generation phase, or
 * if the interfaces are used only from projects that depend on the one declaring these interfaces.
 *
 * <p>For example, Spine Base project introduces
 * the {@link io.spine.base.CommandMessage CommandMessage} interface.
 * There are no command messages generated in the Base project because it does not provide any
 * backend API. The interface is used by multiple subprojects of the Spine SDK that depend
 * on Base, but it is not used withing the project. Annotating the interface
 * with {@code GeneratedMixin} addresses the issue.
 *
 * <h3>Creating a mixin interface</h3>
 *
 * <p>In order to generate a class which implements a custom mixin interface:
 * <ol>
 *    <li>Create the interface and mark it with this annotation.
 *    <li>Declare methods of interest accessing properties of the generated types following
 *    the Protobuf convention for the accessor methods.
 *    For example, if a message has a property named {@code foo_bar}, the method to declare will
 *    be {@code getFooBar()}.
 *    <li>Add {@code default} methods. Presumably bodies of these methods will call accessor
 *    methods declared earlier.
 *    <li>Mark corresponding proto messages using the {@code (is).java_type} option (if for
 *    one message), or {@code (every_is).java_type} option for a file, if the interface is common
 *    for all the message declared in this file. These options instruct Spine Model Compiler to
 *    make the generated code implement this interface.
 * </ol>
 *
 * <p>The annotation should <em>NOT</em> be used on interfaces that do not provide default methods
 * because they will not be mixins.
 *
 * @see io.spine.option.IsOption
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface GeneratedMixin {
}
