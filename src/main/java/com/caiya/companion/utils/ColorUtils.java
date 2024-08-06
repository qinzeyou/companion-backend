package com.caiya.companion.utils;

import org.springframework.stereotype.Component;

/**
 * @author caiya
 * @description 校验颜色工具类
 * @create 2024-08-06 10:44
 */
@Component
public class ColorUtils {
    /**
     * 校验十六进制颜色
     * @param color 待校验颜色的字符串
     * @return boolean值，true为正确十六进制颜色，反之不是
     */
    public boolean isValidHexColor(String color) {
        String hexPattern = "^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$";
        return color.matches(hexPattern);
    }
}
