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

package io.spine.validate;

import com.google.protobuf.Descriptors.FieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;

/**
 * Validates fields of type {@link Boolean}.
 *
 * @author Dmitry Kashcheiev
 */
class BooleanFieldValidator extends FieldValidator<Boolean> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldPathDescriptors a field path in descriptors form to the field
     * @param fieldValues          values to validate
     */
    BooleanFieldValidator(Deque<FieldDescriptor> fieldPathDescriptors, Object fieldValues) {
        super(fieldPathDescriptors, FieldValidator.<Boolean>toValueList(fieldValues), false);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    /**
     * In Protobuf there is no way to tell if the value is {@code false} or was not set.
     *
     * @return false
     */
    @Override
    protected boolean isValueNotSet(Boolean value) {
        return false;
    }

    @Override
    protected void validateOwnRules() {
        if (isRequiredField()) {
            log().warn("'required' option not allowed for boolean field");
        }
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(BooleanFieldValidator.class);
    }
}
