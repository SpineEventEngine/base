package io.spine.code.gen.java.field;

import com.google.common.truth.Correspondence;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.test.code.generate.Article;
import io.spine.test.code.generate.ArticleText;
import io.spine.test.code.generate.Author;
import io.spine.test.code.generate.AuthorName;
import io.spine.test.code.generate.Issue;
import io.spine.test.code.generate.Magazine;
import io.spine.test.code.generate.MagazineCover;
import io.spine.test.code.generate.MagazineNumber;
import io.spine.test.code.generate.MagazineTitle;
import io.spine.test.code.generate.Volume;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("`NestedFieldScanner` should")
class NestedFieldScannerTest {

    @Test
    @DisplayName("collect types of all top-level and nested `Message`-typed fields of a type")
    void collectTypes() {
        List<MessageType> result = scan(Magazine.getDescriptor());

        assertThat(result)
                .comparingElementsUsing(classCorrespondence())
                .containsExactly(MagazineTitle.class, MagazineNumber.class,
                                 Volume.class, Issue.class, Timestamp.class);
    }

    @Test
    @DisplayName("collect zero types if the message type doesn't have `Message`-typed fields")
    void collectZeroTypes() {
        List<MessageType> result = scan(Volume.getDescriptor());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("collect uniquely-named message types")
    void collectUniquelyNamed() {
        List<MessageType> result = scan(MagazineCover.getDescriptor());

        assertThat(result)
                .comparingElementsUsing(nameCorrespondence())
                .containsExactly("Headline", "Photo", "Url");
    }

    @Test
    @DisplayName("collect a single instance of type in case of circular field references")
    void handleCircularReferences() {
        List<MessageType> result = scan(Author.getDescriptor());
        assertThat(result)
                .comparingElementsUsing(classCorrespondence())
                .containsExactly(AuthorName.class, Author.class);
    }

    @Test
    @DisplayName("collect a single instance of type in case of two fields with the same type")
    void handleSameTypeFields() {
        List<MessageType> result = scan(Article.getDescriptor());

        assertThat(result)
                .comparingElementsUsing(classCorrespondence())
                .containsExactly(Article.Headline.class, ArticleText.class);
    }

    private static List<MessageType> scan(Descriptor messageType) {
        MessageType type = new MessageType(messageType);
        NestedFieldScanner scanner = new NestedFieldScanner(type);
        return scanner.scan();
    }

    private static Correspondence<MessageType, Class<? extends Message>> classCorrespondence() {
        return Correspondence.from(NestedFieldScannerTest::classEquals,
                                   "wraps");
    }

    private static boolean classEquals(MessageType type, Class<? extends Message> other) {
        return other.equals(type.javaClass());
    }

    private static Correspondence<MessageType, String> nameCorrespondence() {
        return Correspondence.from(NestedFieldScannerTest::nameEquals,
                                   "has the same simple name as");
    }

    private static boolean nameEquals(MessageType type, String name) {
        String typeName = type.simpleJavaClassName()
                              .value();
        return name.equals(typeName);
    }
}
