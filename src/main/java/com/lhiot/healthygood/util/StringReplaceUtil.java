package com.lhiot.healthygood.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplaceUtil {
    public static String replaceByte4(String str) {
        if (str==null||"".equals(str.trim())) {
            return "";
        }
        try {
            byte[] conbyte = str.getBytes();
            for (int i = 0; i < conbyte.length; i++) {
                if ((conbyte[i] & 0xF8) == 0xF0) {// 如果是4字节字符
                    for (int j = 0; j < 4; j++) {
                        conbyte[i + j] = 0x30;// 将当前字符变为“0000”
                    }
                    i += 3;
                }
            }
            str = new String(conbyte);
            return str.replaceAll("0000", "");
        } catch (Throwable e) {
            return "";
        }
    }
    public static String replaceEmoji(String str) {
        if (str==null||"".equals(str.trim())) {
            return "";
        }
        try {
            Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
            Matcher emojiMatcher = emoji.matcher(str);
            if (emojiMatcher.find()) {
                String temp = str.substring(emojiMatcher.start(), emojiMatcher.end());
                str = str.replaceAll(temp, "");
            }
            return str;
        } catch (Throwable e) {
            return "";
        }
    }

    public static boolean isMobileNO(String mobile){
        if (mobile.length() != 11)
        {
            return false;
        }else{
            Pattern p = null;
            Matcher m = null;
            boolean b = false;
            p = Pattern.compile("^[1-9][0-9][0-9]{9}$"); // 验证手机号 11位
            m = p.matcher(mobile);
            b = m.matches();
            return b;
        }
    }
}
