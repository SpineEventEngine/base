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

import io.spine.net.UrlRecord.QueryParameter;

import java.util.List;

/**
 * Performs conversion of URLs to String.
 *
 * @author Mikhail Mikhaylov
 */
final class UrlPrinter {

    private UrlPrinter() {
    }

    /**
     * Converts {@link UrlRecord} to String.
     */
    static String printToString(UrlRecord url) {
        // We don't know the capacity at this point
        @SuppressWarnings("StringBufferWithoutInitialCapacity") StringBuilder sb = new StringBuilder();

        appendProtocol(url, sb);
        appendAuth(url, sb);
        appendHost(url, sb);
        appendPort(url, sb);
        appendPath(url, sb);
        appendQueries(url, sb);
        appendFragment(url, sb);

        return sb.toString();
    }

    private static void appendProtocol(UrlRecord record, StringBuilder sb) {
        if (!record.hasProtocol()) {
            return;
        }

        UrlRecord.Protocol protocol = record.getProtocol();
        if (protocol.getProtocolCase() == UrlRecord.Protocol.ProtocolCase.NAME) {
            sb.append(protocol.getName())
              .append(UrlParser.PROTOCOL_ENDING);
            return;
        }

        sb.append(protocol.getSchema()
                          .name()
                          .toLowerCase())
          .append(UrlParser.PROTOCOL_ENDING);
    }

    private static void appendAuth(UrlRecord record, StringBuilder sb) {
        if (!record.hasAuth() || record.getAuth()
                                       .equals(UrlRecord.Authorization.getDefaultInstance())) {
            return;
        }

        UrlRecord.Authorization auth = record.getAuth();
        String userName = auth.getUserName();
        String password = auth.getPassword();

        if (userName.isEmpty()) {
            return;
        }
        sb.append(userName);

        if (!password.isEmpty()) {
            sb.append(UrlParser.CREDENTIALS_SEPARATOR)
              .append(password);
        }

        sb.append(UrlParser.CREDENTIALS_ENDING);
    }

    private static void appendHost(UrlRecord record, StringBuilder sb) {
        sb.append(record.getHost());
    }

    private static void appendPort(UrlRecord record, StringBuilder sb) {
        String port = record.getPort();
        if (port.isEmpty()) {
            return;
        }

        sb.append(UrlParser.HOST_PORT_SEPARATOR)
          .append(port);
    }

    private static void appendPath(UrlRecord record, StringBuilder sb) {
        String path = record.getPath();
        if (path.isEmpty()) {
            return;
        }

        sb.append(UrlParser.HOST_ENDING)
          .append(path);
    }

    private static void appendQueries(UrlRecord record, StringBuilder sb) {
        List<QueryParameter> queryList = record.getQueryList();

        if (queryList.isEmpty()) {
            return;
        }

        sb.append(UrlParser.QUERIES_START);

        int queriesSize = queryList.size();
        for (int i = 0; i < queriesSize; i++) {
            String stringQuery = UrlQueryParameters.toString(queryList.get(i));
            sb.append(stringQuery);
            if (i != queriesSize - 1) {
                sb.append(UrlParser.QUERY_SEPARATOR);
            }
        }
    }

    private static void appendFragment(UrlRecord record, StringBuilder sb) {
        String fragment = record.getFragment();
        if (fragment.isEmpty()) {
            return;
        }

        sb.append(UrlParser.FRAGMENT_START)
          .append(fragment);
    }
}
