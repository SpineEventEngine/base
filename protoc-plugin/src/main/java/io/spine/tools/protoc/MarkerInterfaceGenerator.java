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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import java.io.File;
import java.io.IOException;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @author Dmytro Dashenkov
 */
public final class MarkerInterfaceGenerator {

    private static final AnnotationSpec GENERATED;
    private static final String GENERATED_FIELD_NAME = "value";

    static {
        final CodeBlock generatedByDescription = CodeBlock.of("\"by Spine protoc plugin\"");
        GENERATED = AnnotationSpec.builder(Generated.class)
                                  .addMember(GENERATED_FIELD_NAME, generatedByDescription)
                                  .build();
    }

    private final File outputFile;

    public MarkerInterfaceGenerator(File outputFile) {
        this.outputFile = outputFile;
    }

    public void generate(String packageName, String typeName) {
        final TypeSpec spec = TypeSpec.interfaceBuilder(typeName)
                                      .addSuperinterface(Message.class)
                                      .addModifiers(PUBLIC)
                                      .addAnnotation(GENERATED)
                                      .build();
        try {
            JavaFile.builder(packageName, spec)
                    .build()
                    .writeTo(outputFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
