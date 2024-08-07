package com.caiya.companion.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author caiya
 * @description Mybatis-plus 自动填充字段实现类
 * @create 2024-07-09 12:01
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        this.setFieldValByName("joinTime", new Date(), metaObject);
        // 只有传入的标签背景颜色为空时才设置填充默认颜色
//        if (metaObject.getValue("color") == null) {
//            this.setFieldValByName("color", SystemConstant.TAG_COLOR, metaObject);
//        }
//        // 只有传入的标签文字颜色为空时才设置填充默认颜色
//        if (metaObject.getValue("textColor") == null) {
//            this.setFieldValByName("textColor", SystemConstant.TAG_TEXT_COLOR, metaObject);
//        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
