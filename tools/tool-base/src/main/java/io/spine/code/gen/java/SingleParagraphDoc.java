package io.spine.code.gen.java;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.javadoc.JavadocText;

/**
 * A generated Javadoc which contains a single paragraph.
 */
public abstract class SingleParagraphDoc implements GeneratedJavadoc {

    @Override
    public CodeBlock spec() {
        CodeBlock.Builder text = CodeBlock
                .builder();
        addParagraph(text);
        JavadocText paragraph = JavadocText.fromEscaped(text.build().toString())
                                           .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(paragraph.value())
                .build();
        return value;
    }

    /**
     * Adds the paragraph text to the given {@code CodeBlock}.
     */
    protected abstract void addParagraph(CodeBlock.Builder text);
}
