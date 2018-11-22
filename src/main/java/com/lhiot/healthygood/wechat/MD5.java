/**
* Copyright © 2016 SGSL
* 湖南绿航恰果果农产品有限公司
* http://www.sgsl.com 
* All rights reserved. 
*/
package com.lhiot.healthygood.wechat;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * MD5加密类
 * @author leon 
 * @version 1.0  2016年11月9日下午3:28:03
 */
public class MD5
{
    /**
     * 加密
     * @param str 要加密的字符串
     * @return 加密后的字节码
     */
    public static byte[] encrypt(String str)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(str.getBytes("ISO8859-1"));
            return md5.digest();

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    /**
     * 编码
     * @param array 要编码的MD5字节码
     * @return 加密后的MD5字符串
     */
    public static String encode(byte[] array)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            for (int x = 0; x < 16; x++)
            {
                if ((array[x] & 0xff) < 0x10)
                    sb.append("0");

                sb.append(Long.toString(array[x] & 0xff, 16));
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return sb.toString();
    }
    
    public static String md5(String str){
        return encode(encrypt(str));
    }

    public static String md5(InputStream in) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("md5");
        byte[] buff = new byte[1024];
        int size = -1;
        while((size=in.read(buff))!=-1)
        {
            digest.update(buff, 0, size);
        }
        in.close();
        BigInteger bigInteger = new BigInteger(1, digest.digest());
        return bigInteger.toString(16);
    }
}
