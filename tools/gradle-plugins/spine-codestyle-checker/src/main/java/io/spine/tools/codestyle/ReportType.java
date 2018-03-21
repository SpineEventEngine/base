/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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
package io.spine.tools.codestyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * This enum states two behavior types that either log warnings or fail build process.
 *
 * @author Alexander Aleksandrov
 */
public enum ReportType {

    /**
     * This instance will log warning message.
     */
    WARN("warn"),

    /**
     * This instance will log warning message and then will throw an
     * exception and fail a build process.
     */
    ERROR("error") {
        @Override
        public void logOrFail(Throwable cause) {
            super.logOrFail(cause);
            throw new IllegalStateException(cause);
        }
    };

    private final String value;

    ReportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Logs the {@linkplain Throwable#getMessage() cause message} .
     *
     * @param cause the conflict cause
     */
    public void logOrFail(Throwable cause) {
        log().error(cause.getMessage());
    }

    /**
     * Creates {@code ReportType} instance of passed value.
     *
     * @param value report type warn or error
     * @return {@link ReportType} instance
     */
    public static ReportType of(String value) {
        for (ReportType reportType : ReportType.values()) {
            if (reportType.getValue()
                          .equals(value)) {
                return reportType;
            }
        }

        final String msg = format("Invalid report type \"%s\".", value);
        throw new IllegalStateException(msg);
    }

    private static Logger log() {
        return ReportType.LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ReportType.class);
    }
}
