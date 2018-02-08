/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.protoc;

import com.google.protobuf.Message;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.Annotation.generatedAnnotation;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A factory for {@link Message} derived marker interfaces extracted from {@code .proto} files.
 *
 * <p>Each interface is marked with {@link Generated @Generated} annotation. No more additional
 * declarations are made.
 *
 * @author Dmytro Dashenkov
 */
final class MarkerInterfaces {

    /** Prevent utility class instantiation. */
    private MarkerInterfaces() {
    }

    /**
     * Generates a marker interface with the given name and package.
     *
     * @param packageName the name of the package of the required interface
     * @param typeName    the name of the required interface
     * @return {@link JavaFile} instance representing the desired interface
     */
    static JavaFile create(String packageName, String typeName) {
        checkNotNull(packageName);
        checkNotNull(typeName);
        final TypeSpec spec = TypeSpec.interfaceBuilder(typeName)
                                      .addSuperinterface(Message.class)
                                      .addModifiers(PUBLIC)
                                      .addAnnotation(generatedAnnotation())
                                      .build();
        final JavaFile javaFile = JavaFile.builder(packageName, spec)
                                          .build();
        return javaFile;
    }
}
