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
import io.spine.tools.code.java.ClassName;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.Files.exists;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Abstract base class for the annotators of the generated Java sources.
 *
 * <p>{@code Annotator} inserts a reference to the specified annotation
 * to the pre-configured spots of the Java files, generated basing on Protobuf definitions.
 */
public abstract class Annotator {


    private final ImmutableList<FileDescriptor> descriptors;

    /**
     * The name of the Java class of the annotation to apply.
     */
    private final ClassName annotation;

    /**
     * An absolute path to the Java sources to annotate.
     */
    private final Path genProtoDir;

    protected Annotator(ClassName annotation,
                        ImmutableList<FileDescriptor> descriptors,
                        Path genProtoDir) {
        this.annotation = checkNotNull(annotation);
        this.descriptors = checkNotNull(descriptors);
        this.genProtoDir = checkNotNull(genProtoDir);
    }

    /**
     * Annotates the Java sources generated from Protobuf.
     */
    public abstract void annotate();

    /**
     * Rewrites the file applying {@link TypeDeclarationAnnotation}.
     */
    protected final void annotate(SourceFile relativeSourcePath) {
        rewriteSource(relativeSourcePath, new TypeDeclarationAnnotation());
    }

    /**
     * Rewrites a generated Java source with the specified
     * relative path after applying a {@link SourceVisitor}.
     *
     * @param relativeSourcePath
     *         the relative path to a source file
     * @param visitor
     *         the source visitor
     */
    protected <T extends JavaSource<T>>
    void rewriteSource(SourceFile relativeSourcePath, SourceVisitor<T> visitor) {
        rewriteSource(genProtoDir, relativeSourcePath, visitor);
    }

    /**
     * Rewrites a Java source with the specified path after applying a {@link SourceVisitor}.
     *
     * <p>If the specified path does not exist, does nothing.
     *
     * @param sourcePathPrefix
     *         the prefix for the relative source path
     * @param sourceFile
     *         the relative path to a source file
     * @param visitor
     *         the source visitor
     */
    static <T extends JavaSource<T>>
    void rewriteSource(Path sourcePathPrefix, SourceFile sourceFile, SourceVisitor<T> visitor) {
        Path absoluteSourcePath = sourcePathPrefix.resolve(sourceFile.path());
        if (exists(absoluteSourcePath)) {
            @SuppressWarnings("unchecked" /* There is no way to specify generic parameter
                                             for `AbstractJavaSource.class` value. */)
            AbstractJavaSource<T> javaSource = (AbstractJavaSource<T>) parse(absoluteSourcePath);
            visitor.accept(javaSource);
            rewrite(javaSource, absoluteSourcePath);
        }
    }

    private static <T extends JavaSource<T>>
    void rewrite(AbstractJavaSource<T> javaSource, Path destination) {
        String resultingSource = javaSource.toString();
        try {
            Files.write(destination, ImmutableList.of(resultingSource), TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static AbstractJavaSource<?> parse(Path sourcePath) {
        try {
            return Roaster.parse(AbstractJavaSource.class, sourcePath.toFile());
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Adds a fully qualified {@link #annotation} to the {@link AnnotationTargetSource}.
     *
     * <p>If the specified source already has the {@link #annotation},
     * does nothing to avoid annotation duplication and a compilation error as a result.
     *
     * @param source
     *         the program element to annotate
     */
    protected final void addAnnotation(AnnotationTargetSource<?, ?> source) {
        String annotationFQN = annotation.value();
        AnnotationSource<?> annotation = source.getAnnotation(annotationFQN);
        if (annotation == null) {
            AnnotationSource<?> newAnnotation = source.addAnnotation();
            newAnnotation.setName(annotationFQN);
        }
    }

    /**
     * Obtains the list of file descriptors from which the annotated Java code is generated.
     */
    protected final ImmutableList<FileDescriptor> descriptors() {
        return descriptors;
    }

    /**
     * An annotation function, that annotates the type declaration,
     * which is represented by {@link AbstractJavaSource}.
     */
    protected class TypeDeclarationAnnotation implements SourceVisitor<JavaClassSource> {

        @Override
        public void accept(AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            addAnnotation(input);
        }
    }
}
