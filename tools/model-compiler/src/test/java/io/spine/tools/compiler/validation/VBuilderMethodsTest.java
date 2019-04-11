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

package io.spine.tools.compiler.validation;

import com.google.protobuf.Descriptors;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.spine.code.generate.java.OneofDeclaration;
import io.spine.code.java.ClassName;
import io.spine.test.tools.validation.builder.VbtProject;
import io.spine.type.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("VBuilderMethods should generate")
class VBuilderMethodsTest {

    private static final MessageType messageType = new MessageType(VbtProject.getDescriptor());

    private List<MethodSpec> methods;

    @BeforeEach
    void setUp() {
        methods = VBuilderMethods.methodsOf(messageType);
    }

    @Test
    @DisplayName("`newBuilder` method")
    void newBuilder() {
        MethodSpec newBuilder = methodWithName("newBuilder");
        assertThat(newBuilder.modifiers).containsAllOf(PUBLIC, STATIC);
        assertThat(newBuilder.returnType.toString())
                .endsWith(messageType.validatingBuilderClass()
                                     .value());
        assertThat(newBuilder.parameters).isEmpty();
    }

    @Test
    @DisplayName("getter methods for attributes")
    void getters() {
        MethodSpec getDescription = methodWithName("getDescription");
        assertThat(getDescription.returnType).isEqualTo(TypeName.get(String.class));
        assertThat(getDescription.modifiers).containsExactly(PUBLIC);
        assertThat(getDescription.parameters).isEmpty();
    }

    @Test
    @DisplayName("setter methods for attributes")
    void setters() {
        MethodSpec getDescription = methodWithName("setDescription");
        assertThat(getDescription.returnType.toString())
                .endsWith(messageType.validatingBuilderClass()
                                     .value());
        assertThat(getDescription.modifiers).containsExactly(PUBLIC);
        assertThat(getDescription.parameters).hasSize(1);
        assertThat(getDescription.parameters.get(0).type).isEqualTo(TypeName.get(String.class));
    }

    @Test
    @DisplayName("accessor methods for oneof case enums")
    void caseGetters() {
        MethodSpec getDescription = methodWithName("getOwnerCase");
        Descriptors.OneofDescriptor oneofDescriptor = messageType
                .descriptor()
                .getOneofs()
                .get(0);
        OneofDeclaration declaration = new OneofDeclaration(oneofDescriptor, messageType);
        ClassName expectedReturnType = declaration.javaCaseEnum();
        assertThat(getDescription.returnType.toString())
                .isEqualTo(expectedReturnType.toString());
        assertThat(getDescription.modifiers).containsExactly(PUBLIC);
        assertThat(getDescription.parameters).isEmpty();
    }

    private MethodSpec methodWithName(String name) {
        return methods.stream()
                      .filter(method -> method.name.equals(name))
                      .findAny()
                      .orElseGet(() -> fail(format("Method with name %s does not exist.", name)));
    }
}
