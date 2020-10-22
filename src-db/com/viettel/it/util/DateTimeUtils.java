/*
* Copyright (C) 2011 Viettel Telecom. All rights reserved.
* VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/
package com.viettel.it.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author KhiemVK
 * @version 1.0
 * @since: 8/14/13 5:11 PM
 */
public class DateTimeUtils {
//    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
//    public static final SimpleDateFormat DATE_TIME_FORMAT_SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_TIME_FORMAT_YMD = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_TIME_FORMAT_EPOCH = "yyyy-MM-dd HH:mm:ss";
    public static final long ONE_DAY = 86400000;
    public static final long ONE_HOUR = 3600000;

    public static String convertHoursToString(Double hours) {
        if (hours == null) {
            return null;
        } else {
            /*int d = (new Double(hours * 60)).intValue();
            int hour = d / 60;
            int minute = Math.abs(d % 60);
            return String.format("%d:%02d", hour, minute);*/

            Double d = (hours * 60);
            int hour = ((Double) (d / 60)).intValue();
            int minute = ((Double) Math.abs(d % 60)).intValue();
            return String.format("%d:%02d", hour, minute);
        }
    }

    public static String convertDateToString(Date date, String pattern) throws Exception {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            try {
                return dateFormat.format(date);
            } catch (Exception e) {
                throw e;
            }
        }
        return "";
    }

    public static String convertDateToString(Date date, TimeZone timeZone, String pattern) throws Exception {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timeZone);
            try {
                return dateFormat.format(date);
            } catch (Exception e) {
                throw e;
            }
        }
        return "";
    }

    //HoangNT14 begin
    public static Long convertDateToEpoch(Date date) {
        Long epochTime = date.getTime();
        return epochTime;
    }
    //HoangNT14 End

    public static Date convertStringToDate(String date, String pattern) throws Exception {
        if (date != null && date.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            try {
                return dateFormat.parse(date);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static Date convertStringToDate(String date, TimeZone timezone, String pattern) throws Exception {
        if (date != null && date.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(timezone);
            try {
                return dateFormat.parse(date);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static Date getEndTimeOfMonth(Date date) throws Exception {
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(truncateTime(date));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTime();
    }

    public static Date getFirstTimeOfMonth(Date date) throws Exception {
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(truncateTime(date));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date getEndTimeOfWeek(Date date) throws Exception {
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(truncateTime(date));
        cal.add(Calendar.DAY_OF_WEEK,
                cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_YEAR, 7);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTime();
    }

    public static Date getFirstTimeOfWeek(Date date) throws Exception {
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(truncateTime(date));
        cal.add(Calendar.DAY_OF_WEEK,
                cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK));
        return cal.getTime();
    }

    public static Date getEndTimeOfDay(Date date) throws Exception {
        SimpleDateFormat DATE_TIME_FORMAT_SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        String str = new SimpleDateFormat("dd/MM/yyyy").format(date) + " 23:59:59";
        return DATE_TIME_FORMAT_SDF.parse(str);
    }

    public static Date truncateTime(Date date) throws Exception {
        SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
        if ((date == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return DAY_FORMAT.parse(DAY_FORMAT.format(date));
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        if ((date1 == null) || (date2 == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameWeek(cal1, cal2);
    }

    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        if ((cal1 == null) || (cal2 == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(0) == cal2.get(0)) && (cal1.get(1) == cal2.get(1)) && (cal1.get(2) == cal2.get(2));
    }

    public static boolean isSameWeek(Date date1, Date date2) {
        if ((date1 == null) || (date2 == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameWeek(cal1, cal2);
    }

    public static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        if ((cal1 == null) || (cal2 == null)) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(0) == cal2.get(0)) && (cal1.get(1) == cal2.get(1)) && (cal1.get(3) == cal2.get(3));
    }

    public static int getWeekOfYear(Date d) {
        if (d == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return getWeekOfYear(c);
    }

    public static int getWeekOfYear(Calendar c) {
        if (c == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (c.get(Calendar.WEEK_OF_YEAR));
    }


    public static Date convertLongYYYYMMDDHH24MISSToDate(Long dateId) throws Exception {
        if (dateId != null && dateId > 0) {
            try {
                //2014 01 11 00 00 00
                String dateStr = dateId.toString().substring(6, 8) + "/"
                        + dateId.toString().substring(4, 6) + "/"
                        + dateId.toString().substring(0, 4)
                        + " " + dateId.toString().substring(8, 10)
                        + ":" + dateId.toString().substring(10, 12)
                        + ":" + dateId.toString().substring(12, 14);
                return convertStringToDate(dateStr, DATE_TIME_FORMAT);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static String convertLongYYYYMMDDHH24MISSToString(Long dateId, String pattern) throws Exception {
        if (dateId != null && dateId > 0) {
            try {
                //2014 01 11 00 00 00
                String dateStr = dateId.toString().substring(6, 8) + "/"
                        + dateId.toString().substring(4, 6) + "/"
                        + dateId.toString().substring(0, 4)
                        + " " + dateId.toString().substring(8, 10)
                        + ":" + dateId.toString().substring(10, 12)
                        + ":" + dateId.toString().substring(12, 14);
                return convertDateToString(convertStringToDate(dateStr, DATE_TIME_FORMAT), pattern);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static Long convertStringToLongYYYYMMDDHH24MISS(String date, String pattern) throws Exception {
        if (date != null && !"".equals(date)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            try {
                Date dateToParse = dateFormat.parse(date);

                //2014 01 11 00 00 00
                Long year = dateToParse.getYear() + 1900L;
                int month = dateToParse.getMonth() + 1;
                int day = dateToParse.getDate();
                int hours = dateToParse.getHours();
                int minute = dateToParse.getMinutes();
                int second = dateToParse.getSeconds();

                String dateStr = year
                        + convertDate(month)
                        + convertDate(day)
                        + convertDate(hours)
                        + convertDate(minute)
                        + convertDate(second);
                return Long.parseLong(dateStr);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static String convertDate(int date) {
        String value;
        if (date == 0 || date == 1 || date == 2 || date == 3 || date == 4 || date == 5
                || date == 6 || date == 7 || date == 8 || date == 9) {
            value = "0" + date;
        } else {
            value = "" + date;
        }


        return value;
    }

//    public static Long  convertStringToLongYYYYMMDDHH24MISS(Date dateToParse) throws Exception {
//
//            try {
//                String dateStr = dateToParse.getYear()
//                        +""+ dateToParse.getMonth()
//                        +""+ dateToParse.getDay()
//                        +""+ dateToParse.getHours()
//                        +""+ dateToParse.getMinutes()
//                        +""+ dateToParse.getSeconds();
//                return Long.parseLong(dateStr);
//            } catch (Exception e) {
//                throw e;
//
//            }
//    }

    public static String convertLongMMDDToString(Long dateId) throws Exception {
        if (dateId != null && dateId > 0) {
            try {
                //2014 01 11 00 00 00
                String dateStr = dateId.toString().substring(6, 8) + "/"
                        + dateId.toString().substring(4, 6);
                return dateStr;
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static Long convertStringToLongYYYYMMDDHH24MISS(Date dateToParse) throws Exception {
        if (dateToParse != null && !"".equals(dateToParse)) {
            try {
                //2014 01 11 00 00 00
                Long year = dateToParse.getYear() + 1900L;
                int month = dateToParse.getMonth() + 1;
                int day = dateToParse.getDate();
                int hours = dateToParse.getHours();
                int minute = dateToParse.getMinutes();
                int second = dateToParse.getSeconds();

                String dateStr = year
                        + convertDate(month)
                        + convertDate(day)
                        + convertDate(hours)
                        + convertDate(minute)
                        + convertDate(second);
                return Long.parseLong(dateStr);
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    public static String convertDateToStringYYYYMMDDHH24MISS(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static Date toDateCurTimeZone(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTime();
    }
}
