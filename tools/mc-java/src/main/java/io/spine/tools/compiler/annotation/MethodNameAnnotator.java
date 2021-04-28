/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.fs.java.SourceFile;
import io.spine.code.java.ClassName;
import io.spine.code.proto.TypeSet;
import io.spine.type.Type;
import org.jboss.forge.roaster.model.Method;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;

import java.nio.file.Path;

/**
 * An {@link Annotator} which annotates methods matching given patterns.
 *
 * <p>Note that, unlike other annotator implementations, {@code MethodNameAnnotator} works with
 * multiple method patterns rather than just one due to performance considerations. All
 * the generated classes are parsed and checked. This can take some time. Thus, consider
 * the selective approach to marking methods unless a pattern or a set of patterns is completely
 * necessary.
 */
final class MethodNameAnnotator extends Annotator {

    private final ImmutableSet<MethodPattern> patterns;

    MethodNameAnnotator(ClassName annotation,
                        ImmutableSet<MethodPattern> patterns,
                        ImmutableList<FileDescriptor> descriptors,
                        Path genProtoDir) {
        super(annotation, descriptors, genProtoDir);
        this.patterns = patterns;
    }

    @Override
    public void annotate() {
        if (!patterns.isEmpty()) {
            SourceVisitor<?> visitor = new AnnotateMethods();
            descriptors().stream()
                         .map(TypeSet::from)
                         .map(TypeSet::allTypes)
                         .flatMap(ImmutableSet::stream)
                         .map(Type::javaClassName)
                         .map(SourceFile::whichDeclares)
                         .forEach(file -> rewriteSource(file, visitor));
        }
    }

    /**
     * A function annotating methods of a given source according to the patterns.
     */
    private final class AnnotateMethods implements SourceVisitor<JavaClassSource> {

        @Override
        public void accept(AbstractJavaSource<JavaClassSource> source) {
            if (source instanceof MethodHolderSource) {
                ((MethodHolderSource<?>) source)
                        .getMethods()
                        .stream()
                        .filter(Method::isPublic)
                        .filter(this::matching)
                        .forEach(MethodNameAnnotator.this::addAnnotation);
            }
        }

        private boolean matching(Method<?, ?> method) {
            String methodName = method.getName();
            return patterns.stream()
                           .anyMatch(pattern -> pattern.matches(methodName));
        }
    }
}
