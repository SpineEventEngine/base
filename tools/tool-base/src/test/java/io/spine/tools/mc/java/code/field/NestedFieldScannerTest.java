/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.code.field;

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
