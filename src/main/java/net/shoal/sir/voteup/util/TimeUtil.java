package net.shoal.sir.voteup.util;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.enums.DurationType;
import net.shoal.sir.voteup.enums.MessageType;
import org.bukkit.Bukkit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    private static LocaleUtil locale;

    private static final int YEAR = 365 * 24 * 60 * 60;// 年
    private static final int MONTH = 30 * 24 * 60 * 60;// 月
    private static final int DAY = 24 * 60 * 60;// 天
    private static final int HOUR = 60 * 60;// 小时
    private static final int MINUTE = 60;// 分钟
    private static Calendar calendar = Calendar.getInstance();


    /**
     * 根据时间戳获取描述性时间，如3分钟前，1天前
     *
     * @param timestamp
     *            时间戳 单位为毫秒
     * @return 时间字符串
     */
    public static String getDescriptiveTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeGap = (currentTime - timestamp) / 1000;// 与现在时间相差秒数
        String timeStr = null;
        if (timeGap > YEAR) {
            timeStr = timeGap / YEAR + "年前";
        } else if (timeGap > MONTH) {
            timeStr = timeGap / MONTH + "个月前";
        } else if (timeGap > DAY) {// 1天以上
            timeStr = timeGap / DAY + "天前";
        } else if (timeGap > HOUR) {// 1小时-24小时
            timeStr = timeGap / HOUR + "小时前";
        } else if (timeGap > MINUTE) {// 1分钟-59分钟
            timeStr = timeGap / MINUTE + "分钟前";
        } else {// 1秒钟-59秒钟
            timeStr = "刚刚";
        }
        return timeStr;
    }

    public static String getDescriptiveDuration(String duration) {
        return duration
                .replace("d", " 天 ")
                .replace("H", " 小时 ")
                .replace("m", " 分钟");
    }

    public static long getDurationTimeStamp(String duration) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7调用 getDurationTimeStamp 方法.");
        long result = 0;

        String clone = duration.toUpperCase();
        locale.debug("&7待解析持续时间: &c" + clone);
        DurationType durationType;
        while((durationType = getFirstIndexOf(clone)) != null) {
            locale.debug("&7获取到的时间标识符有效: &c" + durationType.toString());
            int index = clone.indexOf(durationType.getS());
            locale.debug("&7时间标识符索引值: &c" + index);
            try {
                String target = clone.substring(0, index);
                locale.debug("&7截取到的时间配置值: &c" + target);
                int amount = Integer.parseInt(target);
                result += amount * durationType.getI();
                locale.debug("&7已算出并添加指定时长至返回值: &c" + result);
                clone = clone.substring(index + 1);
                locale.debug("&7准备开始继续解析, 剩余内容: &c" + clone);
            } catch(Throwable e) {
                Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.ERROR, "&7投票持续时间格式化失败: &c" + duration));
                break;
            }
        }

        return result * 1000;
    }

    public static DurationType getFirstIndexOf(String target) {
        int index = Integer.MAX_VALUE;
        DurationType durationType = null;
        for(DurationType type : DurationType.values()) {
            int currentIndex = target.indexOf(type.getS());
            if(currentIndex == -1) {
                continue;
            }
            if(currentIndex < index) {
                index = currentIndex;
                durationType = type;
            }
        }
        return durationType;
    }

    /**
     * Long型时间格式化
     * @param time 时间
     * @param dateFormat 日期格式,如yyyy年MM月dd日 HH:mm:ss
     * @return 格式化后的日期字符串
     */
    public static String getTime(Long time, String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(time);
    }

    /**
     * long型时间转中式日期
     * @param time
     * @return
     */
    public static String getTime(Long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return format.format(time);
    }

    /**
     * 得到当前的时间，时间格式yyyy-MM-dd
     * @return
     */
    public static String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    /**
     * 得到当前的时间,自定义时间格式
     * y 年 M 月 d 日 H 时 m 分 s 秒
     * @param dateFormat 输出显示的时间格式
     * @return
     */
    public static String getCurrentDate(String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(new Date());
    }

    /**
     * 日期格式化，默认日期格式yyyy-MM-dd
     * @param date
     * @return
     */
    public static String getFormatDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 日期格式化，自定义输出日期格式
     * @param date
     * @return
     */
    public static String getFormatDate(Date date,String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }
    /**
     * 返回当前日期的前一个时间日期，amount为正数 当前时间后的时间 为负数 当前时间前的时间
     * 默认日期格式yyyy-MM-dd
     * @param field 日历字段
     * y 年 M 月 d 日 H 时 m 分 s 秒
     * @param amount 数量
     * @return 一个日期
     */
    public static String getPreDate(String field,int amount){
        calendar.setTime(new Date());
        if(field != null && !field.equals("")){
            if(field.equals("y")){
                calendar.add(Calendar.YEAR, amount);
            }else if(field.equals("M")){
                calendar.add(Calendar.MONTH, amount);
            }else if(field.equals("d")){
                calendar.add(Calendar.DAY_OF_MONTH, amount);
            }else if(field.equals("H")){
                calendar.add(Calendar.HOUR, amount);
            }
        }else{
            return null;
        }
        return getFormatDate(calendar.getTime());
    }

    /**
     * 某一个日期的前一个日期
     * @param date,某一个日期
     * @param field 日历字段
     * y 年 M 月 d 日 H 时 m 分 s 秒
     * @param amount 数量
     * @return 一个日期
     */
    public static String getPreDate(Date date,String field,int amount){
        calendar.setTime(date);
        if(field != null && !field.equals("")){
            if(field.equals("y")){
                calendar.add(Calendar.YEAR, amount);
            }else if(field.equals("M")){
                calendar.add(Calendar.MONTH, amount);
            }else if(field.equals("d")){
                calendar.add(Calendar.DAY_OF_MONTH, amount);
            }else if(field.equals("H")){
                calendar.add(Calendar.HOUR, amount);
            }
        }else{
            return null;
        }
        return getFormatDate(calendar.getTime());
    }

    /**
     * 某一个时间的前一个时间
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getYesterday(String date) throws ParseException {
        Date d = new SimpleDateFormat().parse(date);
        String preD = getPreDate(d,"d",1);
        Date preDate = new SimpleDateFormat().parse(preD);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(preDate);
    }
}

