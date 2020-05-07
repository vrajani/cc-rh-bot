package pl.vrajani.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

    public static boolean isDownTime() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int currentHour = utc.get(Calendar.HOUR_OF_DAY);
        int currentMinute = utc.get(Calendar.MINUTE);

        return (currentHour == 21 && currentMinute > 25 )|| (currentHour == 22 && currentMinute < 5 );
    }

    public static String getCurrentTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean isBadTimeOfTheWeek() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int dayOfWeek = utc.get(Calendar.DAY_OF_WEEK);
        int currentHour = utc.get(Calendar.HOUR_OF_DAY);
        return (dayOfWeek == 1 && currentHour > 10) || (dayOfWeek==2 && currentHour < 18);
    }
}
