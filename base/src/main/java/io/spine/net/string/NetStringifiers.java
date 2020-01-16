/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.net.string;

import com.google.common.collect.ImmutableList;
import io.spine.net.EmailAddress;
import io.spine.net.InternetDomain;
import io.spine.net.Url;
import io.spine.string.Registrar;
import io.spine.string.Stringifier;

/**
 * Provides stringifiers for network-related types.
 */
public final class NetStringifiers {

    static {
        Registrar registrar = new Registrar(ImmutableList.of(
                forUrl(),
                forEmailAddress(),
                forInternetDomain()
        ));
        registrar.register();
    }

    /** Prevents instantiation of this utility class. */
    private NetStringifiers() {
    }

    /** Obtains default stringifier for {@code Url}. */
    public static Stringifier<Url> forUrl() {
        return UrlStringifier.getInstance();
    }

    /** Obtains default stringifier for {@code EmailAddress}. */
    public static Stringifier<EmailAddress> forEmailAddress() {
        return EmailAddressStringifier.getInstance();
    }

    /** Obtains default stringifier for {@code InternetDomain}. */
    public static Stringifier<InternetDomain> forInternetDomain() {
        return InternetDomainStringifier.getInstance();
    }
}
