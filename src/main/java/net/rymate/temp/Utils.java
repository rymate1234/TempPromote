package net.rymate.temp;

import java.util.Calendar;
import java.util.Date;

/**
 * A few static utils
 * <p>
 * Created by Ryan on 27/07/2016.
 */

public class Utils {

    public static String MIN_CHAR = "m";
    public static String HOUR_CHAR = "h";
    public static String DAY_CHAR = "d";
    public static String WEEK_CHAR = "w";
    public static String MONTH_CHAR = "M";

    public static Date parseTime(String time) {
        Date date = new Date();
        String[] elements = time.split(":");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        for (String element : elements) {
            int unit = 0;
            int amount = getAmount(element);

            if (element.endsWith(MIN_CHAR)) {
                unit = Calendar.MINUTE;
            } else if (element.endsWith(HOUR_CHAR) || element.endsWith(DAY_CHAR) || element.endsWith(WEEK_CHAR) || element.endsWith(MONTH_CHAR)) {
                unit = Calendar.HOUR;
            }

            if (element.endsWith(DAY_CHAR)) {
                amount = amount * 24;
            } else if (element.endsWith(WEEK_CHAR)) {
                amount = amount * 24 * 7;
            } else if (element.endsWith(MONTH_CHAR)) {
                amount = amount * 24 * 31;
            }

            if (unit == 0) continue;

            cal.add(unit, amount);
        }

        return cal.getTime();
    }

    public static int getAmount(String element) {
        return Integer.parseInt(element.replace(MIN_CHAR, "")
                .replace(HOUR_CHAR, "")
                .replace(DAY_CHAR, "")
                .replace(WEEK_CHAR, "")
                .replace(MONTH_CHAR, ""));

    }

}
