package com.a10miaomiao.bilimiao.netword;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by 10喵喵 on 2017/4/9.
 */

public class ApiHelper {
    public static final String appKey_IOS = "4ebafd7c4951b366";
    public static final String appKey_Android = "c1b107428d337928";
    public static final String appkey_DONTNOT = "85eb6835b0a1034e";//e5b8ba95cab6104100be35739304c23a

    public static final String _appSecret_Wp = "ba3a4e554e9a6e15dc4d1d70c2b154e3";//Wp
    public static final String _appSecret_IOS = "8cb98205e9b2ad3669aad0fce12a4c13";//Ios
    public static final String _appSecret_Android = "ea85624dfcf12d7cc7b2b3a94fac1f2c";//Android
    public static final String _appSecret_DONTNOT = "2ad42749773c441109bdc0191257a664";
    public static final String _appSecret_Android2 = "jr3fcr8w7qey8wb0ty5bofurg2cmad8x";
    public static final String _appSecret_VIP = "jr3fcr8w7qey8wb0ty5bofurg2cmad8x";

    public static long getTimeSpen() {
        Date date = new Date();
        return date.getTime();
    }
    public static String getSing(String url,String secret){
        String result;
        String str = url.substring(url.indexOf("?", 4) + 1);
        List<String> list = array2list(str.split("&"));
        Collections.sort(list);
        StringBuilder stringBuilder = new StringBuilder();
        for (String str1 : list) {
            stringBuilder.append((stringBuilder.length() > 0 ? "&" : ""));
            stringBuilder.append(str1);
        }
        str = stringBuilder.toString() + secret;
        result = getMD5(str);
        return result;

    }
    public static String getSign_Android(String url) {
        return getSing(url,_appSecret_Android);
    }


    public static List<String> array2list(String[] a) {
        List<String> list = new ArrayList();
        for(String s : a){
            list.add(s);
        }
        return list;
    }

    public static String getMD5(String info)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++)
            {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1)
                {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                }
                else
                {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            return "";
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }
}
