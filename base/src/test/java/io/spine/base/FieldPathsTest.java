/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.base;

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Empty;
import io.spine.test.protobuf.AnyHolder;
import io.spine.test.protobuf.GenericHolder;
import io.spine.test.protobuf.StringHolder;
import io.spine.test.protobuf.StringHolderHolder;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.protobuf.AnyPacker.pack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")// testing deprecated methods
@DisplayName("FieldPaths should")
class FieldPathsTest extends UtilityClassTest<FieldPaths> {

    FieldPathsTest() {
        super(FieldPaths.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(FieldPath.class, FieldPath.getDefaultInstance())
              .setDefault(Descriptor.class, Any.getDescriptor());
    }

    @Test
    @DisplayName("parse simple path")
    void parseSimple() {
        String path = "val";
        FieldPath parsed = FieldPaths.parse(path);
        assertEquals(1 , parsed.getFieldNameCount());
        assertEquals(path , parsed.getFieldName(0));
    }

    @Test
    @DisplayName("parse long field paths")
    void parseLong() {
        String path = "holder_holder.holder.val";
        FieldPath parsed = FieldPaths.parse(path);
        assertThat(parsed.getFieldNameList()).containsExactly("holder_holder", "holder", "val");
    }

    @Test
    @DisplayName("not parse empty")
    void failToParseEmpty() {
        assertThrows(IllegalArgumentException.class, () -> FieldPaths.parse(""));
    }

    @Test
    @DisplayName("obtain value at a simple path")
    void obtainSimple() {
        StringHolder holder = StringHolder
                .newBuilder()
                .setVal("foobar")
                .build();
        FieldPath path = FieldPaths.parse("val");
        Object result = FieldPaths.getValue(path, holder);
        assertEquals(holder.getVal(), result);
    }

    @Test
    @DisplayName("obtain value at a complex path")
    void obtainComplex() {
        Any value = pack(Time.currentTime());
        AnyHolder anyHolder = AnyHolder
                .newBuilder()
                .setVal(value)
                .build();
        GenericHolder anyHolderHolder = GenericHolder
                .newBuilder()
                .setAny(anyHolder)
                .build();
        FieldPath pathToTypeUrl = FieldPaths.parse("any.val.type_url");
        Object actual = FieldPaths.getValue(pathToTypeUrl, anyHolderHolder);
        assertEquals(value.getTypeUrl(), actual);
    }

    @Test
    @DisplayName("obtain value at a recursive path")
    void obtainRecursive() {
        String value = "42";
        StringHolder holder0 = StringHolder
                .newBuilder()
                .setVal(value)
                .build();
        StringHolderHolder holder1 = StringHolderHolder
                .newBuilder()
                .setHolder(holder0)
                .build();
        GenericHolder holder2 = GenericHolder
                .newBuilder()
                .setHolderHolder(holder1)
                .build();
        GenericHolder holder3 = GenericHolder
                .newBuilder()
                .setGeneric(holder2)
                .build();
        FieldPath path = FieldPaths.parse("generic.holder_holder.holder.val");
        Object actual = FieldPaths.getValue(path, holder3);
        assertEquals(value, actual);
    }

    @Test
    @DisplayName("fail when trying to obtain a value at an empty path")
    void notAllowEmptyPaths() {
        assertThrows(IllegalArgumentException.class,
                     () -> FieldPaths.getValue(FieldPath.getDefaultInstance(), Empty.getDefaultInstance()));
        assertThrows(IllegalArgumentException.class,
                     () -> FieldPaths.typeOfFieldAt(Empty.class, FieldPath.getDefaultInstance()));
    }

    @Test
    @DisplayName("fail if the path contains a typo")
    void failOnTypo() {
        String value = "hello";
        StringHolder holder = StringHolder
                .newBuilder()
                .setVal(value)
                .build();
        FieldPath wrongPath = FieldPaths.parse("wrong_field_name");
        assertThrows(IllegalArgumentException.class, () -> FieldPaths.getValue(wrongPath, holder));
    }

    @Test
    @DisplayName("fail if the path reaches over a primitive value")
    void failOnMissingField() {
        String value = "primitive value";
        StringHolder holder = StringHolder
                .newBuilder()
                .setVal(value)
                .build();
        FieldPath wrongPath = FieldPaths.parse("val.this_field_is_absent");
        assertThrows(IllegalArgumentException.class, () -> FieldPaths.getValue(wrongPath, holder));
    }

    @Test
    @DisplayName("obtain a field type")
    void findTypeByPath() {
        FieldPath path = FieldPaths.parse("holder_holder.holder");
        assertEquals(StringHolder.class, FieldPaths.typeOfFieldAt(GenericHolder.class, path));
    }

    @Test
    @DisplayName("fail if the type lookup reaches a primitive value")
    void failOnMissingFieldInTypeLookup() {
        FieldPath wrongPath = FieldPaths.parse("val.non_existing_field");
        assertThrows(IllegalArgumentException.class,
                     () -> FieldPaths.typeOfFieldAt(StringHolder.class, wrongPath));
    }

    @Test
    @DisplayName("fail if the path to find a type by contains a typo")
    void failOnTypoInFieldPathInTypeLookup() {
        FieldPath wrongPath = FieldPaths.parse("generic.hldr");
        assertThrows(IllegalArgumentException.class,
                     () -> FieldPaths.typeOfFieldAt(GenericHolder.class, wrongPath));
    }

    @Test
    @DisplayName("lookup recursive types")
    void recursiveTypeLookup() {
        FieldPath path = FieldPaths.parse("generic.generic.generic.generic.generic");
        assertEquals(GenericHolder.class, FieldPaths.typeOfFieldAt(GenericHolder.class, path));
    }
}
