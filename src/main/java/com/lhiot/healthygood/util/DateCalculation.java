package com.lhiot.healthygood.util;

import com.lhiot.healthygood.entity.PeriodType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 计算月，周的第一天和最后一天，及前15天的日期等
 * @author lynn
 *
 */

public class DateCalculation {

    /**
     * 前几周的第一天,如本周
     * @param week 前几周，0-表示本周，1-上周，2-上上周,以此类推
     * @return
     */
    public static String firstDayOfWeek(int week){
    	return getStartDayOfWeek(LocalDate.now().minusWeeks(week));
    }
    
    /**
     * 前几周的最后一天,如本周
     * @param week 前几周，0-表示本周，1-上周，2-上上周,以此类推
     * @return
     */
    public static String lastDayOfWeek(int week){
    	return getEndDayOfWeek(LocalDate.now().minusWeeks(week));
    }
    
    /**
     * 前几月第一天,如本月
     * @param month 前几月，0-表示本月，1-上月，2-上上月,以此类推
     * @return
     */
    public static String firstDayOfMonth(int month){
    	return getStartDayOfMonth(LocalDate.now().minusMonths(month));
    }
    
    /**
     * 前几月最后一天,如本月
     * @param month 前几月，0-表示本月，1-上月，2-上上月,以此类推
     * @return
     */
    public static String lastDayOfMonth(int month){
    	return getEndDayOfMonth(LocalDate.now().minusMonths(month));
    }
    
    /**
     * 获取几天前的日期
     * @param days 前几天，0-表示今天，1-上昨天，2-前天,以此类推
     * @return
     */
    public static String otherDay(int days){
    	return localDate2Date(LocalDate.now().minusDays(days));
    }
    
    //周第一天
    public static String getStartDayOfWeek(TemporalAccessor date) {
        TemporalField fieldISO = WeekFields.of(Locale.CHINA).dayOfWeek();
        LocalDate localDate = LocalDate.from(date);
        localDate=localDate.with(fieldISO, 1);
        return localDate2Date(localDate);
    }
    
    //周最后一天
    public static String getEndDayOfWeek(TemporalAccessor date) {
        TemporalField fieldISO = WeekFields.of(Locale.CHINA).dayOfWeek();
        LocalDate localDate = LocalDate.from(date);
        localDate=localDate.with(fieldISO, 7);
        Date d = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1L).minusNanos(1L).toInstant());
        return DateFormatUtil.format5(d);
    }
    
    //月的第一天
    public static String getStartDayOfMonth(LocalDate date) {
        LocalDate now = date.with(TemporalAdjusters.firstDayOfMonth());
        return localDate2Date(now);
    }
    
    //月的最后一天
    public static String getEndDayOfMonth(LocalDate date) {
        LocalDate now = date.with(TemporalAdjusters.lastDayOfMonth());
        Date d = Date.from(now.atStartOfDay(ZoneId.systemDefault()).plusDays(1L).minusNanos(1L).toInstant());
        return DateFormatUtil.format5(d);
    }
    
    public static String localDate2Date(LocalDate localDate){
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        Instant instant1 = zonedDateTime.toInstant();
        Date from = Date.from(instant1);
        return DateFormatUtil.format5(from);
    }
    
    /**
     * 计算季度的第一天/最后一天
     * @param period 本季度/上季度
     * @return
     */
    public static Map<String,String> startAndEndDayOfQuarter(PeriodType period){
    	LocalDate now = LocalDate.now(ZoneId.systemDefault());
    	int month = now.getMonthValue();
    	int year = now.getYear();
    	Map<String,Integer> map = yearMonth(year, month, period);
    	LocalDate startMonth = LocalDate.of(map.get("year"), map.get("startMonth"), 1);
    	LocalDate endMonth = LocalDate.of(map.get("year"), map.get("endMonth"), 1);
    	String endDay = getEndDayOfMonth(endMonth);
    	String startDay = getStartDayOfMonth(startMonth);
    	Map<String,String> result = new HashMap<>();
    	result.put("startDay", startDay);
    	result.put("endDay", endDay);
    	return result;
    }
    
    public static Map<String,Integer> yearMonth(int year,int month,PeriodType period){
    	Map<String,Integer> map = new HashMap<>();
    	boolean flag = PeriodType.last.equals(period);
    	int startMonth = 1;
    	int endMonth = 12;
    	//第一季度
    	if(month >=1 && month <=3){
    		endMonth = 3;
    		if(flag){
    			startMonth = 9;
    			endMonth = 12;
    			year = year -1;
    		}
    	}
    	//第二季度
    	if(month >3 && month <=6){
    		startMonth = 4;
    		endMonth = 6;
    		if(flag){
    			startMonth = 1;
    			endMonth = 3;
    		}
    	}
    	//第三季度
    	if(month >6 && month <=9){
    		startMonth = 7;
    		endMonth = 9;
    		if(flag){
    			startMonth = 4;
    			endMonth = 6;
    		}
    	}
    	//第四季度
    	if(month >9 && month <=12){
    		startMonth = 9;
    		if(flag){
    			startMonth = 7;
    			endMonth = 9;
    		}
    	}
    	map.put("year", year);
    	map.put("startMonth", startMonth);
    	map.put("endMonth", endMonth);
    	return map;
    }
}
