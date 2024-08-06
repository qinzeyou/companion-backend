package com.caiya.companion.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author caiya
 * @description 树形标签返回结果
 * @create 2024-08-06 12:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TagTreeVO extends TagVO{
    // 非数据库字段=======
    /**
     * 二级标签
     */
    private List<TagVO> children;
}
