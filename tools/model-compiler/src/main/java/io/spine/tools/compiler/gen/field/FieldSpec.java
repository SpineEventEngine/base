/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.tools.compiler.gen.field;

import com.squareup.javapoet.TypeSpec;
import io.spine.base.SubscribableField;
import io.spine.code.java.PackageName;
import io.spine.tools.compiler.gen.GeneratedTypeSpec;
import io.spine.type.MessageType;

import static io.spine.tools.compiler.annotation.Annotations.generatedBySpineModelCompiler;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class FieldSpec implements GeneratedTypeSpec {

    private static final String NAME_FORMAT = "%sField";

    private final MessageType messageType;

    public FieldSpec(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public PackageName packageName() {
        return messageType.javaPackage();
    }

    @Override
    public TypeSpec typeSpec() {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(className())
                        .addAnnotation(generatedBySpineModelCompiler())
                        .addModifiers(PUBLIC, FINAL)
                        .addSuperinterface(SubscribableField.class);
        return builder.build();
    }

    private String className() {
        String result = format(NAME_FORMAT, messageType.simpleJavaClassName());
        return result;
    }
}
