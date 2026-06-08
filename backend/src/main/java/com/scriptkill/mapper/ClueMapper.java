package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.Clue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClueMapper extends BaseMapper<Clue> {

    @Select("SELECT * FROM clue WHERE script_id = #{scriptId} AND parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Clue> selectByScriptIdAndParentId(@Param("scriptId") Long scriptId, @Param("parentId") Long parentId);

    @Select("SELECT * FROM clue WHERE script_id = #{scriptId} AND is_public = 1 ORDER BY level ASC, sort_order ASC")
    List<Clue> selectPublicCluesByScriptId(@Param("scriptId") Long scriptId);
}
