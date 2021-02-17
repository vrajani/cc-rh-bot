package pl.vrajani.utility;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        return dateFormat.format( new Date());
    }

    public static boolean isPendingOrderForLong(String createdAt, boolean isBuy) {
        createdAt = createdAt.substring(0, 23);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime localDateTime = LocalDateTime.parse(createdAt, formatter);
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusHours(48); // now in UTC
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(7);
        if(isBuy){
            return localDateTime.isBefore(threeHoursAgo);
        }
        return localDateTime.isBefore(twoDaysAgo);
    }
}
