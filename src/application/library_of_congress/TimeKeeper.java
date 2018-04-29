package application.library_of_congress;

import java.util.Calendar;
import java.util.Date;

/**
 * The Time Master is responsible for providing the timestamp for each run. The format does not use colons
 * as this may cause troubles when creating folder and file names.
 */
public class TimeKeeper {

    private final String time;

    public TimeKeeper() {
        Date date = new Date(System.currentTimeMillis());
        Calendar cal = new Calendar.Builder().setInstant(date).build();
        cal.set(Calendar.AM_PM, 1);
        StringBuilder str = new StringBuilder();
        str.append(cal.get(Calendar.YEAR));
        str.append("-");
        str.append(formatMonth(cal.get(Calendar.MONTH)));
        str.append("-");
        str.append(formatNum(cal.get(Calendar.DAY_OF_MONTH)));
        str.append("T");
        str.append(formatNum(formatHour(cal.get(Calendar.HOUR),cal)));
        str.append("-");
        str.append(formatNum(cal.get(Calendar.MINUTE)));
        str.append("-");
        str.append(formatNum(cal.get(Calendar.SECOND)));
        time = str.toString();
    }

    // Dirty hack because java thinks it's March, but today is April
    private String formatMonth(int val) {
        if (val == 3) {
            val ++;
        }
        return formatNum(val);
    }

    // Used to convert to 24 hour time format
    private int formatHour(int val, Calendar cal) {
        if (cal.get(Calendar.AM_PM) == 1) {
            return val + 12;
        }
        return val;
    }

    private String formatNum(int val) {
        return formatNum(String.valueOf(val));
    }

    private String formatNum(String val) {
        if (val.length() < 2) {
            return "0" + val;
        }
        return val;
    }

    public String getTime() {
        return time;
    }

    public static void main(String ... args) {
        System.out.println("new TimeKeeper().getTime() = " + new TimeKeeper().getTime());
    }
}
