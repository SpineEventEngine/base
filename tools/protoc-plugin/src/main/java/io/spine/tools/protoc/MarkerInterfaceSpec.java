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

package io.spine.tools.protoc;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Message;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.spine.code.java.PackageName;
import io.spine.code.java.SourceFile;

import java.util.Objects;

import static io.spine.code.java.Annotations.generatedBySpineModelCompiler;
import static io.spine.code.java.PackageName.DELIMITER;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * The specification of the marker interface to be produces by the generator.
 *
 * <p>The specification includes the package name and the type name.
 *
 * @author Dmytro Dashenkov
 */
final class MarkerInterfaceSpec {

    private final String packageName;
    private final String name;

    @VisibleForTesting
    MarkerInterfaceSpec(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    static MarkerInterfaceSpec prepareInterface(String optionValue,
                                                FileDescriptorProto srcFile) {
        final MarkerInterfaceSpec spec;
        if (optionValue.contains(DELIMITER)) {
            spec = from(optionValue);
        } else {
            final String javaPackage = PackageName.resolve(srcFile)
                                                  .value();
            spec = new MarkerInterfaceSpec(javaPackage, optionValue);
        }
        return spec;
    }

    /**
     * Parses a {@code MarkerInterfaceSpec} from the given type fully qualified name.
     */
    private static MarkerInterfaceSpec from(String fullName) {
        final int index = fullName.lastIndexOf(DELIMITER);
        final String name = fullName.substring(index + 1);
        final String packageName = fullName.substring(0, index);
        return new MarkerInterfaceSpec(packageName, name);
    }

    /**
     * Converts the instance to {@link JavaFile}.
     */
    JavaFile toJavaCode() {
        final TypeSpec spec = TypeSpec.interfaceBuilder(getName())
                                      .addSuperinterface(Message.class)
                                      .addModifiers(PUBLIC)
                                      .addAnnotation(generatedBySpineModelCompiler())
                                      .build();
        final JavaFile javaFile = JavaFile.builder(packageName, spec)
                                          .build();
        return javaFile;
    }

    SourceFile toSourceFile() {
        final SourceFile result = SourceFile.forType(packageName, name);
        return result;
    }

    String getName() {
        return name;
    }

    String getFqn() {
        return packageName + DELIMITER + name;
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
        final MarkerInterfaceSpec other = (MarkerInterfaceSpec) obj;
        return Objects.equals(this.packageName, other.packageName)
                && Objects.equals(this.name, other.name);
    }
}
