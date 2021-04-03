package com.a10miaomiao.bilimiao.ui.widget.expandabletext.model;

import java.util.UUID;

public class UUIDUtils {
    /**
     * 生成一个32位的不带-的不唯一的uuid
     *
     * @return
     */
    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String[] chars = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};


    /**
     * 生成一个32位的不带-的不唯一的uuid
     *
     * @return
     */
    public static String getUuid(int length) {
        StringBuilder stringBuilder = new StringBuilder(UUID.randomUUID().toString());
        while (stringBuilder.length() < length) {
            stringBuilder.append(UUID.randomUUID().toString());
        }
        return stringBuilder.substring(0,length);
    }
}