/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.code.version

import kotlin.math.sign

/**
 * The version of a software component.
 */
public class Version private constructor(
    private val major: String,
    private val minor: String,
    private val patch: String? = null,
    private val snapshot: String? = null
) : Comparable<Version> {

    /**
     * Creates a new version using integer values of the components.
     *
     * If `snapshot` value is non-null, while `patch` is null, zero patch value would be assumed.
     *
     * @throws IllegalArgumentException
     *          if one of the components is a negative value
     */
    public constructor(major: Int, minor: Int, patch: Int? = null, snapshot: Int? = null) :
            this(major.toString(), minor.toString(), patch?.toString(), snapshot?.toString()) {
        require(major >= 0)
        require(minor >= 0)
        require(patch?.sign != -1)
        require(snapshot?.sign != -1)
    }

    public companion object {
        private const val SNAPSHOT_INFIX: String = "-SNAPSHOT"
    }

    private val value: String
        get() {
            val snapshotPart = if (snapshot != null) "$SNAPSHOT_INFIX.$snapshot" else ""
            val patchPart = if (patch != null) ".$patch"
                // Have no patch component here, but may have snapshot one.
                else if (snapshotPart.isNotEmpty()) ".0"
                else ""
            return "$major.$minor$patchPart$snapshotPart"
        }

    /**
     * Tells if this version is a snapshot one.
     */
    public fun isSnapshot(): Boolean = value.contains(SNAPSHOT_INFIX)

    /**
     * Compares two versions.
     */
    override fun compareTo(other: Version): Int {
        var result = major.compareTo(other.major)
        if (result != 0) return result
        result = minor.compareTo(other.minor)
        if (result != 0) return result

        //TODO:2022-02-18:alexander.yevsyukov: Make snapshots less than non-snapshots
        return value.compareTo(other.value)
    }

    /**
     * Obtains the representation of this version using the format
     * `<major>.<minor>[.<patch>[.-SNAPSHOT.<snapshot>]]`.
     */
    override fun toString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Version

        if (major != other.major) return false
        if (minor != other.minor) return false
        if (patch != other.patch) return false
        if (snapshot != other.snapshot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = major.hashCode()
        result = 31 * result + minor.hashCode()
        result = 31 * result + (patch?.hashCode() ?: 0)
        return result
    }
}
