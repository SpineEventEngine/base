/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.tools.mc.java.gradle;

import io.spine.annotation.Beta;
import io.spine.annotation.Experimental;
import io.spine.annotation.Internal;
import io.spine.annotation.SPI;
import io.spine.code.java.ClassName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Type names of Java annotations used to mark generated code.
 */
@SuppressWarnings({"PublicField", "WeakerAccess"}) // Expose fields as a part of Gradle extension.
public class CodeGenAnnotations {

    public String experimental = Experimental.class.getCanonicalName();
    public String beta = Beta.class.getCanonicalName();
    public String spi = SPI.class.getCanonicalName();
    public String internal = Internal.class.getCanonicalName();

    public ClassName experimentalClassName() {
        checkNotNull(experimental);
        return ClassName.of(experimental);
    }

    public ClassName betaClassName() {
        checkNotNull(beta);
        return ClassName.of(beta);
    }

    public ClassName spiClassName() {
        checkNotNull(spi);
        return ClassName.of(spi);
    }

    public ClassName internalClassName() {
        checkNotNull(internal);
        return ClassName.of(internal);
    }
}
