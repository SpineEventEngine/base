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

package io.spine.tools.compiler.annotation;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import io.spine.code.java.SourceFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Abstract base class for the annotators of the generated Java sources.
 *
 * <p>{@code Annotator} inserts a reference to the specified {@link #annotation}
 * to the pre-configured spots of the Java files, generated basing on Protobuf definitions.
 *
 * <p>Different kinds of annotators are purposed for different Protobuf option types
 * such as {@code FileOptions}, {@code MessageOptions} etc.
 *
 * <p>Depending on the option type, an annotator manages a corresponding Protobuf descriptor
 * (e.g. {@code FileDescriptorProto} for {@code FileOptions}).
 *
 * @param <O> the type of Protobuf option, which is managed by the annotator
 * @param <D> the proto descriptor type used to receive {@link #option} value
 * @author Dmytro Grankin
 */
public abstract class Annotator<O extends ExtendableMessage, D extends GeneratedMessageV3> {

    /**
     * An annotation class.
     */
    private final Class<? extends Annotation> annotation;

    /**
     * An Protobuf option, that tells whether generated program elements should be annotated.
     *
     * <p>Can be of any option type, which is {@code boolean}.
     */
    private final GeneratedExtension<O, Boolean> option;

    /**
     * Protobuf file descriptors to process.
     */
    private final ImmutableList<FileDescriptorProto> fileDescriptors;

    /**
     * An absolute path to the Java sources, generated basing on {@link #fileDescriptors}.
     */
    private final String genProtoDir;

    protected Annotator(Class<? extends Annotation> annotation,
                        GeneratedExtension<O, Boolean> option,
                        Collection<FileDescriptorProto> fileDescriptors,
                        String genProtoDir) {
        this.annotation = checkNotNull(annotation);
        this.option = checkNotNull(option);
        this.fileDescriptors = ImmutableList.copyOf(checkNotNull(fileDescriptors));
        this.genProtoDir = checkNotNull(genProtoDir);
    }

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // OK to return immutable impl.
    protected Iterable<FileDescriptorProto> fileDescriptors() {
        return fileDescriptors;
    }

    /**
     * Annotates the Java sources generated from the passed
     * {@linkplain #fileDescriptors file descriptors}.
     */
    public abstract void annotate();

    /**
     * Annotates the Java sources generated from the specified file descriptor
     */
    protected final void annotate(FileDescriptorProto fileDescriptor) {
        if (fileDescriptor.getOptions()
                          .hasJavaMultipleFiles()) {
            annotateMultipleFiles(fileDescriptor);
        } else {
            annotateOneFile(fileDescriptor);
        }
    }

    /**
     * Annotates the Java sources generated from the specified file descriptor
     * if {@linkplain com.google.protobuf.DescriptorProtos.FileOptions#hasJavaMultipleFiles()
     * FileOptions#hasJavaMultipleFiles()} is {@code false}.
     *
     * @param fileDescriptor the file descriptor
     */
    protected abstract void annotateOneFile(FileDescriptorProto fileDescriptor);

    /**
     * Annotates the Java sources generated from the specified file descriptor
     * if {@linkplain com.google.protobuf.DescriptorProtos.FileOptions#hasJavaMultipleFiles()
     * FileOptions#hasJavaMultipleFiles()} is {@code true}.
     *
     * @param fileDescriptor the file descriptor
     */
    protected abstract void annotateMultipleFiles(FileDescriptorProto fileDescriptor);

    /**
     * Tells whether the generated program elements
     * from the specified descriptor should be annotated.
     *
     * @param descriptor the descriptor to extract {@link #option} value.
     * @return {@code true} if generated element should be annotated, {@code false} otherwise
     */
    protected final boolean shouldAnnotate(D descriptor) {
        final String rawValue = getRawOptionValue(descriptor);

        if (rawValue == null) {
            return false;
        }

        final boolean optionValue = Integer.parseInt(rawValue) != 0;
        return optionValue;
    }

    /**
     * Obtains a raw value of {@link #option} in the specified descriptor.
     *
     * <p>The value is a string representation of {@link #option}
     * (either {@code 0} or {@code 1}, as its type is {@code boolean}),
     * that is extracted from {@code descriptor.getOptions.getUnknownFields()}.
     *
     * <p>As the value is a part parsed from a string representation of an
     * {@linkplain com.google.protobuf.UnknownFieldSet UnknownFieldSet}, it is a {@code String}.
     *
     * @param descriptor the descriptor to extract {@link #option} value.
     * @return the option value
     * @see #shouldAnnotate(GeneratedMessageV3)
     * @see io.spine.option.UnknownOptions
     */
    protected abstract String getRawOptionValue(D descriptor);

    /**
     * Obtains the {@link #option} number.
     *
     * @return the option number
     */
    protected final int getOptionNumber() {
        return option.getNumber();
    }

    /**
     * Rewrites a generated Java source with the specified
     * relative path after applying a {@link SourceVisitor}.
     *
     * @param relativeSourcePath the relative path to a source file
     * @param sourceVisitor      the source visitor
     */
    protected <T extends JavaSource<T>> void rewriteSource(SourceFile relativeSourcePath,
                                                           SourceVisitor<T> sourceVisitor) {
        rewriteSource(genProtoDir, relativeSourcePath, sourceVisitor);
    }

    /**
     * Rewrites a Java source with the specified path after applying a {@link SourceVisitor}.
     *
     * <p>If the specified path does not exist, does nothing.
     *
     * @param sourcePathPrefix the prefix for the relative source path
     * @param sourcePath       the relative path to a source file
     * @param sourceVisitor    the source visitor
     */
    @SuppressWarnings("unchecked" /* There is no way to specify generic parameter
                                     for `AbstractJavaSource.class` value. */)
    protected static <T extends JavaSource<T>> void rewriteSource(String sourcePathPrefix,
                                                                  SourceFile sourcePath,
                                                                  SourceVisitor<T> sourceVisitor) {
        final AbstractJavaSource<T> javaSource;
        final Path absoluteSourcePath = Paths.get(sourcePathPrefix, sourcePath.toString());

        if (!Files.exists(absoluteSourcePath)) {
            // Do nothing.
            return;
        }

        try {
            javaSource = Roaster.parse(AbstractJavaSource.class, absoluteSourcePath.toFile());
        } catch (FileNotFoundException e) {
            throw illegalStateWithCauseOf(e);
        }

        sourceVisitor.apply(javaSource);
        final String resultingSource = javaSource.toString();
        try {
            Files.write(absoluteSourcePath, resultingSource.getBytes(), TRUNCATE_EXISTING);
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
     * @param source the program element to annotate
     */
    protected final void addAnnotation(AnnotationTargetSource source) {
        if (source.getAnnotation(annotation) != null) {
            return;
        }

        final String annotationFQN = annotation.getCanonicalName();
        final AnnotationSource newAnnotation = source.addAnnotation();
        newAnnotation.setName(annotationFQN);
    }

    /**
     * An annotation function, that annotates the type declaration,
     * which is represented by {@link AbstractJavaSource}.
     */
    protected class TypeDeclarationAnnotation implements SourceVisitor<JavaClassSource> {
        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            addAnnotation(input);
            return null;
        }
    }
}
