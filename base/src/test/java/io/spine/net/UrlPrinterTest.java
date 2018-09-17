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

package io.spine.net;

import io.spine.net.UrlRecord.Authorization;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.net.UrlPrinter.printToString;

/**
 * Tests of {@link io.spine.net.UrlPrinter}.
 *
 * @author Mikhail Mikhaylov
 */
/* when we call the builder methods. */
@SuppressWarnings("CheckReturnValue")
class UrlPrinterTest extends UtilityClassTest<UrlPrinter> {

    private static final Authorization AUTH =
            Authorization.newBuilder()
                         .setUserName("admin")
                         .setPassword("root")
                         .build();

    private static final String HOST = "spine.io";

    private static final UrlRecord FULL_RECORD =
            UrlRecord.newBuilder()
                  .setHost(HOST)
                  .setPort("80")
                  .setProtocol(UrlRecord.Protocol.newBuilder()
                                              .setSchema(UrlRecord.Schema.HTTP))
                  .setAuth(AUTH)
                  .setPath("index")
                  .addQuery(UrlQueryParameters.parse("key=value"))
                  .addQuery(UrlQueryParameters.parse("key2=value2"))
                  .setFragment("frag1")
                  .build();

    UrlPrinterTest() {
        super(UrlPrinter.class);
    }

    private static Stream<Arguments> recordAndResult() {
        return Stream.of(
                Arguments.of(FULL_RECORD,
                             "http://admin:root@spine.io:80/index?key=value&key2=value2#frag1"),
                Arguments.of(UrlRecord.newBuilder()
                                      .setHost(HOST)
                                      .build(),
                             HOST)
        );
    }

    @ParameterizedTest
    @MethodSource("recordAndResult")
    void verifyPrinting(UrlRecord record, String output) {
        assertThat(printToString(record)).isEqualTo(output);
    }

    @Test
    void print_valid_url() {
        assertThat(printToString(FULL_RECORD))
                .isEqualTo("http://admin:root@spine.io:80/index?key=value&key2=value2#frag1");
    }

    @Test
    void print_empty_url() {
        UrlRecord record = UrlRecord
                .newBuilder()
                .setHost(HOST)
                .build();

        assertThat(printToString(record))
                .isEqualTo(HOST);
    }

    @Test
    void print_url_without_password() {

        UrlRecord record = UrlRecord
                .newBuilder(FULL_RECORD)
                .setAuth(Authorization.newBuilder(AUTH)
                                      .setPassword("")
                                      .build())
                .build();

        assertThat(printToString(record))
                .isEqualTo("http://admin@spine.io:80/index?key=value&key2=value2#frag1");
    }

    @Test
    void print_url_with_broken_auth() {
        UrlRecord record = UrlRecord
                .newBuilder(FULL_RECORD)
                .setAuth(Authorization.newBuilder(AUTH)
                                      .setUserName("")
                                      .build())
                .build();

        // As UrlPrinter assumes that we have already validated url,
        // it just ignores password if user is not set.
        assertThat(printToString(record))
                .isEqualTo("http://spine.io:80/index?key=value&key2=value2#frag1");
    }

    @Test
    void print_url_with_custom_protocol() {
        UrlRecord.Protocol protocol = UrlRecord.Protocol
                .newBuilder()
                .setName("custom")
                .build();
        UrlRecord record = UrlRecord
                .newBuilder()
                .setHost(HOST)
                .setProtocol(protocol)
                .build();

        assertThat(printToString(record))
                .isEqualTo("custom://" + HOST);
    }
}
