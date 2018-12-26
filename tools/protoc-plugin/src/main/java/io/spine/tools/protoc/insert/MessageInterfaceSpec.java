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

package io.spine.tools.protoc.insert;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Message;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.java.SourceFile;
import io.spine.option.IsOption;

import javax.annotation.Generated;
import java.util.Objects;

import static io.spine.code.Generation.compilerAnnotation;
import static io.spine.code.java.PackageName.delimiter;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * The specification of the message interface to be produces by the generator.
 *
 * <p>The specification includes the package name and the type name.
 */
final class MessageInterfaceSpec {

    private static final AnnotationSpec BY_MODEL_COMPILER =
            AnnotationSpec.builder(Generated.class)
                          .addMember(compilerAnnotation().getFieldName(),
                                     CodeBlock.of(compilerAnnotation().getCodeBlock()))
                          .build();

    private final String packageName;
    private final String name;

    @VisibleForTesting
    MessageInterfaceSpec(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    static MessageInterfaceSpec prepareInterface(IsOption optionValue,
                                                 FileDescriptorProto srcFile) {
        String javaType = optionValue.getJavaType();
        MessageInterfaceSpec spec;
        if (javaType.contains(delimiter())) {
            spec = from(javaType);
        } else {
            String javaPackage = PackageName.resolve(srcFile)
                                            .value();
            spec = new MessageInterfaceSpec(javaPackage, javaType);
        }
        return spec;
    }

    /**
     * Parses a {@code MessageInterfaceSpec} from the given type fully qualified name.
     */
    private static MessageInterfaceSpec from(String fullName) {
        int index = fullName.lastIndexOf(delimiter());
        String name = fullName.substring(index + 1);
        String packageName = fullName.substring(0, index);
        return new MessageInterfaceSpec(packageName, name);
    }

    /**
     * Converts the instance to {@link JavaFile}.
     */
    JavaFile toJavaCode() {
        TypeSpec spec = TypeSpec
                .interfaceBuilder(getName())
                .addSuperinterface(Message.class)
                .addModifiers(PUBLIC)
                .addAnnotation(BY_MODEL_COMPILER)
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

    String getName() {
        return name;
    }

    String getFqn() {
        return packageName + delimiter() + name;
    }

    @Override
    public String toString() {
        return getFqn();
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
        MessageInterfaceSpec other = (MessageInterfaceSpec) obj;
        return Objects.equals(this.packageName, other.packageName)
                && Objects.equals(this.name, other.name);
    }
}
