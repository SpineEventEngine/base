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

package io.spine.tools.codestyle.javadoc;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.invoke.WrongMethodTypeException;


/**
 * <a href="https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#google.protobuf.FieldMask">FieldMask specs</a>.
 */
class AllowedFqnFormats {
    /**
      * {@link io.spine.server.event.EventBus EventBus}
      * {@linkplain io.spine.server.event.EventBus EventBus}
      * {@link io.spine.this.is.a.very.long.package.name.to.test.YourPlugin YourPlugin}
      * {@linkplain io.spine.this.is.a.very.long.package.name.to.test.YourPlugin YourPlugin}
      * {@link this.is.few.excessive.Spaces             Spaces}
      * {@link This.Iss}
      */
    public static void SomeVeryLongnameForaTestMethod(WrongMethodTypeException exception, AnnotationTypeMismatchException annotation) {

    }
}
