package ru.abstractmenus.api;

/**
 * Simple static methods for logger
 */
public final class Logger {

    private static java.util.logging.Logger logger;

    private Logger(){}

    /**
     * Set logger instance
     * @param log Logger instance
     */
    public static void set(java.util.logging.Logger log){
        logger = log;
    }

    /**
     * Log with INFO scope
     * @param message Log message
     */
    public static void info(String message){
        logger.info(message);
    }

    /**
     * Log with WARNING scope
     * @param message Log message
     */
    public static void warning(String message){
        logger.warning(message);
    }

    /**
     * Log with SEVERE (error) scope
     * @param message Log message
     */
    public static void severe(String message){
        logger.severe(message);
    }

}
