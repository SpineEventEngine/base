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
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.TypeFilter;
import io.spine.tools.protoc.TypeFilters;

import java.util.Objects;

final class PostfixMethodConfig extends PatternMethodConfig {

    private final PostfixPattern postfix;

    PostfixMethodConfig(PostfixPattern postfix) {
        super();
        this.postfix = postfix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostfixMethodConfig)) {
            return false;
        }
        PostfixMethodConfig config = (PostfixMethodConfig) o;
        return Objects.equals(postfix, config.postfix)
                && Objects.equals(factoryClass(), config.factoryClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(postfix, factoryClass());
    }

    @Override
    public String toString() {
        //noinspection DuplicateStringLiteralInspection
        return MoreObjects.toStringHelper(this)
                          .add("postfix", postfix)
                          .add("factoryName", factoryClass())
                          .toString();
    }

    @Override
    GeneratedMethod generatedMethod() {
        TypeFilter filter = TypeFilters.filePostfix(postfix.getPattern());
        return GeneratedMethod
                .newBuilder()
                .setFilter(filter)
                .setFactoryName(factoryName()
                                        .map(ClassName::value)
                                        .orElse(""))
                .build();
    }
}
