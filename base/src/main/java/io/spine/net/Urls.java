/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

import io.spine.annotation.Experimental;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Utility class for working with {@link Url}.
 *
 * <p>Provides conversion and validation operations.
 *
 * @author Mikhail Mikhaylov
 */
@Experimental
public class Urls {

    private Urls() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Converts {@link Url} with {@linkplain Url#getRaw() raw} data into the
     * {@linkplain Url#getRecord() structurized} instance.
     *
     * @param rawUrl {@link Url} a {@linkplain Url#getRaw() raw} instance
     * @return {@link Url} with {@link io.spine.net.Url.Record Url.Record} instance
     * @throws IllegalArgumentException if the argument is already structurized
     */
    @SuppressWarnings("TypeMayBeWeakened")
    public static Url structurize(Url rawUrl) {
        checkNotNull(rawUrl);
        if (rawUrl.getValueCase() != Url.ValueCase.RAW) {
            throw newIllegalArgumentException("Given url is already built (%s)", rawUrl);
        }

        final String rawUrlString = rawUrl.getRaw();

        final Url url = new UrlParser(rawUrlString).parse();

        validate(url);

        return url;
    }

    /**
     * Creates a {@link Url} from a string value.
     *
     * <p>Does not perform any additional validation of the value, except
     * calling {@link Urls#validate(Url)}.
     *
     * <p>The returned instance is {@linkplain #structurize(Url) structured}.
     *
     * @param rawUrlString raw URL String
     * @return {@link Url} with {@link io.spine.net.Url.Record Url.Record} instance
     */
    public static Url create(String rawUrlString) {
        checkNotNull(rawUrlString);
        final Url.Builder builder = Url.newBuilder();
        builder.setRaw(rawUrlString);
        final Url rawUrl = structurize(builder.build());
        return rawUrl;
    }

    /**
     * Performs String conversion for given {@link Url}.
     *
     * @param url valid {@link Url} instance
     * @return String representation of the given URL
     * @throws IllegalArgumentException if the argument is invalid
     */
    public static String toString(Url url) {
        checkNotNull(url);
        validate(url);
        final String stringUrl = UrlPrinter.printToString(url);
        return stringUrl;
    }

    /**
     * Validates {@link Url} instance.
     *
     * <ul>
     *     <li>{@link Url} with raw String is always valid.
     *     <li>{@link Url} with not set value is always invalid.
     *     <li>{@link Url} can not have empty host.
     *     <li>{@link io.spine.net.Url.Record.Authorization Record.Authorization} can't have
     *          password without having login.
     * </ul>
     *
     * @param url {@link Url} instance
     * @throws IllegalArgumentException in case of invalid {@link Url}
     */
    @SuppressWarnings("TypeMayBeWeakened")
    public static void validate(Url url) {
        checkNotNull(url);
        if (url.getValueCase() == Url.ValueCase.VALUE_NOT_SET) {
            throw new IllegalArgumentException("Url is empty");
        }

        if (url.getValueCase() == Url.ValueCase.RAW) {
            return;
        }

        final Url.Record record = url.getRecord();
        final String host = record.getHost();
        if (host.isEmpty()) {
            throw newIllegalArgumentException("Url host can not be empty (%s)", url);
        }

        final Url.Record.Authorization auth = record.getAuth();
        final String user = auth.getUserName();
        final String password = auth.getPassword();

        if (user.isEmpty() && !password.isEmpty()) {
            throw new IllegalArgumentException("Url can't have password without having user name");
        }
    }
}
