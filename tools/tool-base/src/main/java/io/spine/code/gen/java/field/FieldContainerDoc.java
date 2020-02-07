package io.spine.code.gen.java.field;

import com.squareup.javapoet.CodeBlock;
import io.spine.code.gen.java.ThreeParagraphDoc;
import io.spine.code.javadoc.JavadocText;

/**
 * A Javadoc of the type which is a listing of message fields.
 *
 * @see FieldContainerSpec
 */
final class FieldContainerDoc extends ThreeParagraphDoc {

    @Override
    protected void addFirstParagraph(CodeBlock.Builder text) {
        text.add("The listing of all fields of the message type.");
    }

    @Override
    protected void addSecondParagraph(CodeBlock.Builder text) {
        text.add("The fields exposed by this class can be provided to a subscription filter on " +
                         "creation.");
    }

    @Override
    protected void addThirdParagraph(CodeBlock.Builder text) {
        text.add("Use static methods of this class to access the top-level fields of the " +
                         "message. The nested$L fields can be accessed using the values " +
                         "returned by the top-level field accessors, through$L method chaining.",
                 JavadocText.lineSeparator(), JavadocText.lineSeparator());
    }
}
