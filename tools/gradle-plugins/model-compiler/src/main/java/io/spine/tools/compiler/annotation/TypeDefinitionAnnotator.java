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

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;
import io.spine.tools.java.SimpleClassName;
import io.spine.tools.java.SourceFile;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
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

    TypeDefinitionAnnotator(Class<? extends Annotation> annotation,
                            GeneratedExtension<L, Boolean> option,
                            Collection<FileDescriptorProto> files,
                            String genProtoDir) {
        super(annotation, option, files, genProtoDir);
    }

    @Override
    public final void annotate() {
        for (FileDescriptorProto file : fileDescriptors()) {
            annotate(file);
        }
    }

    @Override
    protected final void annotateOneFile(final FileDescriptorProto file) {
        final SourceFile outerClass = SourceFile.forOuterClassOf(file);
        rewriteSource(outerClass, new AnnotateNestedType(file));
    }

    @Override
    protected final void annotateMultipleFiles(FileDescriptorProto file) {
        for (D definitionDescriptor : getDefinitions(file)) {
            if (shouldAnnotate(definitionDescriptor)) {
                annotateDefinition(definitionDescriptor, file);
            }
        }
    }

    /**
     * Obtains all definitions of a particular type from the specified file descriptor.
     *
     * @param file the file descriptor from which to get definition list
     * @return the definitions list
     */
    protected abstract List<D> getDefinitions(FileDescriptorProto file);

    /**
     * Obtains a definition name for the specified descriptor.
     *
     * @param definition the definition descriptor
     * @return a definition name
     */
    protected abstract String getDefinitionName(D definition);

    /**
     * Annotates a Java source, generated basing on the specified definition descriptor.
     *
     * <p>This method is used for {@link FileDescriptorProto} if it
     * {@linkplain com.google.protobuf.DescriptorProtos.FileOptions#hasJavaMultipleFiles()
     * has multiple Java files}.
     *
     * @param definition the definition descriptor
     * @param file       the descriptor of the file containing the definition
     */
    protected abstract void annotateDefinition(D definition, FileDescriptorProto file);

    static <T extends JavaSource<T>> JavaSource findNestedType(AbstractJavaSource<T> enclosingClass,
                                                               String typeName) {
        for (JavaSource nestedType : enclosingClass.getNestedTypes()) {
            if (nestedType.getName()
                          .equals(typeName)) {
                return nestedType;
            }
        }

        final String errMsg = format("Nested type `%s` is not defined in `%s`.",
                                     typeName, enclosingClass.getName());
        throw new IllegalStateException(errMsg);
    }

    /**
     * Optionally annotates nested types in a file.
     */
    private class AnnotateNestedType implements SourceVisitor<JavaClassSource> {

        private final FileDescriptorProto file;

        private AnnotateNestedType(FileDescriptorProto file) {
            this.file = file;
        }

        @Nullable
        @Override
        public Void apply(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            for (D definition : getDefinitions(file)) {
                if (shouldAnnotate(definition)) {
                    final String messageName = getDefinitionName(definition);
                    final JavaSource message = findNestedType(input, messageName);
                    addAnnotation(message);

                    final String javaType = SimpleClassName.messageOrBuilder(messageName)
                                                           .value();
                    final JavaSource messageOrBuilder = findNestedType(input, javaType);
                    addAnnotation(messageOrBuilder);
                }
            }
            return null;
        }
    }
}
