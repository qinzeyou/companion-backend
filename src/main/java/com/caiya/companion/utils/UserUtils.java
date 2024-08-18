package com.caiya.companion.utils;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * @Author caiya
 * @Description 用户相关工具栏
 * @Version 1.0
 */
@Component
public class UserUtils {
    /**
     * 随机生成指定长度的昵称
     * @param len 指定长度
     * @return 昵称
     */
    public static String getRandomUsername(int len) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String str = null;
            int hightPos, lowPos; // 定义高低位
            Random random = new Random();
            hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            byte[] b = new byte[2];
            b[0] = (byte) hightPos;
            b[1] = (byte) lowPos;
            try {
                str = new String(b, "GBK"); // 转成中文
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            ret.append(str);
        }
        return ret.toString();
    }
}
