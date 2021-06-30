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

package io.spine.tools.mc.java.codegen;

import io.spine.tools.protoc.Validation;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

/**
 * Configuration for validation code generation.
 */
public final class ValidationConfig extends Config<Validation> {

    private final Property<Boolean> skipValidatingBuilders;
    private final Property<Boolean> skipValidation;

    ValidationConfig(Project p) {
        super();
        skipValidatingBuilders = p.getObjects().property(Boolean.class);
        skipValidation = p.getObjects().property(Boolean.class);
    }

    void enableAllByConvention() {
        skipValidatingBuilders.convention(false);
        skipValidation.convention(false);
    }

    /**
     * Makes the code generation skip generating the validation code.
     */
    public void skipValidation() {
        skipValidation.set(true);
    }

    /**
     * Ensures that validation code will be generated.
     *
     * <p>This is the default behaviour.
     */
    public void generateValidation() {
        skipValidation.set(false);
    }

    /**
     * Makes the code generation skip generating the validating builders.
     */
    public void skipBuilders() {
        skipValidatingBuilders.set(true);
    }

    /**
     * Ensures that validating builders will be generated.
     *
     * <p>This is the default behaviour.
     */
    public void generateBuilders() {
        skipValidatingBuilders.set(false);
    }

    @Override
    Validation toProto() {
        return Validation.newBuilder()
                .setSkipBuilders(skipValidatingBuilders.get())
                .setSkipValidation(skipValidation.get())
                .build();
    }
}
