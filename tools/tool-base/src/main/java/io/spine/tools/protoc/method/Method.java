/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.method;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.Immutable;
import com.squareup.javapoet.MethodSpec;
import io.spine.value.StringTypeValue;

/**
 * A generated Java method source code.
 *
 * <p>SPI users are responsible for checking that the content of the method is properly formatted
 * and contains all the required modifiers, comments, and Javadoc.
 *
 * <p>The actual compilation of the generated method is performed as a part of the compilation
 * of other Protobuf-generated sources.
 */
@Immutable
public final class Method extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * Creates a new instance with the passed code block.
     */
    @VisibleForTesting
    public Method(String code) {
        super(code);
    }

    /**
     * Cretes an instance with the code of the method obtained from the passed spec.
     */
    public Method(MethodSpec spec) {
        this(spec.toString());
    }
}
