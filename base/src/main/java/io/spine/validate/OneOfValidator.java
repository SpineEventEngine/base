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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates fields declared inside {@linkplain OneofDescriptor OneOf}.
 *
 * //TODO:2018-10-26:dmytro.grankin:  extent Javadoc
 */
class OneOfValidator {

    private final OneofDescriptor oneOf;
    private final Message message;

    OneOfValidator(OneofDescriptor oneOf, Message message) {
        this.oneOf = checkNotNull(oneOf);
        this.message = checkNotNull(message);
    }

    ImmutableList<ConstraintViolation> validate() {
        ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();
        Optional<FieldDescriptor> populatedField = populatedField();
        if (!populatedField.isPresent()) {
            violations.add(noneFieldIsSet());
        } else {
            List<ConstraintViolation> fieldViolations = validateField(populatedField.get());
            violations.addAll(fieldViolations);
        }
        return violations.build();
    }

    private List<ConstraintViolation> validateField(FieldDescriptor field) {
        //TODO:2018-10-26:dmytro.grankin: implement
        return Collections.emptyList();
    }

    /**
     * Obtains the descriptor of the {@code OneOf} field, which was set.
     *
     * @return the populated field descriptor or {@code Optional.empty()} if none is populated
     */
    private Optional<FieldDescriptor> populatedField() {
        FieldDescriptor field = message.getOneofFieldDescriptor(oneOf);
        return Optional.ofNullable(field);
    }

    private ConstraintViolation noneFieldIsSet() {
        ConstraintViolation requiredFieldNotFound =
                ConstraintViolation.newBuilder()
                                   .setMsgFormat("None of the %s OneOf fields is set.")
                                   .addParam(oneOf.getName())
                                   .build();
        return requiredFieldNotFound;
    }
}
