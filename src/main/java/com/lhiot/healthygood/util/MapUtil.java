/**
* Copyright © 2016 SGSL
* 湖南绿航恰果果农产品有限公司
* http://www.sgsl.com 
* All rights reserved. 
*/
package com.lhiot.healthygood.util;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author User 
 * @version 1.0  2016年11月12日下午2:39:59
 */
public class MapUtil
{
	 private static double EARTH_RADIUS = 6378.137; 
	 
	 private static double rad(double d) { 
	        return d * Math.PI / 180.0; 
	    }

	 private static final String GD_KEY = "0a846fab5fd453615cd027875855482b";

	 private static final String BAIDU_TO_GD_URL = "https://restapi.amap.com/v3/assistant/coordinate/convert?key="+GD_KEY+"&coordsys=baidu&locations=";

	 private static final String GD_MAP = "https://restapi.amap.com/v3/geocode/geo?key="+GD_KEY+"&city=0731&address=";
	 /**
	  * 根据经纬度计算距离
	  * @param lat1Str 坐标1纬度
	  * @param lng1Str坐标1经度
	  * @param lat2Str坐标2纬度
	  * @param lng2Str坐标2经度
	  * @return
	  */
	 public static String getDistance(String lat1Str, String lng1Str, String lat2Str, String lng2Str) {
	        Double lat1 = Double.parseDouble(lat1Str);
	        Double lng1 = Double.parseDouble(lng1Str);
	        Double lat2 = Double.parseDouble(lat2Str);
	        Double lng2 = Double.parseDouble(lng2Str);
	         
	        double radLat1 = rad(lat1);
	        double radLat2 = rad(lat2);
	        double difference = radLat1 - radLat2;
	        double mdifference = rad(lng1) - rad(lng2);
	        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
	                + Math.cos(radLat1) * Math.cos(radLat2)
	                * Math.pow(Math.sin(mdifference / 2), 2)));
	        distance = distance * EARTH_RADIUS;
	        BigDecimal bd = new BigDecimal(distance);
	        bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
	        
	        return bd.toString();
	    }
    
    public static void main(String[] args) {
    	//System.out.println(getDistance("28.236447","112.914096","28.123026","113.021028"));
//		JSONObject J = GD_ADDRESS("麓谷企业广场");
//		System.out.println(J.getJSONArray("geocodes").getJSONObject(0).get("location"));
	}

	public static JSONObject BAIDU_TO_GD(String lng,String lat) {
		try {
			URL url = new URL( BAIDU_TO_GD_URL+lng+","+lat);
			URLConnection httpsConn = (URLConnection) url.openConnection();
			if (httpsConn != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(httpsConn.getInputStream(), "UTF-8"));
				StringBuilder s = new StringBuilder("");
				String data = null;
				while ((data = br.readLine()) != null) {
					s.append(data);
				}
				br.close();
				return JSONObject.parseObject(s.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	public static String GD_ADDRESS(String address) {
		try {
			URL url = new URL( GD_MAP+address);
			URLConnection httpsConn = (URLConnection) url.openConnection();
			if (httpsConn != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(httpsConn.getInputStream(), "UTF-8"));
				StringBuilder s = new StringBuilder("");
				String data = null;
				while ((data = br.readLine()) != null) {
					s.append(data);
				}
				br.close();
				JSONObject J = JSONObject.parseObject(s.toString());
				return  J.getJSONArray("geocodes").getJSONObject(0).get("location")+"";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static double[] bd09_To_Gcj02(double lng, double lat) {
		double x_pi = 52.35987755982988D;
		double x = lng - 0.0065D;
		double y = lat - 0.006D;
		double z = Math.sqrt(x * x + y * y) - 2.0E-5D * Math.sin(y *x_pi);
		double theta = Math.atan2(y, x) - 3.0E-6D * Math.cos(x *x_pi);
		double tempLon = z * Math.cos(theta);
		double tempLat = z * Math.sin(theta);
		return new double[]{tempLat, tempLon};
	}
}
