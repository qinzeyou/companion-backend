package com.caiya.companion.model.enums;

import java.util.Objects;

/**
 * @author caiya
 * @description 标签状态枚举类
 * @create 2024-08-06 15:41
 */
public enum TagStatusEnum {
    VISIBLE(1, "可见"),
    NOT_VISIBLE(0, "不可见");

    private String text;
    private Integer value;

    TagStatusEnum(Integer value, String text) {
        this.text = text;
        this.value = value;
    }

    public static TagStatusEnum getEnumByValue(Integer value) {
        if (value == null) return null;
        TagStatusEnum[] values = TagStatusEnum.values();
        for (TagStatusEnum tagStatusEnum : values) {
            if (Objects.equals(tagStatusEnum.getValue(), value)) return tagStatusEnum;
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
