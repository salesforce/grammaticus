package com.force.i18n.commons.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.LanguageDeclension;

public final class LogUtil {
    public static final String PREFIX = "###\t";
    public static final String PREFIX_ERROR = PREFIX + "Error: ";
    public static final String PREFIX_WARN = PREFIX + "Warning: ";
    public static final String PREFIX_ERROR_WITH_DECLENSION = PREFIX_ERROR + "%s: ";
    public static final String PREFIX_WARN_WITH_DECLENSION = PREFIX_WARN + "%s: ";

    private LogUtil() {
        // do not consturct
    }

    public static void log(Logger logger, Level level, String msg, Object... params) {
        logger.log(level, () -> (params == null || params.length == 0) ? msg : String.format(msg, params));
    }

    public static void log(Logger logger, Level level, String prefix, HumanLanguage language, String msg,
            Object... params) {
        logger.log(level, () -> String.format(prefix, language.getLocaleString())
                + ((params == null || params.length == 0) ? msg : String.format(msg, params)));
    }

    // log with Declension to print language as prefix
    public static void error(Logger logger, Level level, LanguageDeclension declension, String msg, Object... params) {
        log(logger, level, PREFIX_ERROR_WITH_DECLENSION, declension.getLanguage(), msg, params);
    }

    public static void warning(Logger logger, Level level, LanguageDeclension declension, String msg,
            Object... params) {
        log(logger, level, PREFIX_WARN_WITH_DECLENSION, declension.getLanguage(), msg, params);
    }
}
