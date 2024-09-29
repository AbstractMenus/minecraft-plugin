package ru.abstractmenus.util;

import ru.abstractmenus.MainConfig;

public final class TimeUtil {

    private static TimeParser parser;

    private TimeUtil() {}

    public static void init(MainConfig conf){
        parser = new TimeParser(conf.getTimeDay(), conf.getTimeHour(),
                conf.getTimeMinute(), conf.getTimeSecond());
    }

    public static long currentTimeTicks(){
        return System.currentTimeMillis() / 50;
    }

    public static String getTimeString(long millis){
        return parser.toString(millis);
    }

    public static long parseTime(String time) {
        return parser.fromString(time);
    }
}
