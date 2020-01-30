package io.spine.code.gen.java.column;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.gen.java.TwoParagraphDoc;
import io.spine.code.javadoc.JavadocText;

/**
 * A Javadoc of the class which is a listing of columns of the entity.
 *
 * @see ColumnsSpec
 */
final class ColumnsDoc extends TwoParagraphDoc {

    @Override
    protected void addFirstParagraph(CodeBlock.Builder text) {
        text.add("A listing of all entity columns of the type.");
    }

    @Override
    protected void addSecondParagraph(CodeBlock.Builder text) {
        text.add("Use static methods of this class to access the columns of the entity$L" +
                         "which can then be used for query filters creation.",
                 JavadocText.lineSeparator());
    }
}
