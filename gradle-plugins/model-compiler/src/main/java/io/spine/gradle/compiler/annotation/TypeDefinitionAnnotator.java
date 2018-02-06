/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.annotation;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.gradle.compiler.util.JavaSources.getFilePath;
import static io.spine.gradle.compiler.util.JavaSources.getOrBuilderSuffix;
import static java.lang.String.format;

/**
 * Abstract base class for {@linkplain Annotator Annotators}
 * for a particular kind of type definition.
 *
 * @param <L> {@inheritDoc}
 * @param <D> {@inheritDoc}
 * @author Dmytro Grankin
 */
abstract class TypeDefinitionAnnotator<L extends ExtendableMessage, D extends GeneratedMessageV3>
         extends Annotator<L, D> {

    protected TypeDefinitionAnnotator(Class<? extends Annotation> annotation,
                                      GeneratedExtension<L, Boolean> option,
                                      Collection<FileDescriptorProto> fileDescriptors,
                                      String genProtoDir) {
        super(annotation, option, fileDescriptors, genProtoDir);
    }

    @Override
    final void annotate() {
        for (FileDescriptorProto fileDescriptor : fileDescriptors()) {
            annotate(fileDescriptor);
        }
    }

    @Override
    protected final void annotateSingularFile(final FileDescriptorProto fileDescriptor) {
        final Path relativeFilePath = getFilePath(fileDescriptor);
        rewriteSource(relativeFilePath, new SourceVisitor<JavaClassSource>() {
            @Nullable
            @Override
            public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
                checkNotNull(input);
                for (D definitionDescriptor : getDefinitions(fileDescriptor)) {
                    if (shouldAnnotate(definitionDescriptor)) {
                        final String messageName = getDefinitionName(definitionDescriptor);
                        final JavaSource message = getNestedTypeByName(input, messageName);
                        addAnnotation(message);

                        final String messageOrBuilderName = messageName + getOrBuilderSuffix();
                        final JavaSource messageOrBuilder =
                                getNestedTypeByName(input, messageOrBuilderName);
                        addAnnotation(messageOrBuilder);
                    }
                }
                return null;
            }
        });
    }

    @Override
    protected final void annotateMultipleFiles(FileDescriptorProto fileDescriptor) {
        for (D definitionDescriptor : getDefinitions(fileDescriptor)) {
            if (shouldAnnotate(definitionDescriptor)) {
                annotateDefinition(definitionDescriptor, fileDescriptor);
            }
        }
    }

    /**
     * Obtains all definitions of a particular type from the specified file descriptor.
     *
     * @param fileDescriptor the file descriptor from which to get definition list
     * @return the definitions list
     */
    protected abstract List<D> getDefinitions(FileDescriptorProto fileDescriptor);

    /**
     * Obtains a definition name for the specified descriptor.
     *
     * @param definitionDescriptor the definition descriptor
     * @return a definition name
     */
    protected abstract String getDefinitionName(D definitionDescriptor);

    /**
     * Annotates a Java source, generated basing on the specified definition descriptor.
     *
     * <p>This method is used for {@link FileDescriptorProto} if it
     * {@linkplain com.google.protobuf.DescriptorProtos.FileOptions#hasJavaMultipleFiles()
     * has multiple Java files}.
     *
     * @param definitionDescriptor the definition descriptor
     * @param fileDescriptor       the file descriptor, which contains the definition descriptor
     */
    protected abstract void annotateDefinition(D definitionDescriptor,
                                               FileDescriptorProto fileDescriptor);

    static <T extends JavaSource<T>> JavaSource getNestedTypeByName(AbstractJavaSource<T> javaClass,
                                                                    String typeName) {
        for (JavaSource nestedType : javaClass.getNestedTypes()) {
            if (nestedType.getName()
                          .equals(typeName)) {
                return nestedType;
            }
        }

        final String errMsg = format("Nested type `%s` is not defined in `%s`.",
                                     typeName, javaClass.getName());
        throw new IllegalStateException(errMsg);
    }
}
