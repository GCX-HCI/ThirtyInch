/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch;


import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Logging class used for all logging of ThirtyInch.
 */
public class TiLog {

    /**
     * Simple logging interface for log messages from ThirtyInch
     *
     * @see #setLogger(Logger)
     */
    public interface Logger {

        /**
         * @param level one of {@link Log#VERBOSE}, {@link Log#DEBUG},{@link Log#INFO},
         *              {@link Log#WARN},{@link Log#ERROR}
         * @param tag   log tag, caller
         * @param msg   message to log
         */
        void log(final int level, final String tag, final String msg);
    }

    private static final String TAG = "ThirtyInch";

    /**
     * predefined logger using {@link Log} to print into Logcat with tag "ThirtyInch"
     *
     * @see #setLogger(Logger)
     */
    public static Logger LOGCAT = new Logger() {
        @Override
        public void log(final int level, final String tag, final String msg) {
            Log.println(level, TAG, tag + ": " + msg);
        }
    };

    /**
     * no-op version, doesn't log
     */
    public static Logger NOOP = new Logger() {
        @Override
        public void log(final int level, final String tag, final String msg) {
            // no-op
        }
    };

    private static Logger logger;

    /**
     * forward log to {@link TiLog} for logging
     */
    public static Logger TI_LOG = new Logger() {
        @Override
        public void log(final int level, final String tag, final String msg) {
            TiLog.log(level, tag, msg);
        }
    };

    public static void d(final String tag, final String msg) {
        if (logger != null) {
            logger.log(Log.DEBUG, tag, msg);
        }
    }

    public static void e(final String tag, final String msg) {
        if (logger != null) {
            logger.log(Log.ERROR, tag, msg);
        }
    }

    public static void i(final String tag, final String msg) {
        if (logger != null) {
            logger.log(Log.INFO, tag, msg);
        }
    }

    public static void log(final int level, final String tag, final String msg) {
        if (logger != null) {
            logger.log(level, tag, msg);
        }
    }

    /**
     * set a custom logger, {@code null} to disable logging
     * <p>
     *
     * Use the default logcat logger for Android:
     * <code>
     * <pre>
     * TiLog.setLogger(TiLog.LOGCAT);
     * </pre>
     * </code>
     * <p>
     * Combine it with Timber:<br>
     *
     * <code>
     * <pre>
     * TiLog.setLogger(new TiLog.Logger() {
     *    &#64;Override
     *    public void log(final int level, final String tag, final String msg) {
     *        Timber.tag(tag).log(level, msg);
     *    }
     * });
     * </pre>
     * </code>
     */
    public static void setLogger(@Nullable final Logger logger) {
        if (logger == TI_LOG) {
            throw new IllegalArgumentException(
                    "Recursion warning: You can't use TI_LOG as Logger for TiLog");
        }
        TiLog.logger = logger;
    }

    public static void v(final String tag, final String msg) {
        if (logger != null) {
            logger.log(Log.VERBOSE, tag, msg);
        }
    }

    public static void w(final String tag, final String msg) {
        if (logger != null) {
            logger.log(Log.WARN, tag, msg);
        }
    }

    private TiLog() {
        throw new AssertionError("no instances");
    }
}