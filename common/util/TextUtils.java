package com.viettel.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import com.viettel.controller.AamConstants;
import com.viettel.controller.ChecklistAlarm;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by quanns2 on 4/8/17.
 */
public class TextUtils {
    private static Logger logger = LogManager.getLogger(com.viettel.controller.TextUtils.class);

    public static String toJavaFieldName(String name) { // "MY_COLUMN"
        String name0 = name.replace("_", " "); // to "MY COLUMN"
        name0 = WordUtils.capitalizeFully(name0); // to "My Column"
        name0 = name0.replace(" ", ""); // to "MyColumn"
        name0 = WordUtils.uncapitalize(name0); // to "myColumn"
        return name0;
    }

    public static String toJavaClassName(String name) { // "MY_TABLE"
        String name0 = name.replace("_", " "); // to "MY TABLE"
        name0 = WordUtils.capitalizeFully(name0); // to "My Table"
        name0 = name0.replace(" ", ""); // to "MyTable"
        return name0;
    }

    public static String getNewConnectionString(String url) {
        String connectionStr;

        String tmp = url.replaceAll("\r", "").replaceAll("\n", "").split(":@")[1].replaceAll("\\s", "");

        if (tmp.toUpperCase().trim().startsWith("(DESCRIPTION"))
            return tmp;

        String ip;
        String port;
        String sid;
        if (tmp.contains("/")) {
            sid = "SERVICE_NAME=" + tmp.split("/")[1];
            ip = tmp.split("/")[0].split(":")[0];
            port = tmp.split("/")[0].split(":")[1];
        } else {
            ip = tmp.split(":")[0];
            port = tmp.split(":")[1];
            sid = "SID=" + tmp.split(":")[2];
        }

        connectionStr = String.format("(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%s))(CONNECT_DATA=(%s)))", ip,
                port, sid);

        return connectionStr;
    }

    public static Float getCriticalAlarm(String time) {
        if (StringUtils.isEmpty(time))
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<ChecklistAlarm> myObjects = objectMapper.readValue(time, new TypeReference<List<ChecklistAlarm>>() {
            });
            DateTime dateTime = DateTime.now();
            String currentDay = dayOfWeek(dateTime.getDayOfWeek());
            String currentHour = String.valueOf(dateTime.getHourOfDay());

            for (ChecklistAlarm myObject : myObjects) {
                List<String> days = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(myObject.getDay());
                List<String> hours = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(myObject.getHour());

                if (days.contains(currentDay) && hours.contains(currentHour)) {
                    if (StringUtils.isNotEmpty(myObject.getCritical()))
                        return Float.valueOf(myObject.getCritical());
                    else if (StringUtils.isNotEmpty(myObject.getWarning()))
                        return Float.valueOf(myObject.getWarning());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String dayOfWeek(Integer day) {
        String dayOfWeek = null;
        switch (day) {
            case 1:
                dayOfWeek = "T2";
                break;
            case 2:
                dayOfWeek = "T3";
                break;
            case 3:
                dayOfWeek = "T4";
                break;
            case 4:
                dayOfWeek = "T5";
                break;
            case 5:
                dayOfWeek = "T6";
                break;
            case 6:
                dayOfWeek = "T7";
                break;
            case 7:
                dayOfWeek = "CN";
                break;
        }
        return dayOfWeek;
    }

    public static Integer convertOperator(String advance) {
        Integer operator = AamConstants.CKL_MATH_OPERATOR.OPT_LT;

        if (StringUtils.isEmpty(advance))
            return operator;
        Pattern pattern = Pattern.compile("^COMPARISON_OPERATOR:([^|]*)|");
        Matcher matcher = pattern.matcher(advance);
        if (matcher.find()) {
            String ope = matcher.group(1);
            if (ope == null)
                return operator;
            switch (ope) {
                case AamConstants.AOM_OPERATOR.EQ:
                    operator = AamConstants.CKL_MATH_OPERATOR.OPT_NOT_EQ;
                    break;
                case AamConstants.AOM_OPERATOR.GE:
                    operator = AamConstants.CKL_MATH_OPERATOR.OPT_LT;
                    break;
                case AamConstants.AOM_OPERATOR.GT:
                    operator = AamConstants.CKL_MATH_OPERATOR.OPT_LTE;
                    break;
                case AamConstants.AOM_OPERATOR.LE:
                    operator = AamConstants.CKL_MATH_OPERATOR.OPT_GT;
                    break;
                case AamConstants.AOM_OPERATOR.LT:
                    operator = AamConstants.CKL_MATH_OPERATOR.OPT_GTE;
                    break;
                default:
                    break;
            }
        }

        return operator;
    }

    public static boolean isNullOrEmpty(Object o) {
        if (o == null) {
            return true;
        } else if (o instanceof CharSequence) {
            return ((CharSequence) o).length() == 0;
        } else if (o instanceof Collection) {
            return ((Collection) o).isEmpty();
        } else if (o instanceof Map) {
            return ((Map) o).isEmpty();
        } else if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }
        return false;
    }

    public static String checkAndPrintObject(Object value) {
        try {
            if (isNullOrEmpty(value)) {
                return "null";
            } else {
                return value.toString();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "null";
        }
    }

    //Quytv7 20181122_Ghi them log start
    public static void checkAndPrintObject(Logger logger1, String title, Object... object) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (object != null) {
                stringBuilder.append("=====").append(title).append("(");
                for (int i = 0; i < object.length; i++) {
                    if (i % 2 == 0) {
                        stringBuilder.append(checkAndPrintObject(object[i]));
                        stringBuilder.append(":");
                    } else {
                        stringBuilder.append(checkAndPrintObject(object[i]));
                        stringBuilder.append(", ");
                    }
                }
                stringBuilder.append(")=====");
            }
//            logger1.info("Vao khong");
            logger1.info(stringBuilder.toString());
        } catch (Exception e) {
            logger1.error("Du lieu dau vao khong dung");
        }
    }
    //Quytv7 20181122_Ghi them log end

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
