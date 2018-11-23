/**
* Copyright © 2016 SGSL
* 湖南绿航恰果果农产品有限公司
* http://www.sgsl.com 
* All rights reserved. 
*/
package com.lhiot.healthygood.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author User 
 * @version 1.0  2016年11月12日下午2:39:59
 */
public class DateFormatUtil
{
    private static final String format1 = "yyyy-MM-dd HH:mm:ss";
    private static final String format2 = "yyyy-MM-dd hh:mm:ss";
    private static final String format3 = "yyyyMMddHHmmss";
    private static final String format4 = "yyyy/MM/dd HH:mm:ss";
    private static final String format5 = "yyyy-MM-dd";
    private static final String format6 = "yyyyMMdd";
    
    /**
     * 将时间格式转化为字符串，yyyy-MM-dd HH:mm:ss。
     * */
    public static String format1(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format1);  
        return formatter.format(date);  
    }
    
    /**
     * 将时间格式转化为字符串，yyyy-MM-dd hh:mm:ss。
     * */
    public static String format2(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format2);  
        return formatter.format(date);  
    }
    /**
     * 将时间格式转化为字符串，yyyyMMddHHmmss。
     * */
    public static String format3(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format3);  
        return formatter.format(date);  
    }
    /**
     * 将时间格式转化为字符串，yyyy/MM/dd HH:mm:ss。
     * */
    public static String format4(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format4);  
        return formatter.format(date);  
    }
    /**
     * 将时间格式转化为字符串，yyyy-MM-dd。
     * */
    public static String format5(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format5);  
        return formatter.format(date);  
    }
    /**
     * 将时间格式转化为字符串，yyyyMMdd。
     * */
    public static String format6(Date date){
        SimpleDateFormat formatter=new SimpleDateFormat(format6);  
        return formatter.format(date);  
    }

    public static Date dayAdd(int day){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }
    public static Date parse5(String dateStr){
        SimpleDateFormat formatter=new SimpleDateFormat(format5);
        try{
            return formatter.parse(dateStr);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return null;
    }
}
