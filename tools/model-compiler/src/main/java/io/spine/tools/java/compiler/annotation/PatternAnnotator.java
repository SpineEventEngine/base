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

package io.spine.tools.java.compiler.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.java.fs.SourceFile;
import io.spine.tools.java.gen.NestedClassName;
import io.spine.tools.code.java.ClassName;
import io.spine.tools.code.java.SimpleClassName;
import io.spine.tools.code.proto.TypeSet;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An {@link Annotator} which annotates Java classes names of which match certain
 * {@linkplain io.spine.tools.java.compiler.annotation.ClassNamePattern patterns}.
 */
final class PatternAnnotator extends Annotator {

    private final ClassNamePattern pattern;

    PatternAnnotator(ClassName annotation,
                     ClassNamePattern pattern,
                     ImmutableList<FileDescriptor> fileDescriptors,
                     Path genProtoDir) {
        super(annotation, fileDescriptors, genProtoDir);
        this.pattern = pattern;
    }

    @Override
    public void annotate() {
        descriptors().stream()
                     .flatMap(PatternAnnotator::allClasses)
                     .filter(pattern::matches)
                     .forEach(this::annotate);
    }

    private static Stream<ClassName> allClasses(FileDescriptor file) {
        TypeSet typeSet = TypeSet.from(file);
        ClassName outerClass = ClassName.outerClass(file);
        Stream.Builder<ClassName> result = Stream.builder();
        result.accept(outerClass);
        typeSet.allTypes()
               .stream()
               .flatMap(type -> {
                   ClassName typeName = type.javaClassName();
                   return type.supportsBuilders()
                          ? Stream.of(typeName, typeName.orBuilder())
                          : Stream.of(typeName);
               })
               .forEach(result);
        return result.build();
    }

    private void annotate(ClassName targetClass) {
        rewriteSource(SourceFile.whichDeclares(targetClass),
                      new NestedTypeDeclarationAnnotation(targetClass));
    }

    /**
     * An annotation function, that finds and annotates a type declaration by the name of the type.
     *
     * <p>The target type may be nested or top-level. The function first checks the root type and
     * looks in depth into nested types if the root it not the target.
     */
    private final class NestedTypeDeclarationAnnotation implements SourceVisitor<JavaClassSource> {

        private final ClassName targetClass;

        private NestedTypeDeclarationAnnotation(ClassName targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public void accept(AbstractJavaSource<JavaClassSource> source) {
            checkNotNull(source);
            AnnotationTargetSource<?, ?> target = findTarget(source);
            addAnnotation(target);
        }

        /**
         * Performs a breadth-first search through the tree of nested type declarations and obtains
         * the target type.
         *
         * <p>If the target is not found, an {@link java.lang.IllegalStateException} is thrown.
         *
         * @param root
         *         the root of the type declaration lookup
         * @return target type declaration
         */
        private AnnotationTargetSource<?, ?> findTarget(AbstractJavaSource<JavaClassSource> root) {
            if (root.getQualifiedName().equals(targetClass.value())) {
                return root;
            } else {
                ImmutableList<SimpleClassName> names = NestedClassName.from(targetClass).split();
                checkState(!names.isEmpty(), "Invalid class name %s.", targetClass);
                SimpleClassName rootName = names.get(0);
                checkArgument(root.getName()
                                  .equals(rootName.value()));
                AbstractJavaSource<JavaClassSource> source = root;
                for (SimpleClassName name : names.subList(1, names.size())) {
                    @SuppressWarnings("unchecked")
                    AbstractJavaSource<JavaClassSource> nested =
                            (AbstractJavaSource<JavaClassSource>) source.getNestedType(name.value());
                    source = nested;
                }
                checkState(source.getQualifiedName()
                                 .equals(targetClass.value()));
                return source;
            }
        }
    }
}
