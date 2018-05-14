/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.common.testing.NullPointerTester;
import org.junit.Test;

import java.util.List;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.tools.proto.FileName.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Yevsyukov
 */
public class FileNameShould {

    @Test
    public void have_utility_ctor_for_Suffix() {
        assertHasPrivateParameterlessCtor(FileName.Suffix.class);
    }

    @Test
    public void pass_null_tolerance_check() {
        new NullPointerTester().testStaticMethods(FileName.class,
                                                  NullPointerTester.Visibility.PACKAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void require_standard_extension() {
        FileName.of("some_thing");
    }

    @Test
    public void return_words() {
        final List<String> words = FileName.of("some_file_name.proto").words();

        assertEquals(ImmutableList.of("some", "file", "name"), words);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    public void calculate_outer_class_name() {
        assertEquals(RejectionDeclaration.OUTER_CLASS_NAME_SUFFIX,
                     of("rejections.proto").nameOnlyCamelCase());
        assertEquals("ManyRejections",
                     of("many_rejections.proto").nameOnlyCamelCase());
        assertEquals("ManyMoreRejections",
                     of("many_more_rejections.proto").nameOnlyCamelCase());
    }

    @Test
    public void tell_commands_file_kind() {
        final FileName commandsFile = FileName.of("my_commands.proto");

        assertTrue(commandsFile.isCommands());
        assertFalse(commandsFile.isEvents());
        assertFalse(commandsFile.isRejections());
    }

    @Test
    public void tell_events_file_kind() {
        final FileName eventsFile = FileName.of("project_events.proto");

        assertTrue(eventsFile.isEvents());
        assertFalse(eventsFile.isCommands());
        assertFalse(eventsFile.isRejections());
    }

    @Test
    public void tell_rejections_file_kind() {
        final FileName rejectsionFile = FileName.of("rejections.proto");

        assertTrue(rejectsionFile.isRejections());
        assertFalse(rejectsionFile.isCommands());
        assertFalse(rejectsionFile.isEvents());
    }
}
