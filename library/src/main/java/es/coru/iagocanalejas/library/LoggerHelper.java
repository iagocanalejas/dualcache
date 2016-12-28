package es.coru.iagocanalejas.library;

class LoggerHelper {

    private static final String LOG_PREFIX = "Entry for ";

    private final Logger mLogger;

    LoggerHelper(Logger logger) {
        this.mLogger = logger;
    }

    void logEntrySavedForKey(String key) {
        mLogger.logInfo(LOG_PREFIX + key + " is saved in cache.");
    }

    void logEntryForKeyIsInRam(String key) {
        mLogger.logInfo(LOG_PREFIX + key + " is in RAM.");
    }

    void logEntryForKeyIsNotInRam(String key) {
        mLogger.logInfo(LOG_PREFIX + key + " is not in RAM.");
    }

    void logEntryForKeyIsOnDisk(String key) {
        mLogger.logInfo(LOG_PREFIX + key + " is on disk.");
    }

    void logEntryForKeyIsNotOnDisk(String key) {
        mLogger.logInfo(LOG_PREFIX + key + " is not on disk.");
    }
}
