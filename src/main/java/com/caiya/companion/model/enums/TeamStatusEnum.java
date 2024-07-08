package com.caiya.companion.model.enums;

import java.util.Objects;

/**
 * @author caiya
 * @description 队伍状态枚举
 * @create 2024-07-08 22:57
 */
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private Integer value;
    private String text;

    TeamStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) return null;
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (Objects.equals(teamStatusEnum.getValue(), value)) return teamStatusEnum;
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
