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

package io.spine.tools.gradle.compiler.protoc;

import io.spine.code.java.ClassName;
import io.spine.tools.protoc.GeneratedInterface;
import io.spine.tools.protoc.TypeFilters;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.checker.signature.qual.FullyQualifiedName;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class InterfacePostfixPattern extends PostfixPattern<GeneratedInterface> {

    private @Nullable ClassName interfaceName;

    InterfacePostfixPattern(@Regex String postfix) {
        super(postfix);
    }

    /**
     * Sets current target class to a supplied value.
     */
    public void markWith(@FullyQualifiedName String targetName) {
        checkNotNull(targetName);
        this.interfaceName = ClassName.of(targetName);
    }

    @Override
    public GeneratedInterface toProto() {
        return GeneratedInterface
                .newBuilder()
                .setFilter(TypeFilters.filePostfix(getPattern()))
                .setInterfaceName(interfaceName()
                                          .map(ClassName::value)
                                          .orElse(""))
                .build();
    }

    private Optional<ClassName> interfaceName() {
        return Optional.ofNullable(interfaceName);
    }
}
