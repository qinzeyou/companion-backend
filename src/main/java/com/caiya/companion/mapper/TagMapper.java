package com.caiya.companion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caiya.companion.model.domain.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2024-08-06 10:20:35
* @Entity generator.domain.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 热门标签分页查询
     * @param offset 查询起始位置
     * @param pageSize 查询多少条
     * @param userId 用户id，用于过滤用户身上已有的标签
     * @return 标签列表
     */
    List<Tag> hotTagPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize, @Param("userId") Long userId);
}




