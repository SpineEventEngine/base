package io.spine.code.gen.java;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.javadoc.JavadocText;

/**
 * A generated Javadoc with three paragraphs.
 */
public abstract class ThreeParagraphDoc implements GeneratedJavadoc {

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
                                                 .withNewLine()
                                                 .withNewLine();

        CodeBlock.Builder thirdParagraphText = CodeBlock.builder();
        addThirdParagraph(thirdParagraphText);
        String thirdParagraphString = thirdParagraphText.build()
                                                        .toString();
        JavadocText thirdParagraph = JavadocText.fromEscaped(thirdParagraphString)
                                                .withPTag()
                                                .withNewLine();
        CodeBlock value = CodeBlock
                .builder()
                .add(firstParagraph.value())
                .add(secondParagraph.value())
                .add(thirdParagraph.value())
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

    /**
     * Adds the third paragraph text to the passed {@code CodeBlock}.
     */
    protected abstract void addThirdParagraph(CodeBlock.Builder text);
}
