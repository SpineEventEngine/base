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

package io.spine.tools.protoc.method;

import io.spine.code.proto.MessageType;
import io.spine.tools.protoc.GeneratedMethod;
import io.spine.tools.protoc.GeneratedMethodsConfig;
import io.spine.tools.protoc.TypeFilter;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Scans the given type for a match upon patterns defined in {@link GeneratedMethodsConfig}.
 */
class PatternScanner extends TypeScanner {

    PatternScanner(GeneratedMethodsConfig config) {
        super(config);
    }

    @Override
    Predicate<GeneratedMethod> concreteFilter(MessageType type) {
        return new MatchesPattern(type);
    }

    private static class MatchesPattern implements Predicate<GeneratedMethod> {

        private final String sourceFilePath;

        private MatchesPattern(MessageType type) {
            checkNotNull(type);
            this.sourceFilePath = type.sourceFile()
                                      .getPath()
                                      .toString();
        }

        @Override
        public boolean test(GeneratedMethod method) {
            checkNotNull(method);
            TypeFilter filter = method.getFilter();
            if (filter.getValueCase() != TypeFilter.ValueCase.FILE_POSTFIX) {
                return false;
            }
            return sourceFilePath.contains(filter.getFilePostfix());
        }
    }
}
