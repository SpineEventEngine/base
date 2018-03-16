/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.proto;

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.test.compiler.message.Top;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class SourceFileShould {

    private static final FileDescriptorProto TEST_FILE_DESCRIPTOR = Top.getDescriptor()
                                                                       .getFile()
                                                                       .toProto();
    private SourceFile sourceFile;

    @Before
    public void setUp() {
        sourceFile = SourceFile.from(TEST_FILE_DESCRIPTOR);
    }

    @Test
    public void search_nested_declarations_recursively() {
        final Descriptor nestedForNested = Top.NestedForTop.NestedForNested.getDescriptor();
        final TypeName expectedTypeName = TypeName.from(nestedForNested);
        final MessageDeclaration result = findDeclaration(expectedTypeName.getSimpleName());
        assertEquals(expectedTypeName, result.getTypeName());
    }

    private MessageDeclaration findDeclaration(String name) {
        final Predicate<DescriptorProto> predicate = new MessageWithName(name);
        final Collection<MessageDeclaration> searchResult = sourceFile.allThat(predicate);
        assertEquals(searchResult.size(), 1);
        return searchResult.iterator()
                           .next();
    }

    /**
     * Test predicate that matches a message declaration by its name.
     */
    private static class MessageWithName implements Predicate<DescriptorProto> {

        private final String name;

        private MessageWithName(String name) {
            this.name = name;
        }

        @Override
        public boolean apply(@Nullable DescriptorProto input) {
            checkNotNull(input);
            final String messageName = input.getName();
            return messageName.equals(name);
        }
    }
}
