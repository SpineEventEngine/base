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

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.java.SourceFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.forge.roaster.model.impl.AbstractJavaSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Abstract base class for {@linkplain Annotator Annotators}
 * for a particular kind of type definition.
 */
abstract class TypeDefinitionAnnotator<D extends GenericDescriptor>
         extends Annotator<D> {

    TypeDefinitionAnnotator(ClassName annotation,
                            ApiOption option,
                            Collection<FileDescriptor> files,
                            String genProtoDir) {
        super(annotation, option, files, genProtoDir);
    }

    @Override
    public final void annotate() {
        for (FileDescriptor file : fileDescriptors()) {
            annotate(file);
        }
    }

    @Override
    protected final void annotateOneFile(FileDescriptor file) {
        SourceFile outerClass = SourceFile.forOuterClassOf(file.toProto());
        rewriteSource(outerClass, new AnnotateNestedType(file));
    }

    @Override
    protected final void annotateMultipleFiles(FileDescriptor file) {
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
    protected abstract List<D> getDefinitions(FileDescriptor file);

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
     * {@linkplain com.google.protobuf.DescriptorProtos.FileOptions#getJavaMultipleFiles()
     * has multiple Java files}.
     *
     * @param definition the definition descriptor
     * @param file       the descriptor of the file containing the definition
     */
    protected abstract void annotateDefinition(D definition, FileDescriptor file);

    static <T extends JavaSource<T>>
    JavaSource findNestedType(AbstractJavaSource<T> enclosingClass, String typeName) {
        for (JavaSource nestedType : enclosingClass.getNestedTypes()) {
            if (nestedType.getName()
                          .equals(typeName)) {
                return nestedType;
            }
        }

        String errMsg = format("Nested type `%s` is not defined in `%s`.",
                               typeName, enclosingClass.getName());
        throw new IllegalStateException(errMsg);
    }

    /**
     * Optionally annotates nested types in a file.
     */
    private class AnnotateNestedType implements SourceVisitor<JavaClassSource> {

        private final FileDescriptor file;

        private AnnotateNestedType(FileDescriptor file) {
            this.file = file;
        }

        @Override
        public void accept(@Nullable AbstractJavaSource<JavaClassSource> input) {
            checkNotNull(input);
            for (D definition : getDefinitions(file)) {
                if (shouldAnnotate(definition)) {
                    String messageName = getDefinitionName(definition);
                    JavaSource message = findNestedType(input, messageName);
                    addAnnotation(message);

                    String javaType = SimpleClassName.messageOrBuilder(messageName)
                                                     .value();
                    JavaSource messageOrBuilder = findNestedType(input, javaType);
                    addAnnotation(messageOrBuilder);
                }
            }
        }
    }
}
