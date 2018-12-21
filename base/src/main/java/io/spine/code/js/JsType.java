/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.js;

import io.spine.code.proto.Type;
import io.spine.type.TypeUrl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Javascript type generated from a Protobuf type.
 */
public final class JsType {

    /**
     * The Protobuf type from which the type was generated.
     */
    private final Type type;

    private JsType(Type type) {
        this.type = checkNotNull(type);
    }

    public static JsType generatedFrom(Type type) {
        return new JsType(type);
    }

    /**
     * Obtains the name of the type in the generated code.
     */
    public TypeName name() {
        return TypeName.from(type.descriptor());
    }

    /**
     * Obtains the URL of the type.
     */
    public TypeUrl url() {
        return type.url();
    }

    /**
     * Obtains the reference to a static method of the type.
     *
     * @param methodName
     *         the methodName of the method
     */
    public String staticMethod(String methodName) {
        return name().value() + '.' + methodName;
    }

    /**
     * Obtains the reference to an instance method of the type.
     *
     * @param methodName
     *         the methodName of the method
     */
    public String instanceMethod(String methodName) {
        return prototype() + '.' + methodName;
    }

    /**
     * Obtains the reference to the prototype.
     */
    public String prototype() {
        return name() + ".prototype";
    }
}
