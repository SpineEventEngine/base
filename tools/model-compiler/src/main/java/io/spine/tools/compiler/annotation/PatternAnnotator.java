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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.java.ClassName;
import io.spine.code.proto.TypeSet;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

final class PatternAnnotator extends Annotator {

    private final ImmutableList<FileDescriptor> descriptors;
    private final ClassNamePattern pattern;

    PatternAnnotator(ClassName annotation,
                     ClassNamePattern pattern,
                     ImmutableList<FileDescriptor> fileDescriptors,
                     Path genProtoDir) {
        super(annotation, genProtoDir);
        this.descriptors = fileDescriptors;
        this.pattern = pattern;
    }

    @Override
    public void annotate() {
        descriptors.stream()
                   .flatMap(PatternAnnotator::allClasses)
                   .filter(pattern::matches)
                   .forEach(this::annotate);
    }

    private static Stream<ClassName> allClasses(FileDescriptor file) {
        TypeSet typeSet = TypeSet.messagesAndEnums(file);
        ClassName outerClass = ClassName.outerClass(file);
        Stream.Builder<ClassName> result = Stream.builder();
        result.accept(outerClass);
        typeSet.types()
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
        rewriteSource(targetClass.resolveFile(), new NestedTypeDeclarationAnnotation(targetClass));
    }

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

        private AnnotationTargetSource<?, ?> findTarget(AbstractJavaSource<JavaClassSource> root) {
            String className = targetClass.value();
            Queue<AbstractJavaSource<JavaClassSource>> classesToCheck = new ArrayDeque<>();
            classesToCheck.add(root);
            while (!classesToCheck.isEmpty()) {
                AbstractJavaSource<JavaClassSource> currentClass = classesToCheck.poll();
                if (currentClass.getQualifiedName().equals(className)) {
                    return currentClass;
                }
                if (currentClass.hasNestedType(className)) {
                    return currentClass.getNestedType(className);
                }
                currentClass.getNestedTypes()
                            .stream()
                            .filter(type -> type instanceof AbstractJavaSource)
                            .forEach(type -> {
                                @SuppressWarnings("unchecked")
                                // Due to inconvenience of Roaster API.
                                        AbstractJavaSource<JavaClassSource> abstractJavaSource =
                                        (AbstractJavaSource<JavaClassSource>) type;
                                classesToCheck.add(abstractJavaSource);
                            });
            }
            throw newIllegalStateException("Class `%s` not found in Java source `%s`.",
                                           className,
                                           root.getCanonicalName());
        }
    }
}
