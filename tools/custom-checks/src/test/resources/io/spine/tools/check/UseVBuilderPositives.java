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

package io.spine.tools.check;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import static com.google.protobuf.Int32Value.newBuilder;

class UseVBuilderPositives {

    StringValue stringValue = StringValue.getDefaultInstance();

    void callNewBuilder() {

        // BUG: Diagnostic matches: UseVBuilderError
        StringValue.newBuilder();
    }

    void callNewBuilderWithArg() {

        // BUG: Diagnostic matches: UseVBuilderError
        StringValue.newBuilder(stringValue);
    }

    void callNewBuilderForType() {

        // BUG: Diagnostic matches: UseVBuilderError
        stringValue.newBuilderForType();
    }

    void callToBuilder() {

        // BUG: Diagnostic matches: UseVBuilderError
        stringValue.toBuilder();
    }

    void callNewBuilderStaticImported() {

        // BUG: Diagnostic matches: UseVBuilderError
        newBuilder();
    }

    void callNewBuilderWithArgStaticImported() {
        Int32Value int32Value = Int32Value.getDefaultInstance();

        // BUG: Diagnostic matches: UseVBuilderError
        newBuilder(int32Value);
    }
}
