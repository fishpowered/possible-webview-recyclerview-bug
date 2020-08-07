package fishpowered.best.browser.utilities;

import android.content.Context;
import android.text.format.DateFormat;

import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;

public class DateTimeHelper {

    public static String convertTimeStampToLocalDate(int timeStamp, Context context){
        java.util.Date date = new Date(timeStamp*1000L);
        return DateFormat.getDateFormat(context).format(date);
    }

    public static String convertTimeStampToDateTimeFormat(int timeStamp){
        return convertTimeStampDateFormat(timeStamp, "yyyy-MM-dd HH:mm:ss");
    }

    public static String convertTimeStampDateFormat(int timeStamp, String format){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timeStamp*1000L);
        return DateFormat.format(format, cal).toString();
    }

    public static String convertTimeStampToHHMM(int timeStamp, Context context){
        java.util.Date date = new Date(timeStamp*1000L);
        return DateFormat.getTimeFormat(context).format(date); // .substring(0,5) causes error on oona's phone because formats can be different e.g. 05:12:30, 5:12 AM etc
    }

    public static int getCurrentTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.intValue();
    }

    public static Long getCurrentTimeStampInMilliseconds(){
        Long tsLong = System.currentTimeMillis();
        return tsLong;
    }
}
