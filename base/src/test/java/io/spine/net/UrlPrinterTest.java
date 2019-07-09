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

package io.spine.net;

import io.spine.net.Uri.Authorization;
import io.spine.net.Uri.Protocol;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.net.UrlPrinter.printToString;

@SuppressWarnings("CheckReturnValue") // Calling builder methods.
@DisplayName("UrlPrinter should")
class UrlPrinterTest extends UtilityClassTest<UrlPrinter> {

    private static final String HOST = "spine.io";

    private static final Authorization AUTH =
            Authorization.newBuilder()
                         .setUserName("admin")
                         .setPassword("root")
                         .build();

    private static final Uri FULL_RECORD =
            Uri.newBuilder()
               .setHost(HOST)
               .setPort("80")
               .setProtocol(Protocol.newBuilder()
                                          .setSchema(Uri.Schema.HTTP))
               .setAuth(AUTH)
               .setPath("index")
               .addQuery(UrlQueryParameters.parse("key=value"))
               .addQuery(UrlQueryParameters.parse("key2=value2"))
               .setFragment("frag1")
               .build();

    UrlPrinterTest() {
        super(UrlPrinter.class);
    }

    @SuppressWarnings("UnusedMethod") // used via annotation `@MethodSource`.
    private static Stream<Arguments> recordAndResult() {
        return Stream.of(
                Arguments.of(
                        FULL_RECORD,
                        "http://admin:root@spine.io:80/index?key=value&key2=value2#frag1"
                ),
                Arguments.of(
                        Uri.newBuilder()
                           .setHost(HOST)
                           .build(),
                        HOST
                ),
                Arguments.of(
                        Uri.newBuilder(FULL_RECORD)
                           .setAuth(Authorization.newBuilder(AUTH)
                                                       .setPassword("")
                                                       .build())
                           .build(),
                        "http://admin@spine.io:80/index?key=value&key2=value2#frag1"
                ),

                Arguments.of(
                        Uri.newBuilder(FULL_RECORD)
                           .setAuth(Authorization.newBuilder(AUTH)
                                                       .setUserName("")
                                                       .build())
                           .build(),
                        // As UrlPrinter assumes that we have already validated url,
                        // it just ignores password if user is not set.
                        "http://spine.io:80/index?key=value&key2=value2#frag1"
                ),

                Arguments.of(
                        Uri.newBuilder()
                           .setHost(HOST)
                           .setProtocol(Protocol.newBuilder()
                                                      .setName("custom")
                                                      .build())
                           .build(),
                        "custom://" + HOST
                )
        );
    }

    @ParameterizedTest
    @MethodSource("recordAndResult")
    void verifyPrinting(Uri record, String expectedOutput) {
        String str = printToString(record);
        assertThat(str).isEqualTo(expectedOutput);
    }
}
