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

package io.spine.type;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Error;
import io.spine.code.proto.Type;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import io.spine.test.types.Task;
import io.spine.test.types.TaskId;
import io.spine.test.types.TaskName;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static io.spine.testing.Verify.assertSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Litus
 */
public class KnownTypesShould {

    private KnownTypes knownTypes;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        knownTypes = KnownTypes.instance();
    }

    @Test
    public void return_known_proto_message_type_urls() {
        Set<TypeUrl> typeUrls = knownTypes.getAllUrls();

        assertFalse(typeUrls.isEmpty());
    }

    @Test
    public void return_spine_java_class_names_by_proto_type_urls() {
        assertHasClassNameByTypeUrlOf(EntityOption.class);
        assertHasClassNameByTypeUrlOf(Error.class);
        assertHasClassNameByTypeUrlOf(IfMissingOption.class);
    }

    @Test
    public void return_google_java_class_names_by_proto_type_urls() {
        assertHasClassNameByTypeUrlOf(Any.class);
        assertHasClassNameByTypeUrlOf(Timestamp.class);
        assertHasClassNameByTypeUrlOf(Duration.class);
        assertHasClassNameByTypeUrlOf(Empty.class);
    }

    private void assertHasClassNameByTypeUrlOf(Class<? extends Message> msgClass) {
        TypeUrl typeUrl = TypeUrl.of(msgClass);

        ClassName className = knownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(msgClass), className);
    }

    @Test
    public void return_java_inner_class_name_by_proto_type_url() {
        TypeUrl typeUrl = TypeUrl.from(EntityOption.Kind.getDescriptor());

        ClassName className = knownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(EntityOption.Kind.class), className);
    }

    @Test
    public void return_proto_type_url_by_proto_type_name() {
        TypeUrl typeUrlExpected = TypeUrl.from(StringValue.getDescriptor());

        Optional<TypeUrl> typeUrlActual = knownTypes.find(typeUrlExpected.toName())
                                                    .map(Type::url);
        assertTrue(typeUrlActual.isPresent());
        assertEquals(typeUrlExpected, typeUrlActual.get());
    }

    @Test
    public void return_all_types_under_certain_package() {
        TypeUrl taskId = TypeUrl.from(TaskId.getDescriptor());
        TypeUrl taskName = TypeUrl.from(TaskName.getDescriptor());
        TypeUrl task = TypeUrl.from(Task.getDescriptor());

        String packageName = "spine.test.types";

        Collection<TypeUrl> packageTypes = knownTypes.getAllFromPackage(packageName);
        assertSize(3, packageTypes);
        assertTrue(packageTypes.containsAll(Arrays.asList(taskId, taskName, task)));
    }

    @Test
    public void return_empty_collection_if_package_is_empty_or_invalid() {
        String packageName = "com.foo.invalid.package";
        Collection<?> emptyTypesCollection = knownTypes.getAllFromPackage(packageName);
        assertNotNull(emptyTypesCollection);
        assertTrue(emptyTypesCollection.isEmpty());
    }

    @Test
    public void do_not_return_types_of_package_by_package_prefix() {
        String prefix = "spine.test.ty"; // "spine.test.types" is a valid package

        Collection<TypeUrl> packageTypes = knownTypes.getAllFromPackage(prefix);
        assertTrue(packageTypes.isEmpty());
    }

    @Test
    public void throw_exception_if_no_java_class_name_by_type_url() {
        TypeUrl unexpectedUrl = TypeUrl.parse("prefix/unexpected.type");
        thrown.expect(UnknownTypeException.class);
        knownTypes.getClassName(unexpectedUrl);
    }
}
