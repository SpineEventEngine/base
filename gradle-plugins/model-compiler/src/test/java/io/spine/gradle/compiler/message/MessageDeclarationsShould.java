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

package io.spine.gradle.compiler.message;

import com.google.common.base.Predicate;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import io.spine.test.compiler.message.Top;
import io.spine.tools.proto.MessageDeclaration;
import io.spine.type.TypeName;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.proto.MessageDeclarations.find;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class MessageDeclarationsShould {

    private static final FileDescriptorProto TEST_FILE_DESCRIPTOR = Top.getDescriptor()
                                                                       .getFile()
                                                                       .toProto();

    @Test
    public void search_nested_declarations_recursively() {
        final Descriptor nestedForNested = Top.NestedForTop.NestedForNested.getDescriptor();
        final TypeName expectedTypeName = TypeName.from(nestedForNested);
        final MessageDeclaration result = findDeclaration(expectedTypeName.getSimpleName());
        assertEquals(expectedTypeName, result.getTypeName());
    }

    private static MessageDeclaration findDeclaration(String name) {
        final Predicate<DescriptorProto> predicate = new MessageWithName(name);
        final Collection<MessageDeclaration> searchResult = find(singleton(TEST_FILE_DESCRIPTOR),
                                                                 predicate);
        assertEquals(searchResult.size(), 1);
        return searchResult.iterator()
                           .next();
    }

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
