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

package io.spine.type;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.Error;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import io.spine.test.types.Task;
import io.spine.test.types.TaskId;
import io.spine.test.types.TaskName;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.test.Verify.assertSize;
import static io.spine.type.KnownTypes.getAllFromPackage;
import static io.spine.type.KnownTypes.getDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Litus
 */
public class KnownTypesShould {

    @Test
    public void have_private_constructor() {
        assertHasPrivateParameterlessCtor(KnownTypes.class);
    }

    @Test
    public void return_known_proto_message_type_urls() {
        final Set<TypeUrl> typeUrls = KnownTypes.getAllUrls();

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

    private static void assertHasClassNameByTypeUrlOf(Class<? extends Message> msgClass) {
        final TypeUrl typeUrl = TypeUrl.of(msgClass);

        final ClassName className = KnownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(msgClass), className);
    }

    @Test
    public void return_java_inner_class_name_by_proto_type_url() {
        final TypeUrl typeUrl = TypeUrl.from(EntityOption.Kind.getDescriptor());

        final ClassName className = KnownTypes.getClassName(typeUrl);

        assertEquals(ClassName.of(EntityOption.Kind.class), className);
    }

    @Test
    public void return_proto_type_url_by_java_class_name() {
        final ClassName className = ClassName.of(EntityOption.class);

        final TypeUrl typeUrl = KnownTypes.getTypeUrl(className);

        assertEquals(TypeUrl.from(EntityOption.getDescriptor()), typeUrl);
    }

    @Test
    public void return_proto_type_url_by_proto_type_name() {
        final TypeUrl typeUrlExpected = TypeUrl.from(StringValue.getDescriptor());

        final TypeUrl typeUrlActual = KnownTypes.getTypeUrl(typeUrlExpected.getTypeName());

        assertEquals(typeUrlExpected, typeUrlActual);
    }

    @Test
    public void return_all_types_under_certain_package() {
        final TypeUrl taskId = TypeUrl.from(TaskId.getDescriptor());
        final TypeUrl taskName = TypeUrl.from(TaskName.getDescriptor());
        final TypeUrl task = TypeUrl.from(Task.getDescriptor());

        final String packageName = "spine.test.types";

        final Collection<TypeUrl> packageTypes = getAllFromPackage(packageName);
        assertSize(3, packageTypes);
        assertTrue(packageTypes.containsAll(Arrays.asList(taskId, taskName, task)));
    }

    @Test
    public void return_empty_collection_if_package_is_empty_or_invalid() {
        final String packageName = "com.foo.invalid.package";
        final Collection<?> emptyTypesCollection = getAllFromPackage(packageName);
        assertNotNull(emptyTypesCollection);
        assertTrue(emptyTypesCollection.isEmpty());
    }

    @Test
    public void do_not_return_types_of_package_by_package_prefix() {
        final String prefix = "spine.test.ty"; // "spine.test.types" is a valid package

        final Collection<TypeUrl> packageTypes = getAllFromPackage(prefix);
        assertTrue(packageTypes.isEmpty());
    }

    @Test
    public void provide_proto_descriptor_by_type_name() {
        final String typeName = "spine.test.types.Task";
        final Descriptor typeDescriptor = (Descriptor) getDescriptor(typeName);
        assertNotNull(typeDescriptor);
        assertEquals(typeName, typeDescriptor.getFullName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_to_find_invalid_type_descriptor() {
        final String invalidTypeName = "no.such.package.InvalidType";
        getDescriptor(invalidTypeName);
    }

    @Test(expected = IllegalStateException.class)
    public void throw_exception_if_no_proto_type_url_by_java_class_name() {
        KnownTypes.getTypeUrl(ClassName.of(Exception.class));
    }

    @Test(expected = UnknownTypeException.class)
    public void throw_exception_if_no_java_class_name_by_type_url() {
        final TypeUrl unexpectedUrl = TypeUrl.parse("prefix/unexpected.type");
        KnownTypes.getClassName(unexpectedUrl);
    }
}
