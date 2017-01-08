/*
 * Copyright 2014 Vincent Brison.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iagocanalejas.dualcache;

import android.util.Log;

/**
 * This class provide a logging instance to the library.
 */
final class Logger {

    private static final String DEFAULT_LOG_TAG = "DualCache";
    private final boolean mLogEnabled;

    Logger(boolean logEnabled) {
        this.mLogEnabled = logEnabled;
    }

    /**
     * Default log info using tag {@link #DEFAULT_LOG_TAG}.
     *
     * @param tag caller class name.
     * @param msg the msg to log.
     */
    void logInfo(String tag, String msg) {
        if (mLogEnabled) {
            Log.i(DEFAULT_LOG_TAG, tag + ": " + msg);
        }
    }

    /**
     * Default log info using tag {@link #DEFAULT_LOG_TAG}.
     *
     * @param msg the msg to log.
     */
    void logInfo(String msg) {
        if (mLogEnabled) {
            Log.i(DEFAULT_LOG_TAG, msg);
        }
    }

    /**
     * Log with level warning and tag {@link #DEFAULT_LOG_TAG}.
     *
     * @param tag caller class name.
     * @param msg the msg to log.
     */
    void logWarning(String tag, String msg) {
        if (mLogEnabled) {
            Log.w(DEFAULT_LOG_TAG, tag + ": " + msg);
        }
    }

    /**
     * Log with level error and tag {@link #DEFAULT_LOG_TAG}.
     *
     * @param tag   caller class name.
     * @param error the error to log.
     */
    void logError(String tag, Throwable error) {
        if (mLogEnabled) {
            Log.e(DEFAULT_LOG_TAG, tag + ": " + error);
        }
    }

    /**
     * Log with level error and tag {@link #DEFAULT_LOG_TAG}.
     *
     * @param tag   caller class name.
     * @param error the error to log.
     */
    void logError(String tag, String error) {
        if (mLogEnabled) {
            Log.e(DEFAULT_LOG_TAG, tag + ": " + error);
        }
    }

    void logEntrySavedForKey(String key) {
        logInfo("ENTRY: " + key + " SAVED.");
    }

    void logEntryForKeyIsInRam(String key) {
        logInfo("ENTRY: " + key + " IN RAM.");
    }

    void logEntryForKeyIsNotInRam(String key) {
        logInfo("ENTRY: " + key + " NOT IN RAM.");
    }

    void logEntryForKeyIsOnDisk(String key) {
        logInfo("ENTRY: " + key + " ON DISK.");
    }

    void logEntryForKeyIsNotOnDisk(String key) {
        logInfo("ENTRY: " + key + " NOT ON DISK.");
    }

}
