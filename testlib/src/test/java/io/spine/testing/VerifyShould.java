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

package io.spine.testing;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.security.acl.AclNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.testing.Verify.assertBefore;
import static io.spine.testing.Verify.assertClassNonInstantiable;
import static io.spine.testing.Verify.assertContains;
import static io.spine.testing.Verify.assertContainsAll;
import static io.spine.testing.Verify.assertContainsEntry;
import static io.spine.testing.Verify.assertContainsKey;
import static io.spine.testing.Verify.assertContainsKeyValue;
import static io.spine.testing.Verify.assertEmpty;
import static io.spine.testing.Verify.assertEndsWith;
import static io.spine.testing.Verify.assertEqualsAndHashCode;
import static io.spine.testing.Verify.assertError;
import static io.spine.testing.Verify.assertInstanceOf;
import static io.spine.testing.Verify.assertItemAtIndex;
import static io.spine.testing.Verify.assertIterableEmpty;
import static io.spine.testing.Verify.assertIterableNotEmpty;
import static io.spine.testing.Verify.assertMapsEqual;
import static io.spine.testing.Verify.assertNegative;
import static io.spine.testing.Verify.assertNotContains;
import static io.spine.testing.Verify.assertNotContainsKey;
import static io.spine.testing.Verify.assertNotEmpty;
import static io.spine.testing.Verify.assertNotEquals;
import static io.spine.testing.Verify.assertNotInstanceOf;
import static io.spine.testing.Verify.assertPositive;
import static io.spine.testing.Verify.assertSetsEqual;
import static io.spine.testing.Verify.assertShallowClone;
import static io.spine.testing.Verify.assertSize;
import static io.spine.testing.Verify.assertStartsWith;
import static io.spine.testing.Verify.assertThrows;
import static io.spine.testing.Verify.assertThrowsWithCause;
import static io.spine.testing.Verify.assertZero;
import static io.spine.testing.Verify.denyContainsKey;
import static io.spine.testing.Verify.mangledException;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class VerifyShould {

    private static final String EMPTY_STRING = "";
    private static final String NON_EMPTY_STRING = "Non-empty string";

    private static final String MAP_NAME = Verify.Param.MAP;

    @Test
    public void extend_Assert_class() {
        Type expectedSuperclass = Assert.class;
        Type actualSuperclass = Verify.class.getGenericSuperclass();
        assertEquals(expectedSuperclass, actualSuperclass);
    }

    @Test
    public void has_private_ctor() {
        assertHasPrivateParameterlessCtor(Verify.class);
    }

    @SuppressWarnings({"ThrowCaughtLocally", "ErrorNotRethrown"})
    @Test
    public void mangle_assertion_error() {
        AssertionError sourceError = new AssertionError();
        int framesBefore = sourceError.getStackTrace().length;

        try {
            throw mangledException(sourceError);
        } catch (AssertionError e) {
            int framesAfter = e.getStackTrace().length;

            assertEquals(framesBefore - 1, framesAfter);
        }
    }

    @SuppressWarnings({"ThrowCaughtLocally", "ErrorNotRethrown"})
    @Test
    public void mangle_assertion_error_for_specified_frame_count() {
        AssertionError sourceError = new AssertionError();
        int framesBefore = sourceError.getStackTrace().length;
        int framesToPop = 3;

        try {
            throw mangledException(sourceError, framesToPop);
        } catch (AssertionError e) {
            int framesAfter = e.getStackTrace().length;

            assertEquals(framesBefore - framesToPop + 1, framesAfter);
        }
    }

    @SuppressWarnings("ErrorNotRethrown")
    @Test
    public void fail_with_specified_message_and_cause() {
        String message = "Test failed";
        Throwable cause = new Error();

        try {
            Verify.fail(message, cause);
            fail("Error was not thrown");
        } catch (AssertionError e) {
            assertEquals(message, e.getMessage());
            assertEquals(cause, e.getCause());
        }
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_float_values_are_positive_infinity() {
        float anyDeltaAcceptable = 0.0f;
        assertNotEquals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, anyDeltaAcceptable);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_float_values_are_negative_infinity() {
        float anyDeltaAcceptable = 0.0f;
        assertNotEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, anyDeltaAcceptable);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_float_values_are_NaN() {
        float anyDeltaAcceptable = 0.0f;
        assertNotEquals(Float.NaN, Float.NaN, anyDeltaAcceptable);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_float_values_are_equal() {
        float positiveValue = 5.0f;
        float negativeValue = -positiveValue;
        float equalToValuesDifference = positiveValue - negativeValue;
        assertNotEquals(positiveValue, negativeValue, equalToValuesDifference);
    }

    @Test
    public void pass_assertNotEquals_if_float_values_are_different_types_of_infinity() {
        float anyDeltaAcceptable = 0.0f;
        assertNotEquals(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, anyDeltaAcceptable);
        assertNotEquals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, anyDeltaAcceptable);
    }

    @Test
    public void pass_assertNotEquals_if_float_values_are_not_equal() {
        float expected = 0.0f;
        float actual = 1.0f;
        float lessThanValuesDifference = Math.abs(expected - actual) - 0.1f;
        assertNotEquals(expected, actual, lessThanValuesDifference);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_bool_values_are_equal() {
        assertNotEquals(true, true);
    }

    @Test
    public void pass_assertNotEquals_if_bool_values_are_not_equal() {
        assertNotEquals(true, false);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_byte_values_are_equal() {
        assertNotEquals((byte) 0, (byte) 0);
    }

    @Test
    public void pass_assertNotEquals_if_byte_values_are_not_equal() {
        assertNotEquals((byte) 0, (byte) 1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_char_values_are_equal() {
        assertNotEquals('a', 'a');
    }

    @Test
    public void pass_assertNotEquals_if_char_values_are_not_equal() {
        assertNotEquals('a', 'b');
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEquals_if_short_values_are_equal() {
        assertNotEquals((short) 0, (short) 0);
    }

    @Test
    public void pass_assertNotEquals_if_short_values_are_not_equal() {
        assertNotEquals((short) 0, (short) 1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertInstanceOf_if_object_is_not_instance_of_specified_type() {
        assertInstanceOf(Integer.class, EMPTY_STRING);
    }

    @Test
    public void pass_assertInstanceOf_if_object_is_instance_of_specified_type() {
        assertInstanceOf(EMPTY_STRING.getClass(), EMPTY_STRING);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotInstanceOf_if_object_is_instance_of_specified_type() {
        assertNotInstanceOf(EMPTY_STRING.getClass(), EMPTY_STRING);
    }

    @Test
    public void pass_assertNotInstanceOf_if_object_is_not_instance_of_specified_type() {
        assertNotInstanceOf(Integer.class, EMPTY_STRING);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertIterableEmpty_if_iterable_is_not_empty() {
        assertIterableEmpty(ImmutableList.of(1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertIterableEmpty_if_iterable_is_null_in_iterable_empty() {
        assertIterableEmpty(null);
    }

    @Test
    public void pass_assertIterableEmpty_if_iterable_is_empty() {
        assertIterableEmpty(ImmutableList.of());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEmpty_if_map_is_not_empty() {
        assertEmpty(Collections.singletonMap(1, 1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertEmpty_if_map_is_null_in_assert_empty() {
        assertEmpty((Map) null);
    }

    @Test
    public void pass_assertEmpty_if_map_is_empty() {
        assertEmpty(Collections.emptyMap());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEmpty_if_multimap_is_not_empty() {
        Multimap<Integer, Integer> multimap = ArrayListMultimap.create();

        multimap.put(1, 1);

        assertEmpty(multimap);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertEmpty_if_multimap_is_null_in_assert_empty() {
        assertEmpty((Multimap) null);
    }

    @Test
    public void pass_assertEmpty_if_multimap_is_empty() {
        assertEmpty(ArrayListMultimap.create());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_iterable_is_empty() {
        assertNotEmpty(ImmutableList.of());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertIterableNotEmpty_if_iterable_is_null_in_iterable_not_empty() {
        assertIterableNotEmpty(null);
    }

    @Test
    public void pass_assertNotEmpty_if_iterable_is_not_empty() {
        assertNotEmpty(ImmutableList.of(1));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_map_is_empty() {
        assertNotEmpty(Collections.emptyMap());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_map_is_null_in_not_empty() {
        assertNotEmpty((Map) null);
    }

    @Test
    public void pass_assertNotEmpty_if_map_is_not_empty() {
        assertNotEmpty(Collections.singletonMap(1, 1));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_multimap_is_empty() {
        assertNotEmpty(ArrayListMultimap.create());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_multimap_is_null_in_assert_not_empty() {
        assertNotEmpty((Multimap) null);
    }

    @Test
    public void pass_assertNotEmpty_if_multimap_is_not_empty() {
        Multimap<Integer, Integer> multimap = ArrayListMultimap.create();

        multimap.put(1, 1);

        assertNotEmpty(multimap);
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_array_is_empty() {
        assertNotEmpty(new Integer[0]);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotEmpty_if_array_is_null_in_assert_not_empty() {
        assertNotEmpty((Integer[]) null);
    }

    @Test
    public void pass_assertNotEmpty_if_array_is_not_empty() {
        Integer[] array = {1, 2, 3};
        assertNotEmpty(array);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_object_array_size_is_not_equal() {
        assertSize(0, new Object[1]);
    }

    @SuppressWarnings({"ConstantConditions", "OverlyStrongTypeCast"})
    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_array_is_null_in_assert_size() {
        assertSize(0, (Integer[]) null);
    }

    @Test
    public void pass_assertSize_if_object_array_size_is_equal() {
        int size = 0;
        assertSize(size, new Object[size]);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_iterable_size_is_not_equal() {
        assertSize(0, ImmutableList.of(1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_iterable_is_null_in_assert_size() {
        assertSize(0, (Iterable) null);
    }

    @Test
    public void pass_assertSize_if_iterable_size_is_equal() {
        assertSize(0, ImmutableList.of());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_map_size_is_not_equal() {
        assertSize(1, Collections.emptyMap());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_map_is_null_in_assert_size() {
        assertSize(0, (Map) null);
    }

    @Test
    public void pass_assertSize_if_map_size_is_equal() {
        assertSize(0, Collections.emptyMap());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_multimap_size_is_not_equal() {
        assertSize(1, ArrayListMultimap.create());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_multimap_is_null_in_assert_size() {
        assertSize(0, (Multimap) null);
    }

    @Test
    public void pass_assertSize_if_multimap_size_is_equal() {
        assertSize(0, ArrayListMultimap.create());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSize_if_collection_size_is_not_equal() {
        assertSize(1, Collections.emptyList());
    }

    @Test
    public void pass_assertSize_if_collection_size_is_equal() {
        assertSize(0, Collections.emptyList());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_string_not_contains_char_sequence() {
        assertContains(NON_EMPTY_STRING, EMPTY_STRING);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_char_sequence_is_null_in_contains() {
        assertContains(null, EMPTY_STRING);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_string_is_null_in_contains() {
        assertContains(EMPTY_STRING, (String) null);
    }

    @SuppressWarnings({"ConstantConditions", "ErrorNotRethrown"})
    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_contains_char_sequence_or_string_is_null() {
        String nullString = null;

        try {
            assertContains(null, EMPTY_STRING);
        } catch (AssertionError e) {
            assertContains(EMPTY_STRING, nullString);
        }
    }

    @Test
    public void pass_assertContains_if_string_contains_char_sequence() {
        assertContains(EMPTY_STRING, NON_EMPTY_STRING);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_string_contains_char_sequence() {
        assertNotContains(EMPTY_STRING, NON_EMPTY_STRING);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_char_sequence_is_null_in_not_contains() {
        assertNotContains(null, EMPTY_STRING);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_string_is_null_in_not_contains() {
        assertNotContains(EMPTY_STRING, (String) null);
    }

    @Test
    public void pass_assertNotContains_if_string_not_contains_char_sequence() {
        assertNotContains(NON_EMPTY_STRING, EMPTY_STRING);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_collection_not_contains_item() {
        assertContains(1, Collections.emptyList());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_collection_is_null_in_contains() {
        assertContains(1, (Collection) null);
    }

    @Test
    public void pass_assertContains_if_collection_contains_item() {
        Integer item = 1;
        assertContains(item, Collections.singletonList(item));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_immutable_collection_not_contains_item() {
        assertContains(1, ImmutableList.of());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContains_if_immutable_collections_is_null_in_contains() {
        assertContains(1, null);
    }

    @Test
    public void pass_assertContains_if_immutable_collection_contains_item() {
        Integer item = 1;
        assertContains(item, ImmutableList.of(item));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContainsAll_if_iterable_not_contains_all() {
        assertContainsAll(Collections.emptyList(), 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContainsAll_if_iterable_is_null_in_contains_all() {
        assertContainsAll(null);
    }

    @Test
    public void pass_assertContainsAll_if_iterable_contains_all() {
        Integer item = 1;
        assertContainsAll(Collections.singletonList(item), item);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertMapsEqual_if_map_are_not_equal() {
        Map<Integer, Map<Integer, Integer>> firstOne = singletonMap(1, singletonMap(1, 1));
        Map<Integer, Map<Integer, Integer>> secondOne = singletonMap(1, singletonMap(1, 2));

        assertMapsEqual(firstOne, secondOne, MAP_NAME);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void pass_assertMapsEqual_if_maps_are_null() {
        assertMapsEqual(null, null, MAP_NAME);
    }

    @Test
    public void pass_assertMapsEqual_if_maps_are_equal() {
        Map<Integer, Map<Integer, Integer>> firstOne = singletonMap(1, singletonMap(1, 1));
        Map<Integer, Map<Integer, Integer>> secondOne = new HashMap<>(firstOne);

        assertMapsEqual(firstOne, secondOne, MAP_NAME);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSetsEqual_if_sets_are_not_equal_by_size() {
        Set<Integer> firstOne = Sets.newHashSet(1, 2, 3, 4);
        Set<Integer> secondOne = Sets.newHashSet(1, 2, 4);

        assertSetsEqual(firstOne, secondOne);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSetsEqual_if_sets_are_not_equal_by_content() {
        Set<Integer> firstOne = Sets.newHashSet(1, 2, 3);
        Set<Integer> secondOne = Sets.newHashSet(1, 2, 777);

        assertSetsEqual(firstOne, secondOne);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertSetsEqual_if_sets_are_equal_by_size_but_have_over_5_differences() {
        Set<Integer> firstOne = Sets.newHashSet(1, 2, 3, 4, 5, 6);
        Set<Integer> secondOne = Sets.newHashSet(11, 12, 13, 14, 15, 16);

        assertSetsEqual(firstOne, secondOne);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void pass_assertSetsEqual_if_sets_are_null() {
        assertSetsEqual(null, null);
    }

    @Test
    public void pass_assertSetsEqual_if_sets_are_equal() {
        Set<Integer> firstOne = Sets.newHashSet(1, 2, 3);
        Set<Integer> secondOne = Sets.newHashSet(firstOne);

        assertSetsEqual(firstOne, secondOne);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContainsEntry_if_multimap_not_contains_entry() {
        assertContainsEntry(1, 1, ArrayListMultimap.create());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContainsEntry_if_multimap_is_null_in_contains_entry() {
        assertContainsEntry(1, 1, null);
    }

    @Test
    public void pass_assertContainsEntry_if_multimap_contains_entry() {
        Integer entryKey = 1;
        Integer entryValue = 1;

        Multimap<Integer, Integer> multimap = ArrayListMultimap.create();
        multimap.put(entryKey, entryValue);

        assertContainsEntry(entryKey, entryValue, multimap);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContainsKey_if_map_not_contains_key() {
        assertContainsKey(1, Collections.emptyMap());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContainsKey_if_map_is_null_in_contains_key() {
        assertContainsKey(1, null);
    }

    @Test
    public void pass_assertContainsKey_if_map_contains_key() {
        Integer key = 1;
        assertContainsKey(key, Collections.singletonMap(key, 1));
    }

    @Test(expected = AssertionError.class)
    public void fail_denyContainsKey_if_map_contains_denied_key() {
        Integer key = 1;
        denyContainsKey(key, Collections.singletonMap(key, 1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_denyContainsKey_if_map_is_null_in_deny_contains_key() {
        denyContainsKey(1, null);
    }

    @Test
    public void pass_denyContainsKey_if_map_not_contains_denied_key() {
        denyContainsKey(1, Collections.emptyMap());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertContainsKeyValue_if_map_not_contains_entry() {
        Integer key = 0;
        assertContainsKeyValue(key, 0, Collections.singletonMap(key, 1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertContainsKeyValue_if_map_is_null_in_contains_key_value() {
        assertContainsKeyValue(1, 1, null);
    }

    @Test
    public void pass_assertContainsKeyValue_if_map_contains_entry() {
        Integer key = 1;

        assertContainsKeyValue(key, EMPTY_STRING, Collections.singletonMap(key, EMPTY_STRING));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_collection_contains_item() {
        Integer item = 1;
        assertNotContains(item, Collections.singletonList(item));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_collection_is_null_in_not_contains() {
        assertNotContains(1, null);
    }

    @Test
    public void pass_assertNotContains_if_collection_not_contains_item() {
        assertNotContains(1, Collections.emptyList());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_iterable_contains_item() {
        Integer item = 1;
        assertNotContains(item, ImmutableList.of(item));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotContains_if_iterable_is_null_in_contains_item() {
        assertNotContains(1, (Iterable) null);
    }

    @Test
    public void pass_assertNotContains_if_iterable_not_contains_item() {
        assertNotContains(1, ImmutableList.of());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNotContainsKey_if_map_contains_key() {
        Integer key = 1;
        assertNotContainsKey(key, Collections.singletonMap(key, 1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertNotContainsKey_if_map_is_null_in_not_contains_key() {
        assertNotContainsKey(1, null);
    }

    @Test
    public void pass_assertNotContainsKey_if_map_not_contains_key() {
        assertNotContainsKey(1, Collections.emptyMap());
    }

    @Test(expected = AssertionError.class)
    public void fail_assertBefore_if_former_goes_after_latter_in_list() {
        Integer firstItem = 1;
        Integer secondItem = 2;

        List<Integer> list = Arrays.asList(firstItem, secondItem);

        assertBefore(secondItem, firstItem, list);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertBefore_if_former_and_latter_are_equal() {
        Integer sameItem = 1;
        assertBefore(sameItem, sameItem, Collections.singletonList(sameItem));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertBefore_if_list_is_null_in_assert_befor() {
        assertBefore(1, 2, null);
    }

    @Test
    public void pass_assertBefore_if_former_goes_before_latter_in_list() {
        Integer firstItem = 1;
        Integer secondItem = 2;

        List<Integer> list = Arrays.asList(firstItem, secondItem);

        assertBefore(firstItem, secondItem, list);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertItemAtIndex_if_list_item_not_at_index() {
        Integer firstItem = 1;
        Integer secondItem = 2;

        List<Integer> list = Arrays.asList(firstItem, secondItem);

        assertItemAtIndex(firstItem, list.indexOf(secondItem), list);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertItemAtIndex_if_list_is_null_in_assert_item_at_index() {
        assertItemAtIndex(1, 1, (List) null);
    }

    @Test
    public void pass_assertItemAtIndex_if_list_item_at_index() {
        Integer value = 1;
        List<Integer> list = Collections.singletonList(value);

        assertItemAtIndex(value, list.indexOf(value), list);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertItemAtIndex_if_array_item_not_at_index() {
        Integer firstItem = 1;
        Integer secondItem = 2;

        Object[] array = {firstItem, secondItem};

        assertItemAtIndex(firstItem, 1, array);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertItemAtIndex_if_array_is_null_in_assert_item_at_index() {
        assertItemAtIndex(1, 1, (Object[]) null);
    }

    @Test
    public void pass_assertItemAtIndex_if_array_item_at_index() {
        Integer value = 1;
        Object[] array = {value};

        assertItemAtIndex(value, 0, array);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_array_not_starts_with_items() {
        Integer[] array = {1, 2, 3};
        Integer notStartsWith = 777;

        assertStartsWith(array, notStartsWith);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_array_is_null_in_starts_with() {
        assertStartsWith((Integer[]) null, 1, 2, 3);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_items_is_empty_in_array_starts_with() {
        assertStartsWith(new Integer[1]);
    }

    @Test
    public void pass_assertStartsWith_if_array_starts_with_items() {
        Integer[] array = {1, 2};
        Integer firstItem = array[0];

        assertStartsWith(array, firstItem);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_list_not_starts_with_items() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Integer notStartsWith = 777;

        assertStartsWith(list, notStartsWith);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_list_is_null_in_starts_with() {
        assertStartsWith((List<Integer>) null, 1, 2, 3);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertStartsWith_if_items_is_empty_in_list_starts_with() {
        assertStartsWith(Collections.emptyList());
    }

    @Test
    public void pass_assertStartsWith_if_list_starts_with_items() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Integer firstItem = list.get(0);

        assertStartsWith(list, firstItem);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_list_not_ends_with_items() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Integer notEndsWith = 777;

        assertEndsWith(list, notEndsWith);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_list_is_null_in_ends_with() {
        assertEndsWith((List<Integer>) null, 1, 2, 3);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_items_is_empty_in_list_ends_with() {
        assertEndsWith(Collections.emptyList());
    }

    @Test
    public void pass_assertEndsWith_if_list_ends_with_items() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Integer lastItem = list.get(list.size() - 1);

        assertEndsWith(list, lastItem);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_array_not_ends_with_items() {
        Integer[] array = {1, 2, 3};
        Integer notEndsWith = 777;

        assertEndsWith(array, notEndsWith);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_array_is_null_in_ends_with() {
        assertEndsWith((Integer[]) null, 1, 2, 3);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEndsWith_if_items_is_empty_in_array_ends_with() {
        assertEndsWith(new Integer[1]);
    }

    @Test
    public void pass_assertEndsWith_if_array_ends_with() {
        Integer[] array = {1, 2, 3};
        Integer lastItem = array[array.length - 1];

        assertEndsWith(array, lastItem);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEqualsAndHashCode_if_objects_are_not_equal() {
        assertEqualsAndHashCode(1, 2);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertEqualsAndHashCode_if_objects_are_equal_but_hash_codes_are_not_equal() {
        ClassThatViolateHashCodeAndCloneableContract objectA =
                new ClassThatViolateHashCodeAndCloneableContract(1);
        ClassThatViolateHashCodeAndCloneableContract objectB =
                new ClassThatViolateHashCodeAndCloneableContract(1);

        assertEqualsAndHashCode(objectA, objectB);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertEqualsAndHashCode_if_objects_are_null() {
        assertEqualsAndHashCode(null, null);
    }

    @Test
    public void pass_assertEqualsAndHashCode_if_objects_and_their_hash_codes_are_equal() {
        assertEqualsAndHashCode(1, 1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNegative_if_value_is_not_negative() {
        assertNegative(1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertNegative_if_value_is_zero_in_assert_negative() {
        assertNegative(0);
    }

    @Test
    public void pass_assertNegative_if_value_is_negative() {
        assertNegative(-1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertPositive_if_value_is_not_positive() {
        assertPositive(-1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertPositive_if_value_is_zero_in_assert_positive() {
        assertPositive(0);
    }

    @Test
    public void pass_assertPositive_if_value_is_positive() {
        assertPositive(1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertZero_if_value_is_positive() {
        assertZero(1);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertZero_if_value_is_negative() {
        assertZero(-1);
    }

    @Test
    public void pass_assertZero_if_value_is_zero() {
        assertZero(0);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertShallowClone_if_clone_returns_same_object() {
        assertShallowClone(new ClassThatViolateHashCodeAndCloneableContract(1));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertShallowClone_if_clone_does_not_work_correctly() {
        assertShallowClone(new ClassThatImplementCloneableIncorrectly(1));
    }

    @Test
    public void pass_assertShallowClone_if_cloneable_equals_and_hash_code_overridden_correctly() {
        assertShallowClone(new ClassThatImplementCloneableCorrectly(1));
    }

    @Test(expected = AssertionError.class)
    public void fail_assertClassNonInstantiable_if_class_instantiable() {
        assertClassNonInstantiable(Object.class);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertClassNonInstantiable_if_class_instantiable_through_reflection() {
        assertClassNonInstantiable(ClassWithPrivateCtor.class);
    }

    @Test
    public void pass_assertClassNonInstantiable_if_new_instance_throw_instantiable_exception() {
        assertClassNonInstantiable(void.class);
    }

    @Test
    public void pass_assertClassNonInstantiable_if_class_non_instantiable_through_reflection() {
        assertClassNonInstantiable(ClassThatThrowExceptionInConstructor.class);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertError_if_runnable_not_throws_error() {
        Runnable notThrowsException = () -> {};

        assertError(AssertionError.class, notThrowsException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertError_if_runnable_not_throws_specified_error() {
        Runnable throwsAssertionError = () -> {
            throw new AssertionError();
        };

        assertError(Error.class, throwsAssertionError);
    }

    @Test
    public void pass_assertError_if_runnable_throws_specified_error() {
        Runnable throwsAssertionError = () -> {
            throw new AssertionError();
        };

        assertError(AssertionError.class, throwsAssertionError);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrows_if_callable_not_throws_exception() {
        Callable notThrowsException = () -> null;

        assertThrows(Exception.class, notThrowsException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrows_if_callable_not_throws_specified_exception() {
        Callable throwsEmptyStackException = () -> {
            throw new EmptyStackException();
        };

        assertThrows(Exception.class, throwsEmptyStackException);
    }

    @Test
    public void pass_assertThrows_if_callable_throws_specified_exception() {
        Callable throwsEmptyStackException = () -> {
            throw new EmptyStackException();
        };

        assertThrows(EmptyStackException.class, throwsEmptyStackException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrows_if_runnable_not_throws_exception() {
        Runnable notThrowsException = () -> {};

        assertThrows(Exception.class, notThrowsException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrows_if_runnable_not_throws_specified_exception() {
        Runnable throwsEmptyStackException = () -> {
            throw new EmptyStackException();
        };

        assertThrows(Exception.class, throwsEmptyStackException);
    }

    @Test
    public void pass_assertThrows_if_runnable_throws_specified_exception() {
        Runnable throwsEmptyStackException = () -> {
            throw new EmptyStackException();
        };

        assertThrows(EmptyStackException.class, throwsEmptyStackException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_callable_not_throws_exception_with_cause() {
        Callable notThrowsException = () -> null;

        assertThrowsWithCause(Exception.class, Exception.class, notThrowsException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_callable_throws_exception_with_different_causes() {
        Throwable expectedCause = new EmptyStackException();
        Throwable actualCause = new AclNotFoundException();
        RuntimeException runtimeException = new RuntimeException(actualCause);
        Callable throwsRuntimeException = () -> {
            throw runtimeException;
        };

        assertThrowsWithCause(runtimeException.getClass(), expectedCause.getClass(),
                                     throwsRuntimeException);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_callable_expected_cause_is_null() {
        Callable throwsRuntimeException = () -> {
            throw new RuntimeException(new EmptyStackException());
        };

        assertThrowsWithCause(EmptyStackException.class, null,
                                     throwsRuntimeException);
    }

    @Test
    public void pass_assertThrowsWithCause_if_callable_throws_specified_exception_with_specified_cause() {
        Throwable cause = new EmptyStackException();
        RuntimeException runtimeException = new RuntimeException(cause);
        Callable throwsRuntimeException = () -> {
            throw runtimeException;
        };

        assertThrowsWithCause(runtimeException.getClass(), cause.getClass(),
                                     throwsRuntimeException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_runnable_not_throws_exception_with_cause() {
        Runnable notThrowsException = () -> {
        };

        assertThrowsWithCause(Exception.class, Exception.class, notThrowsException);
    }

    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_runnable_throws_exception_with_different_causes() {
        Throwable expectedCause = new EmptyStackException();
        Throwable actualCause = new AclNotFoundException();
        RuntimeException runtimeException = new RuntimeException(actualCause);
        Runnable throwsRuntimeException = () -> {
            throw runtimeException;
        };

        assertThrowsWithCause(runtimeException.getClass(), expectedCause.getClass(),
                                     throwsRuntimeException);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void fail_assertThrowsWithCause_if_runnable_expected_cause_is_null() {
        Runnable throwsRuntimeException = () -> {
            throw new RuntimeException(new EmptyStackException());
        };

        assertThrowsWithCause(EmptyStackException.class, null,
                                     throwsRuntimeException);
    }

    @Test
    public void pass_assertThrowsWithCause_if_runnable_throws_specified_exception_with_specified_cause() {
        Throwable cause = new EmptyStackException();
        RuntimeException runtimeException = new RuntimeException(cause);
        Runnable throwsRuntimeException = () -> {
            throw runtimeException;
        };

        assertThrowsWithCause(runtimeException.getClass(), cause.getClass(),
                                     throwsRuntimeException);
    }

    @SuppressWarnings({"EqualsAndHashcode", "EqualsHashCode"})
        // Not having hashCode() is a part of this test. environment class.
    private static class ClassThatViolateHashCodeAndCloneableContract implements Cloneable {
        private final int value;

        private ClassThatViolateHashCodeAndCloneableContract(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassThatViolateHashCodeAndCloneableContract that =
                    (ClassThatViolateHashCodeAndCloneableContract) o;

            return value == that.value;
        }

        @SuppressWarnings({"MethodDoesntCallSuperMethod", "RedundantThrows"})
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }
    }

    private static class ClassThatImplementCloneableCorrectly implements Cloneable {
        private final int value;

        private ClassThatImplementCloneableCorrectly(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassThatImplementCloneableCorrectly that = (ClassThatImplementCloneableCorrectly) o;

            return value == that.value;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    private static final class ClassThatImplementCloneableIncorrectly implements Cloneable {
        private final int value;

        private ClassThatImplementCloneableIncorrectly(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassThatImplementCloneableIncorrectly that = (ClassThatImplementCloneableIncorrectly) o;

            return value == that.value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @SuppressWarnings({"RedundantThrows", "MethodDoesntCallSuperMethod"})
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return new ClassThatImplementCloneableIncorrectly(value + 1);
        }
    }

    private static class ClassWithPrivateCtor {
        @SuppressWarnings("RedundantNoArgConstructor")
        private ClassWithPrivateCtor() {
        }
    }

    private static class ClassThatThrowExceptionInConstructor {
        private ClassThatThrowExceptionInConstructor() {
            throw new AssertionError("This is non-instantiable class");
        }
    }
}
