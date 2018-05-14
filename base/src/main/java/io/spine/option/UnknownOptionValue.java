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

package io.spine.option;

import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;

import javax.annotation.Nullable;
import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * A parser for {@linkplain UnknownOptions unknown options}.
 *
 * @param <O> the type of the options
 * @param <D> the type of the descriptor to obtain the option
 * @param <R> the type of an element to be returned after parsing
 * @author Dmytro Grankin
 * @author Alexander Yevsyukov
 */
public abstract
class UnknownOptionValue<O extends ExtendableMessage, D extends GeneratedMessageV3, R>
        implements OptionValue<R> {

    /**
     * The tag number of the option for extracting values.
     */
    private final int optionNumber;

    /**
     * Creates a new instance.
     *
     * @param option the option to be handled by the parser
     */
    UnknownOptionValue(GeneratedExtension<O, String> option) {
        this.optionNumber = option.getNumber();
    }

    /**
     * Obtains the option number.
     */
    protected final int getOptionNumber() {
        return optionNumber;
    }

    /**
     * Obtains a collection of parsed items from the unknown option.
     *
     * <p>The option will be obtained from the specified descriptor.
     *
     * <p>If the specified descriptor does not have the option, empty collection will be returned.
     *
     * @param descriptor the descriptor to obtain the option value
     * @return the collection of parsed elements
     */
    public final Collection<R> parse(D descriptor) {
        final String optionValue = get(descriptor);
        if (optionValue == null) {
            return emptyList();
        }

        return parse(optionValue);
    }

    /**
     * Obtains the option value from the descriptor by the specified option number.
     *
     * @param descriptor   the descriptor to obtain the option
     * @return the option value or {@code null} if there is no option with the number
     */
    @Nullable
    protected abstract String get(D descriptor);
}
