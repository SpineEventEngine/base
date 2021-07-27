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

package io.spine.net;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link io.spine.net.UrlParser}.
 */
@DisplayName("UrlParser should parse")
class UrlParserTest {

    private static final String HOST = "ulr-parser-should.com";
    private static final String HTTP_PROTOCOL = "http";
    private static final String UNKNOWN_PROTOCOL = "http5";
    private static final String PROTOCOL_HOST = HTTP_PROTOCOL + "://" + HOST;
    private static final String UNKNOWN_PROTOCOL_HOST = UNKNOWN_PROTOCOL + "://" + HOST;
    private static final String PORT = "8080";

    @Test
    @DisplayName("protocol and host")
    void protocolAndHost() {
        Uri record = new UrlParser(PROTOCOL_HOST).parse();
        assertEquals(HOST, record.getHost());
        assertEquals(Uri.Schema.HTTP, record.getProtocol()
                                            .getSchema());
    }

    @Test
    @DisplayName("host")
    void host() {
        Uri record = new UrlParser(HOST).parse();

        assertEquals(HOST, record.getHost());
        assertEquals(Uri.Schema.UNDEFINED, record.getProtocol()
                                                 .getSchema());
    }

    @Test
    @DisplayName("unknown protocol")
    void unknownProtocol() {
        Uri record = new UrlParser(UNKNOWN_PROTOCOL_HOST).parse();
        assertEquals(UNKNOWN_PROTOCOL, record.getProtocol()
                                             .getName());
    }

    @Test
    @DisplayName("credentials")
    void credentials() {
        String userName = "admin";
        String password = "root";

        String userUrl = HTTP_PROTOCOL + "://" + userName + '@' + HOST;
        String userPasswordUrl = HTTP_PROTOCOL + "://" + userName + ':' +
                password + '@' + HOST;

        Uri record1 = new UrlParser(userUrl).parse();
        String user1 = record1.getAuth()
                              .getUserName();
        assertEquals(userName, user1);

        Uri record2 = new UrlParser(userPasswordUrl).parse();
        Uri.Authorization auth2 = record2.getAuth();
        String user2 = auth2.getUserName();
        assertEquals(userName, user2);
        assertEquals(password, auth2.getPassword());
    }

    @Test
    @DisplayName("port")
    void port() {
        String url = HOST + ':' + PORT;

        Uri parsedUrl = new UrlParser(url).parse();

        assertEquals(PORT, parsedUrl.getPort());
    }

    @Test
    @DisplayName("path")
    void path() {
        String resource = "index/2";
        String rawUrl = HOST + '/' + resource;

        Uri url = new UrlParser(rawUrl).parse();

        assertEquals(resource, url.getPath());
    }

    @Test
    @DisplayName("fragment")
    void fragment() {
        String fragment = "reference";
        String rawUrl = HOST + "/index/2#" + fragment;

        Uri url = new UrlParser(rawUrl).parse();

        assertEquals(fragment, url.getFragment());
    }

    @Test
    @DisplayName("queries")
    void queries() {
        String key1 = "key1";
        String key2 = "key2";

        String value1 = "value1";
        String value2 = "value2";

        String query1 = key1 + '=' + value1;
        String query2 = key2 + '=' + value2;

        String rawUrl = HOST + '?' + query1 + '&' + query2;

        Uri url = new UrlParser(rawUrl).parse();

        List<Uri.QueryParameter> queries = url.getQueryList();

        assertEquals(2, queries.size());

        Uri.QueryParameter queryInstance1 = queries.get(0);
        Uri.QueryParameter queryInstance2 = queries.get(1);

        assertEquals(key1, queryInstance1.getKey());
        assertEquals(value1, queryInstance1.getValue());
        assertEquals(key2, queryInstance2.getKey());
        assertEquals(value2, queryInstance2.getValue());
    }

    @Test
    @DisplayName("URL with all sub-items")
    void urlWithAllSubItems() {
        String rawUrl = "https://user:password@spine.io/index?auth=none&locale=us#fragment9";

        Uri record = new UrlParser(rawUrl).parse();

        assertEquals(Uri.Schema.HTTPS, record.getProtocol()
                                             .getSchema());
        assertEquals("user", record.getAuth()
                                   .getUserName());
        assertEquals("password", record.getAuth()
                                       .getPassword());
        assertEquals("spine.io", record.getHost());
        assertEquals("index", record.getPath());
        assertEquals("auth=none", UrlQueryParameters.toString(record.getQuery(0)));
        assertEquals("locale=us", UrlQueryParameters.toString(record.getQuery(1)));
        assertEquals("fragment9", record.getFragment());
    }
}
