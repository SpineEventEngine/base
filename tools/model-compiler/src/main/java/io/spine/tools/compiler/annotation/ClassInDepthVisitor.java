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

package io.spine.tools.compiler.annotation;

import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

public interface ClassInDepthVisitor extends SourceVisitor<JavaClassSource> {

    default Stream<AbstractJavaSource<JavaClassSource>>
    allClasses(AbstractJavaSource<JavaClassSource> root) {
        Queue<AbstractJavaSource<JavaClassSource>> classesToAdd = new ArrayDeque<>();
        classesToAdd.add(root);
        Stream.Builder<AbstractJavaSource<JavaClassSource>> result = Stream.builder();
        result.accept(root);
        while (!classesToAdd.isEmpty()) {
            AbstractJavaSource<JavaClassSource> currentClass = classesToAdd.poll();
            currentClass.getNestedTypes()
                        .stream()
                        .filter(type -> type instanceof AbstractJavaSource)
                        .forEach(type -> {
                            @SuppressWarnings("unchecked")
                                // Due to inconvenience of Roaster API.
                            AbstractJavaSource<JavaClassSource> abstractJavaSource =
                                    (AbstractJavaSource<JavaClassSource>) type;
                            classesToAdd.add(abstractJavaSource);
                            result.accept(abstractJavaSource);
                        });
        }
        return result.build();
    }
}
