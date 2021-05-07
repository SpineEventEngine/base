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

package io.spine.tools.code;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A non-negative indentation of the generated code.
 */
@Immutable
public final class Indent implements Serializable {

    private static final long serialVersionUID = 0L;
    private static final String SPACE = " ";

    private static final Indent TWO = new Indent(2, 0);
    private static final Indent FOUR = new Indent(4, 0);

    /**
     * The number of {@link #SPACE} characters are put for each {@link #level} of indentation.
     */
    private final int size;

    /**
     * The level of indentation.
     *
     * <p>This field defines a number of times the code would be shifted.
     */
    private final int level;

    /**
     * Creates indentation of the passed size at the given level.
     *
     * @param size
     *         the size of indentation (typically 2 or 4) of
     * @param level
     *         the level of indentation the code block to be generated
     */
    private Indent(int size, int level) {
        checkArgument(size >= 0, "Size must be non-negative. Passed: %s.", size);
        checkArgument(level >= 0, "Level must be non-negative. Passed: %s.", level);
        this.size = size;
        this.level = level;
    }

    private Indent(int size) {
        this(size, 1);
    }

    /**
     * Creates an instance for non-negative number of space characters.
     */
    public static Indent of(int spaces) {
        checkArgument(spaces >= 0);
        if (spaces == 4) {
            return of4();
        }
        if (spaces == 2) {
            return of2();
        }
        return new Indent(spaces);
    }

    /**
     * Obtains the indent with two spaces.
     */
    public static Indent of2() {
        return TWO;
    }

    /**
     * Obtains the indent with four spaces.
     */
    public static Indent of4() {
        return FOUR;
    }

    public Indent at(int level) {
        checkArgument(level >=0);
        return new Indent(this.size, level);
    }

    /**
     * Obtains the size of the indentation.
     */
    public int size() {
        return size;
    }

    /**
     * Obtains the level of the indentation.
     */
    public int level() {
        return level;
    }

    /**
     * Obtains an instance shifted to the right by one level.
     */
    public Indent shiftedRight() {
        return new Indent(size, level + 1);
    }

    /**
     * Obtains an instance shifted to the left by one level.
     *
     * @throws IllegalStateException
     *          if this indentation is already at the zero column
     */
    public Indent shiftedLeft() {
        checkState(level > 0, "Already at zero. Cannot shift to the left more.");
        return new Indent(size, level - 1);
    }

    /**
     * Obtains an new indent shifted to the this one by the passed number of levels.
     *
     * <p>A new level of indentation must be non-negative.
     */
    Indent shifted(int delta) {
        if (delta == 0) {
            return this;
        }
        int requested = level + delta;
        checkArgument(requested >= 0,
                      "Cannot indent to the left more (`%s`)." +
                              " Current indentation: `%s`." +
                              " Requested delta: `%s`.",
                      requested, level, delta
        );
        return new Indent(size, requested);
    }

    /**
     * Obtains the string representing the total indentation for the indent
     * with this {@link #size()} at this {@link #level()}.
     */
    @Override
    public String toString() {
        String text = Strings.repeat(SPACE, size * level);
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Indent other = (Indent) o;
        if (size != other.size) {
            return false;
        }
        return level == other.level;
    }

    @Override
    public int hashCode() {
        int result = size;
        result = 31 * result + level;
        return result;
    }
}
