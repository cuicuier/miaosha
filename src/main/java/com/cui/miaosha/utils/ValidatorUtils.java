package com.cui.miaosha.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验
 */
public class ValidatorUtils {
    private static Pattern mobile_pattern=Pattern.compile("1\\d{10}");

    public static boolean isMobile(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        Matcher matcher = mobile_pattern.matcher(src);
        return matcher.matches();
    }

    public static void main(String[] args) {
        System.out.println(isMobile("1234567891c"));
    }

}
