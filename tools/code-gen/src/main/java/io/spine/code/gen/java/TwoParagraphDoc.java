package io.spine.code.gen.java;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.gen.javadoc.JavadocText;

/**
 * A generated Javadoc with two paragraphs.
 */
public abstract class TwoParagraphDoc implements GeneratedJavadoc {

    @Override
    public CodeBlock spec() {
        CodeBlock.Builder firstParagraphText = CodeBlock.builder();
        addFirstParagraph(firstParagraphText);
        String firstParagraphString = firstParagraphText.build()
                                                        .toString();
        JavadocText firstParagraph = JavadocText.fromEscaped(firstParagraphString)
                                                .withNewLine()
                                                .withNewLine();

        CodeBlock.Builder secondParagraphText = CodeBlock.builder();
        addSecondParagraph(secondParagraphText);
        String secondParagraphString = secondParagraphText.build()
                                                          .toString();
        JavadocText secondParagraph = JavadocText.fromEscaped(secondParagraphString)
                                                 .withPTag()
                                                 .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .build();
        return value;
    }

    /**
     * Adds the first paragraph text to the passed {@code CodeBlock}.
     */
    protected abstract void addFirstParagraph(CodeBlock.Builder text);

    /**
     * Adds the second paragraph text to the passed {@code CodeBlock}.
     */
    protected abstract void addSecondParagraph(CodeBlock.Builder text);
}
