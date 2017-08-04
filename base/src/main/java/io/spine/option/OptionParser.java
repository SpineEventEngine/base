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

package io.spine.option;

import com.google.protobuf.GeneratedMessageV3;

import java.util.Collection;

/**
 * A parser of Protobuf options.
 *
 * @param <D> the type of the descriptor to obtain the option
 * @param <R> the type of an element to be returned after parsing
 * @author Dmytro Grankin
 */
public interface OptionParser<D extends GeneratedMessageV3, R> {

    /**
     * Obtains a collection of parsed items from the option.
     *
     * <p>The option value will be obtained from the specified descriptor.
     *
     * <p>If the specified descriptor does not have the option, empty collection will be returned.
     *
     * @param descriptor the descriptor to obtain the option value
     * @return the collection of parsed elements
     */
    Collection<R> parse(D descriptor);

    /**
     * Obtains a collection of parsed items from the specified option value.
     *
     * @param optionValue the valid value of option to parse
     * @return the collection of parsed elements
     */
    Collection<R> parse(String optionValue);
}
