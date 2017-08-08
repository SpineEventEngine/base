/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.option;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.StringValue;
import org.junit.Test;

import java.util.Collection;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class ValidationTargetsParserShould {

    private final ValidationTargetsParser parser = ValidationTargetsParser.getInstance();

    @Test
    public void have_the_private_ctor() {
        assertHasPrivateParameterlessCtor(ValidationTargetsParser.class);
    }

    @Test
    public void not_prepare_additional_actions_with_option_part() {
        final String value = "a value";
        assertSame(value, parser.asElement(value));
    }

    @Test
    public void return_empty_collection_if_option_is_not_present() {
        final DescriptorProtos.DescriptorProto definitionWithoutOption = StringValue.getDescriptor()
                                                                                    .toProto();
        final Collection<String> result = parser.parseUnknownOption(definitionWithoutOption);
        assertTrue(result.isEmpty());
    }
}
