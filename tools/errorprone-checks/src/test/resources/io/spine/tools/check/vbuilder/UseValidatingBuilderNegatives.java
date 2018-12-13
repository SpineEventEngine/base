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

package io.spine.tools.check.vbuilder;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.StringValue;
import io.spine.base.FieldPathVBuilder;
import io.spine.validate.AbstractValidatingBuilder;

import static io.spine.base.ErrorVBuilder.newBuilder;

/**
 * Contains statements for which the {@link UseValidatingBuilder} bug pattern should
 * generate no warning.
 */
abstract class UseValidatingBuilderNegatives {

    /** This method calls generated VBuilder. */
    void callOnVBuilder() {
        FieldPathVBuilder.newBuilder();
    }

    /** This method calls statically imported method of generated VBuilder. */
    void callOnVBuilderStaticImported() {
        newBuilder();
    }

    /** This method is annotated suppressing the warning. */
    @SuppressWarnings("UseValidatingBuilder")
    void callUnderWarningSuppressed() {
        StringValue.newBuilder();
    }

    class SomeBuilder extends AbstractValidatingBuilder {

        /**
         * This method contains a call from a method, which is inside a class derived
         * from {@code AbstractValidatingBuilder}.
         */
        void callInsideVBuilder() {
            StringValue.newBuilder();
        }
    }

    abstract class SomeMessage extends AbstractMessage {

        /** The call to builder is made inside a message class. */
        void callInsideMessage() {
            StringValue.newBuilder();
        }
    }
}
