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

package io.spine.tools.compiler.rejection;

import com.squareup.javapoet.ClassName;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.RejectionDeclaration;

/**
 * A declaration of a generated rejection.
 *
 * <p>The class is purposed for the {@code JavaPoet}-based code generation.
 */
class GeneratedRejectionDeclaration {

    private final RejectionDeclaration protoDeclaration;

    GeneratedRejectionDeclaration(RejectionDeclaration protoDeclaration) {
        this.protoDeclaration = protoDeclaration;
    }

    /**
     * Obtains the class name of the {@linkplain io.spine.base.ThrowableMessage rejection},
     * that will be generated.
     *
     * @return the rejection class name
     */
    ClassName throwableRejection() {
        PackageName javaPackage = protoDeclaration.getJavaPackage();
        return ClassName.get(javaPackage.value(), simpleTypeName());
    }

    /**
     * Obtains the fully qualified name for the
     * {@link io.spine.base.RejectionMessage RejectionMessage}.
     *
     * @return the FQN name for the rejection message
     */
    ClassName rejectionMessage() {
        PackageName javaPackage = protoDeclaration.getJavaPackage();
        SimpleClassName outerClass = protoDeclaration.getOuterJavaClass();
        ClassName outerClassFqn = ClassName.get(javaPackage.value(),
                                                outerClass.value());
        return outerClassFqn.nestedClass(protoDeclaration.getSimpleJavaClassName()
                                                         .value());
    }

    String simpleTypeName() {
        return protoDeclaration.getSimpleTypeName();
    }

    RejectionDeclaration protoDeclaration() {
        return protoDeclaration;
    }
}
