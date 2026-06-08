package com.scriptkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scriptkill.entity.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerMapper extends BaseMapper<Player> {

    @Select("SELECT * FROM player WHERE session_id = #{sessionId} ORDER BY id ASC")
    List<Player> selectBySessionId(@Param("sessionId") Long sessionId);
}
