package com.caiya.companion.utilsTest;

import com.caiya.companion.utils.ColorUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
class ColorUtilTests {
    @Resource
    private ColorUtils colorUtils;

    @Test
    void isValidHexColorTest() {
        String color = "#F00";
        boolean res = colorUtils.isValidHexColor(color);
        System.out.println(res);
    }
}
