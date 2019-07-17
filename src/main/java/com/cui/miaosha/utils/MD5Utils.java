package com.cui.miaosha.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    /**
     * 客户端明文密码第一次MD5加密
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass) {
        String string = salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(string);
    }

    /**
     * 服务器进行第二次MD5加密再存到数据库
     * @param inputPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String inputPass,String salt) {
        String string = salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(string);
    }

    /**
     * 合并两次加密
     * @param input
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String input, String salt) {
        String formPass = inputPassToFormPass(input);
        String dbPass = formPassToDBPass(formPass, salt);
        return dbPass;
    }

}
