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

package io.spine.tools.protoc.method.vbuilder;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.tools.protoc.method.MethodFactory;
import io.spine.type.MessageType;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link MethodFactory} used by default to create new static helper methods for all messages.
 *
 * <p>Creates {@code public static final VBuilder vBuilder()} method.
 */
@Immutable
public class VBuilderMethodFactory implements MethodFactory {

    public VBuilderMethodFactory() {
    }

    @Override
    public List<GeneratedMethod> createFor(MessageType messageType) {
        checkNotNull(messageType);
        if (!messageType.hasVBuilder()) {
            return ImmutableList.of();
        }
        SimpleClassName vBuilderClass = messageType.validatingBuilderClass();
        PackageName packageName = messageType.javaPackage();
        ClassName vBuilder = ClassName.get(packageName.value(), vBuilderClass.value());
        return ImmutableList.of(newVBuilderSpec(vBuilder), newToVBuilderSpec(vBuilder));
    }

    /**
     * Creates new {@code public static final T vBuilder()} method where {@code <T>} is a
     * {@link io.spine.validate.ValidatingBuilder ValidatingBuilder}:
     * <pre>
     *     {@code
     *     public static final T vBuilder(){
     *         return T.newBuilder();
     *     }
     *     }
     * </pre>
     */
    private static GeneratedMethod newVBuilderSpec(ClassName vBuilder) {
        MethodSpec spec = MethodSpec
                .methodBuilder("vBuilder")
                .returns(vBuilder)
                .addStatement("return $T.newBuilder()", vBuilder)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("Creates a new instance of a {@link $T}.\n", vBuilder)
                .build();
        return new GeneratedMethod(spec.toString());
    }

    /**
     * Creates new {@code public final T toVBuilder()} method where {@code <T>} is a
     * {@link io.spine.validate.ValidatingBuilder ValidatingBuilder} created from the current
     * instance state:
     * <pre>
     *     {@code
     *     public final T toVBuilder(){
     *         T result = T.newBuilder();
     *         result.setOriginalState(this);
     *         return result;
     *     }
     *     }
     * </pre>
     */
    private static GeneratedMethod newToVBuilderSpec(ClassName vBuilder) {
        MethodSpec spec = MethodSpec
                .methodBuilder("toVBuilder")
                .returns(vBuilder)
                .addStatement("$1T result = $1T.newBuilder()", vBuilder)
                .addStatement("result.setOriginalState(this)")
                .addStatement("return result")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Creates a new instance of a {@link $T} with the current state.\n",
                            vBuilder)
                .build();
        return new GeneratedMethod(spec.toString());
    }
}
