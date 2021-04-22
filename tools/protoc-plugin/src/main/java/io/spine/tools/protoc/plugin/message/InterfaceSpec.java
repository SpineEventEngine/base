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

package io.spine.tools.protoc.plugin.message;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.spine.tools.java.gen.GeneratedBy;
import io.spine.option.IsOption;
import io.spine.tools.java.fs.SourceFile;
import io.spine.type.MessageType;

import java.util.Objects;

import static io.spine.code.java.PackageName.delimiter;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * The specification of the message interface to be produces by the generator.
 *
 * <p>The specification includes the package name and the type name.
 */
final class InterfaceSpec {

    private final String packageName;
    private final String name;

    @VisibleForTesting
    InterfaceSpec(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    static InterfaceSpec createFor(MessageType declaringType, IsOption isOption) {
        String javaType = isOption.getJavaType();
        InterfaceSpec spec;
        if (javaType.contains(delimiter())) {
            spec = from(javaType);
        } else {
            String javaPackage = declaringType.javaPackage()
                                              .value();
            spec = new InterfaceSpec(javaPackage, javaType);
        }
        return spec;
    }

    /**
     * Parses a {@code MessageInterfaceSpec} from the given type fully qualified name.
     */
    private static InterfaceSpec from(String fullName) {
        int index = fullName.lastIndexOf(delimiter());
        String name = fullName.substring(index + 1);
        String packageName = fullName.substring(0, index);
        return new InterfaceSpec(packageName, name);
    }

    /**
     * Converts the instance to {@link JavaFile}.
     */
    JavaFile toJavaCode() {
        TypeSpec spec = TypeSpec
                .interfaceBuilder(name())
                .addSuperinterface(Message.class)
                .addModifiers(PUBLIC)
                .addAnnotation(GeneratedBy.spineModelCompiler())
                .build();
        JavaFile javaFile = JavaFile
                .builder(packageName, spec)
                .build();
        return javaFile;
    }

    SourceFile toSourceFile() {
        SourceFile result = SourceFile.forType(packageName, name);
        return result;
    }

    private String name() {
        return name;
    }

    String fullName() {
        return packageName + delimiter() + name;
    }

    @Override
    public String toString() {
        return fullName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InterfaceSpec other = (InterfaceSpec) obj;
        return Objects.equals(this.packageName, other.packageName)
                && Objects.equals(this.name, other.name);
    }
}
