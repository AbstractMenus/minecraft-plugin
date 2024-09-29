package ru.abstractmenus.util;

import java.util.concurrent.TimeUnit;

public final class TimeParser {

    private static final long[][] TIME_UNIT_MULT = {
            {'d', 'h', 'm', 's'},
            {86_400_000L, 3_600_000L, 60_000L, 1000L}
    };

    private static final int DIGIT_MIN = 48, DIGIT_MAX = 57;

    private final String day;
    private final String hour;
    private final String minute;
    private final String second;

    public TimeParser() {
        this("d", "h", "min", "sec");
    }

    public TimeParser(String day, String hour, String minute, String second){
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public long fromString(String time){
        char[] chars = time.toCharArray();
        long value = 0L;
        long multiplier = 0;

        StringBuilder num = new StringBuilder();

        for (int i = chars.length - 1; i >= 0; i--){
            char c = chars[i];
            int digit = getDigit(c);

            if (digit > -1){
                num.insert(0, c);
            } else {
                if (num.length() > 0){
                    value += parseValue(num.toString(), multiplier);
                }

                for (int j = 0; j < TIME_UNIT_MULT[0].length; j++){
                    if (TIME_UNIT_MULT[0][j] == c){
                        multiplier = TIME_UNIT_MULT[1][j];
                        break;
                    }
                }

                num = new StringBuilder();
            }
        }

        value += parseValue(num.toString(), multiplier);

        return value;
    }

    public String toString(long millis){
        StringBuilder builder = new StringBuilder();

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        if (days > 0) {
            builder.append(days);
            builder.append(day);
        }

        if (hours > 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(hours);
            builder.append(hour);
        }

        if (minutes > 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(minutes);
            builder.append(minute);
        }

        if (seconds > 0) {
            if (builder.length() > 0) builder.append(" ");
            builder.append(seconds);
            builder.append(second);
        }

        if (builder.length() == 0) {
            builder.append(0).append(second);
        }

        return builder.toString();
    }

    private long parseValue(String str, long multiplier){
        int numVal = Integer.parseInt(str, 10);
        return multiplier * numVal;
    }

    private int getDigit(int character) {
        return character >= DIGIT_MIN && character <= DIGIT_MAX ? character - DIGIT_MIN : -1;
    }
}
