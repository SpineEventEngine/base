/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.mc.java.code.check.vbuild.given;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Empty;
import io.spine.base.FieldPath;
import io.spine.validate.ValidatingBuilder;

import java.util.function.Supplier;

/**
 * Contains statements for which the {@link UseVBuild} bug pattern should
 * generate no warning.
 */
abstract class UseVBuildNegatives {

    /** This method calls the generated vBuild() method. */
    void callOnVBuilder() {
        FieldPath.newBuilder()
                 .vBuild();
    }

    /** This method is annotated suppressing the warning. */
    @SuppressWarnings("UseVBuild")
    void callUnderWarningSuppressed() {
        FieldPath.newBuilder()
                 .build();
    }

    /** This method calls buildPartial() to explititly state that the message is not validated. */
    void callBuildPartial() {
        FieldPath.newBuilder()
                 .buildPartial();
    }

    abstract class SomeBuilder implements ValidatingBuilder<Empty> {

        /** The call to builder is made inside a builder class. */
        void callInsideBuilder() {
            FieldPath.newBuilder()
                     .build();
        }

        void useMethodRefInsimeBuilder() {
            Supplier<?> sup = FieldPath.newBuilder()::build;
            sup.get();
        }

        /** Added to satisfy the compiler. Does not affect the ErrorProne checks. */
        @Override
        public abstract SomeBuilder clone();
    }

    abstract class SomeMessage extends AbstractMessage {

        /** The call to builder is made inside a message class. */
        void callInsideMessage() {
            FieldPath.newBuilder()
                     .build();
        }

        void useMethodRefInsimeMessage() {
            Supplier<?> sup = FieldPath.newBuilder()::build;
            sup.get();
        }
    }
}
