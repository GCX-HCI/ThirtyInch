/*
 * Copyright (C) 2016 grandcentrix GmbH
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


import android.support.annotation.Nullable;
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

    // logs everything to Timber by default
    private static Logger logger = new Logger() {
        @Override
        public void log(final int level, final String tag, final String msg) {
            Log.println(level, TAG, tag + ": " + msg);
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

    /**
     * set a custom logger, {@code null} to disable logging
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

    TiLog() {
        throw new AssertionError("no instances");
    }
}