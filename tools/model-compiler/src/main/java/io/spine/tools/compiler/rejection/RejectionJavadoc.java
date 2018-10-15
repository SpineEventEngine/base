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

package io.spine.tools.compiler.rejection;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import io.spine.code.javadoc.JavadocEscaper;
import io.spine.code.proto.RejectionDeclaration;

import java.util.Optional;

/**
 * Generates Javadoc for a rejection.
 *
 * <p>Requires the following Protobuf plugin configuration:
 * <pre> {@code
 * generateProtoTasks {
 *     all().each { final task ->
 *         // If true, the descriptor set will contain line number information
 *         // and comments. Default is false.
 *         task.descriptorSetOptions.includeSourceInfo = true
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * @see <a href="https://github.com/google/protobuf-gradle-plugin/blob/master/README.md#generate-descriptor-set-files">
 *         Protobuf plugin configuration</a>
 */
class RejectionJavadoc {

    private static final String OPENING_PRE = "<pre>";
    private static final String CLOSING_PRE = "</pre>";
    /*
      TODO:2017-03-24:dmytro.grankin: Replace hardcoded line separator by system-independent
      after https://github.com/square/javapoet/issues/552 is fixed.
    */
    @SuppressWarnings("HardcodedLineSeparator")
    private static final String LINE_SEPARATOR = "\n";

    private final RejectionDeclaration declaration;
    private final RejectionDocumentation documentation;

    RejectionJavadoc(RejectionDeclaration declaration, RejectionDocumentation documentation) {
        this.declaration = declaration;
        this.documentation = documentation;
    }

    /**
     * Generates a Javadoc content for the rejection.
     *
     * @return the class-level Javadoc content
     */
    String forClass() {
        Optional<String> leadingComments = documentation.leadingComments();
        StringBuilder builder = new StringBuilder(256);

        leadingComments.ifPresent(s -> builder.append(OPENING_PRE)
                                              .append(LINE_SEPARATOR)
                                              .append(JavadocEscaper.escape(s))
                                              .append(CLOSING_PRE)
                                              .append(LINE_SEPARATOR)
                                              .append(LINE_SEPARATOR));

        builder.append("Rejection based on proto type {@code ")
               .append(declaration.getJavaPackage())
               .append('.')
               .append(declaration.getSimpleJavaClassName())
               .append('}')
               .append(LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     * Generates a Javadoc content for the rejection constructor.
     *
     * @param builderParameter
     *         the name of a rejection builder parameter
     * @return the constructor Javadoc content
     */
    CodeBlock forConstructor(ParameterSpec builderParameter) {
        return CodeBlock.builder()
                        .add("Creates a new instance.")
                        .add(LINE_SEPARATOR)
                        .add(LINE_SEPARATOR)
                        .add("@param $L the builder for the rejection", builderParameter)
                        .add(LINE_SEPARATOR)
                        .build();
    }
}
