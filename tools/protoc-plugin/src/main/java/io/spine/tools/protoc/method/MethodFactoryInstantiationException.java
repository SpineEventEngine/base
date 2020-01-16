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

package io.spine.tools.protoc.method;

import static java.lang.String.format;

/**
 * Exception that is thrown when a particular
 * {@link io.spine.tools.protoc.method.MethodFactory MethodFactory} could not be instantiated.
 */
final class MethodFactoryInstantiationException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    private static final String ERR_MSG_UNABLE_TO_INSTANTIATE =
            "Unable to instantiate MethodFactory `%s`.";

    /**
     * Creates a new instance with the factory name.
     *
     * @param factoryName
     *         the MessageFactory name
     */
    MethodFactoryInstantiationException(String factoryName) {
        super(makeMsg(factoryName));
    }

    private static String makeMsg(String factoryName) {
        return format(ERR_MSG_UNABLE_TO_INSTANTIATE, factoryName);
    }

    /**
     * Creates a new instance with the factory name and the cause.
     *
     * @param factoryName
     *         the MessageFactory name
     * @param cause
     *         the exception cause
     */
    MethodFactoryInstantiationException(String factoryName, Throwable cause) {
        super(makeMsg(factoryName), cause);
    }
}
