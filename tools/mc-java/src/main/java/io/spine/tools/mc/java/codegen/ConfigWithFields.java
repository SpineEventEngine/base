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

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.spine.tools.protoc.GenerateFields;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

abstract class ConfigWithFields<P extends Message> extends ConfigWithInterfaces<P> {

    private final Property<Boolean> generateFields;
    private final Property<String> markFieldsAs;

    ConfigWithFields(Project p) {
        super(p);
        generateFields = p.getObjects().property(Boolean.class);
        markFieldsAs = p.getObjects().property(String.class);
    }

    final void convention(boolean generateFields, @Nullable Class<?> fieldSuperclass) {
        this.generateFields.convention(generateFields);
        if (fieldSuperclass != null) {
            markFieldsAs.convention(fieldSuperclass.getCanonicalName());
        }
    }

    public final void setGenerateFields(boolean shouldGenerate) {
        generateFields.set(shouldGenerate);
    }

    public final void markFieldsAs(String className) {
        markFieldsAs.set(className);
    }

    final GenerateFields generateFields() {
        GenerateFields generateFields;
        if (!this.generateFields.get()) {
            generateFields = GenerateFields.newBuilder()
                    .setSkip(Empty.getDefaultInstance())
                    .build();
        } else if (markFieldsAs.isPresent()) {
            String superclass = markFieldsAs.get();
            generateFields = GenerateFields.newBuilder()
                    .setGenerateWithSuperclass(className(superclass))
                    .build();
        } else {
            generateFields = GenerateFields.newBuilder()
                    .setGenerate(Empty.getDefaultInstance())
                    .build();
        }
        return generateFields;
    }
}
