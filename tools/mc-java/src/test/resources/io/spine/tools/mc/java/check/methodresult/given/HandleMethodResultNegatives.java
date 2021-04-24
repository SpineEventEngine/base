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

package io.spine.tools.mc.java.check.vbuild.given;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import io.spine.base.FieldPath;

/**
 * Contains statements for which the {@link HandleMethodResult} bug pattern should
 * generate no warning.
 */
abstract class HandleMethodResultNegatives {

    Timestamp callSet() {
        Timestamp.Builder builder = Timestamp.newBuilder();
        builder.setNanos(42);
        return builder.build();
    }

    FieldPath callAdd() {
        FieldPath.Builder builder = FieldPath.newBuilder();
        builder.addFieldName("foo")
               .addFieldName("bar");
        return builder.vBuild();
    }

    Struct callPut() {
        Struct.Builder builder = Struct.newBuilder();
        builder.putFields("foo", Value.getDefaultInstance());
        return builder.build();
    }

    Struct callMerge() {
        Struct.Builder builder = Struct.newBuilder();
        builder.mergeFrom(Struct.getDefaultInstance());
        return builder.build();
    }

    Struct callRemove() {
        Struct.Builder builder = Struct.newBuilder();
        builder.removeFields("foo");
        builder.removeFields("bar");
        return builder.build();
    }

    @SuppressWarnings("HandleMethodResult")
    void callUnderWarningSuppressed() {
        checkMe();
    }

    @SuppressWarnings("CheckReturnValue")
    void suppressParentCheck() {
        checkMe();
    }

    void callIgnorableMethod() {
        dontCheckMe();
    }

    @CanIgnoreReturnValue
    public String dontCheckMe() {
        return "no";
    }

    public String checkMe() {
        return "42";
    }
}
