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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.OnDuplicate;
import io.spine.option.OptionsProto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.spine.option.OnDuplicate.IGNORE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * An option that can be applied to {@code repeated} Protobuf fields to specify that values
 * represented by that {@code repeated} field don't contain duplicates.
 */
final class DistinctFieldOption extends FieldValidatingOption<OnDuplicate> {

    private DistinctFieldOption(){
    }

    public static DistinctFieldOption distinctFieldOption(){
        return new DistinctFieldOption();
    }

    @Override
    boolean applicableTo(FieldDeclaration field) {
        return field.isRepeated();
    }

    @Override
    OptionInapplicableException onInapplicable(FieldDeclaration field) {
        String format = "Error for field %s. A non-repeated field cannot be `distinct`.";
        String exceptionText = format(format, field.name().value());
        return new OptionInapplicableException(exceptionText);
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue value) {
        return checkForDuplicates(value);
    }

    @Override
    boolean optionPresentAt(FieldValue value) {
        return valueFrom(value) != IGNORE;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static List<ConstraintViolation> checkForDuplicates(FieldValue value) {
        List<?> potentialDuplicates = new ArrayList<>(value.asList());
        Set<?> duplicates = findDuplicates(potentialDuplicates);
        return duplicates.isEmpty() ?
               ImmutableList.of() :
               ImmutableList.of(duplicateFound(value, duplicates));
    }

    private static Set<?> findDuplicates(Iterable<?> potentialDuplicates) { Set<Object> duplicateLess = new HashSet<>();
        ImmutableSet.Builder<Object> duplicates = ImmutableSet.builder();
        for (Object potentialDuplicate : potentialDuplicates) {
            if (duplicateLess.contains(potentialDuplicate)) {
                duplicates.add(potentialDuplicate);
            } else {
                duplicateLess.add(potentialDuplicate);
            }
        }
        return duplicates.build();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static <T> ConstraintViolation duplicateFound(FieldValue value, Set<T> duplicates) {
        String fieldName = value.declaration()
                                .name()
                                .value();
        List<String> stringValues = duplicates.stream()
                                              .map(Object::toString)
                                              .collect(toList());
        String formatParams = String.join(", ", stringValues);
        String msgFormat =
                "Found a duplicate element in a `distinct` field %s. " +
                        "Duplicate elements: " +
                        formatParams;
        ConstraintViolation.Builder duplicatesBuilder = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msgFormat)
                .addParam(fieldName);
        stringValues.forEach(duplicatesBuilder::addParam);
        return duplicatesBuilder.build();
    }

    @Override
    public OnDuplicate valueFrom(FieldValue fieldValue) {
        return fieldValue.valueOf(OptionsProto.onDuplicate);
    }
}
