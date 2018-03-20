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
package io.spine.tools.codestyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A configuration class for the {@link CodeStyleCheckerPlugin} nested plugins.
 *
 * @author Dmytro Grankin
 */
public class StepConfiguration {

    /**
     * A number of violations that can be ignored per file.
     *
     * <p>The default value is zero.
     */
    private Threshold threshold = new Threshold(0);

    /**
     * A maximum line length permitted.
     *
     * <p>The default value is 100.
     */
    private int maxTextWidth = 100;

    /**
     * A plugin {@link ReportType}.
     *
     * {@link ReportType#WARN} is default.
     */
    private ReportType reportType = ReportType.WARN;

    public ReportType getReportType() {
        return reportType;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public int getMaxTextWidth() {
        return maxTextWidth;
    }

    @SuppressWarnings("unused")
    public void setMaxTextWidth(int value) {
        this.maxTextWidth = value;
        log().debug("Right margin set up to {}", maxTextWidth);
    }

    public void setThreshold(int value) {
        this.threshold = new Threshold(value);
        log().debug("Threshold set up to {}", threshold);
    }

    public void setReportType(String reportType) {
        checkNotNull(reportType);
        this.reportType = ReportType.of(reportType);
        log().debug("Report type set up to {}", this.reportType);
    }

    private static Logger log() {
        return LoggerSingleton.INSTANCE.logger;
    }

    private enum LoggerSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger logger = LoggerFactory.getLogger(StepConfiguration.class);
    }
}
