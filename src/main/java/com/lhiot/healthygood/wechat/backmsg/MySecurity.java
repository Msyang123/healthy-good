package com.lhiot.healthygood.wechat.backmsg;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MySecurity {
    public static final String SHA_1 = "SHA-1";
    public static final String MD5 = "MD5";

    public MySecurity() {
    }

    public String encode(String strSrc, String encodeType) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();

        try {
            if (encodeType == null || "".equals(encodeType)) {
                encodeType = "MD5";
            }

            md = MessageDigest.getInstance(encodeType);
            md.update(bt);
            strDes = this.bytes2Hex(md.digest());
            return strDes;
        } catch (NoSuchAlgorithmException var7) {
            return strSrc;
        }
    }

    public String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;

        for(int i = 0; i < bts.length; ++i) {
            tmp = Integer.toHexString(bts[i] & 255);
            if (tmp.length() == 1) {
                des = des + "0";
            }

            des = des + tmp;
        }

        return des;
    }
}
