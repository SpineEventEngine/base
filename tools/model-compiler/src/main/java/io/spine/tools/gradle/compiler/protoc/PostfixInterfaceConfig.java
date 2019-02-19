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

import com.google.common.base.MoreObjects;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.GeneratedInterface;

import java.util.Objects;

/**
 * A {@link GeneratedInterfaceConfig} which configures message types defined in a Proto file with
 * a certain naming.
 */
final class PostfixInterfaceConfig extends PatternInterfaceConfig {

    private final PostfixPattern postfix;

    PostfixInterfaceConfig(PostfixPattern postfix) {
        super();
        this.postfix = postfix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostfixInterfaceConfig)) {
            return false;
        }
        PostfixInterfaceConfig config = (PostfixInterfaceConfig) o;
        return Objects.equals(postfix, config.postfix)
                && Objects.equals(interfaceClass(), config.interfaceClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(postfix, interfaceClass());
    }

    @Override
    public String toString() {
        //noinspection DuplicateStringLiteralInspection
        return MoreObjects.toStringHelper(this)
                          .add("postfix", postfix)
                          .add("interfaceName", interfaceClass())
                          .toString();
    }

    @Override
    GeneratedInterface generatedInterface() {
        return GeneratedInterface
                .newBuilder()
                .setFilePostfix(postfix.getPattern())
                .setInterfaceName(interfaceName()
                                          .map(ClassName::value)
                                          .orElse(""))
                .build();
    }
}
